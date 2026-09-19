package com.wefit.apiGateway.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@Configuration
public class HttpToHttpsRedirectConfig {

    @Value("${server.port:8443}")
    private int httpsPort;

    @Value("${server.http.port:8085}")
    private int httpPort;

    private DisposableServer disposableServer;

    @PostConstruct
    public void startRedirectServer() {
        disposableServer = HttpServer.create()
                .port(httpPort)
                .handle((request, response) -> {
                    String host = request.requestHeaders().get("Host");
                    if (host != null && host.contains(":")) {
                        host = host.split(":")[0];
                    }
                    String httpsUri = "https://" + (host != null ? host : "localhost") + ":" + httpsPort + request.uri();
                    return response.status(HttpStatus.MOVED_PERMANENTLY)
                            .header("Location", httpsUri)
                            .send();
                })
                .bindNow();
    }

    @PreDestroy
    public void stopRedirectServer() {
        if (disposableServer != null) {
            disposableServer.disposeNow();
        }
    }
}
