package com.wefit.apiGateway.filter;

import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.wefit.apiGateway.user.UserRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Reactive WebFilter that synchronizes Keycloak-authenticated users with the
 * internal userService. On each authenticated request it:
 * 1. Extracts the Keycloak subject (sub) and profile claims from the JWT.
 * 2. Looks up the user in userService by keycloakId (String, NOT Long).
 * 3. If the user doesn't exist, registers them automatically.
 * 4. Adds the internal user ID as an X-User-Id header for downstream services.
 */
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class KeyCloakUserSyncFilter implements WebFilter {

    private final WebClient userServiceWebClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // No JWT present — skip sync, let security handle rejection
            return chain.filter(exchange);
        }

        UserRequestDto userDetails = extractUserDetails(token);
        if (userDetails == null || userDetails.getKeycloakId() == null) {
            log.warn("Could not extract user details from JWT, skipping sync");
            return chain.filter(exchange);
        }

        String keycloakId = userDetails.getKeycloakId();

        // Look up the user by keycloakId (String) — no Long.parseLong needed
        return lookupUserByKeycloakId(keycloakId)
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    // User doesn't exist yet — register them
                    log.info("User not found for keycloakId={}, registering...", keycloakId);
                    return registerNewUser(userDetails);
                })
                .flatMap(syncResponse -> {
                    // Add internal user ID (Long) as header for downstream services
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", String.valueOf(syncResponse.getId()))
                            .header("X-User-Keycloak-Id", keycloakId)
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }

    /**
     * Calls GET /api/users/keycloak/{keycloakId} on userService.
     * Returns the user response if found, or propagates a 404.
     */
    private Mono<UserSyncResponse> lookupUserByKeycloakId(String keycloakId) {
        return userServiceWebClient.get()
                .uri("/api/users/keycloak/{keycloakId}", keycloakId)
                .retrieve()
                .bodyToMono(UserSyncResponse.class);
    }

    /**
     * Calls POST /api/users/register on userService with profile data
     * extracted from the JWT. Returns the newly created user.
     */
    private Mono<UserSyncResponse> registerNewUser(UserRequestDto userDetails) {
        // Set a placeholder password since Keycloak manages auth
        userDetails.setPassword("KEYCLOAK_MANAGED");

        log.info("Registering new user from Keycloak [keycloakId={}, email={}]",
                userDetails.getKeycloakId(), userDetails.getEmail());

        return userServiceWebClient.post()
                .uri("/api/users/register")
                .bodyValue(userDetails)
                .retrieve()
                .bodyToMono(UserSyncResponse.class);
    }

    /**
     * Extracts user details from the JWT Authorization header.
     * Parses the token directly using nimbus-jose (no Spring Security context
     * needed).
     */
    private UserRequestDto extractUserDetails(String token) {
        try {
            String jwt = token.substring(7); // Remove "Bearer "
            SignedJWT signedJwt = SignedJWT.parse(jwt);
            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();

            UserRequestDto dto = new UserRequestDto();
            dto.setKeycloakId(claims.getSubject());
            dto.setEmail(claims.getStringClaim("email"));
            dto.setFirstName(claims.getStringClaim("given_name"));
            dto.setLastName(claims.getStringClaim("family_name"));
            dto.setUserName(claims.getStringClaim("preferred_username"));
            return dto;
        } catch (Exception e) {
            log.error("Error parsing JWT for user details: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Minimal response DTO used only within this filter to extract the
     * internal user ID from userService responses.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class UserSyncResponse {
        private Long id;
        private String keycloakId;
    }
}
