package com.wefit.apiGateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class XssSanitizationFilter implements GlobalFilter, Ordered {

    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();
        MediaType contentType = request.getHeaders().getContentType();

        // Only intercept requests with a body and JSON content type
        if (method != null && (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT) || method.equals(HttpMethod.PATCH))
                && MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {

            return DataBufferUtils.join(request.getBody())
                    .flatMap(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);

                        String bodyString = new String(bytes, StandardCharsets.UTF_8);
                        String sanitizedBody = sanitize(bodyString);

                        byte[] sanitizedBytes = sanitizedBody.getBytes(StandardCharsets.UTF_8);

                        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {
                            @Override
                            public HttpHeaders getHeaders() {
                                HttpHeaders headers = new HttpHeaders();
                                headers.putAll(super.getHeaders());
                                headers.remove(HttpHeaders.CONTENT_LENGTH);
                                headers.setContentLength(sanitizedBytes.length);
                                return headers;
                            }

                            @Override
                            public Flux<DataBuffer> getBody() {
                                DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                                return Flux.just(bufferFactory.wrap(sanitizedBytes));
                            }
                        };

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    });
        }
        return chain.filter(exchange);
    }

    private String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return policy.sanitize(input);
    }

    @Override
    public int getOrder() {
        return -1; // Run early
    }
}
