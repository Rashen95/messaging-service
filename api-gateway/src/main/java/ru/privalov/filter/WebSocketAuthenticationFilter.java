package ru.privalov.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.privalov.service.JwtService;

import java.util.Optional;
import java.util.UUID;

@Component
public class WebSocketAuthenticationFilter implements GlobalFilter, Ordered {

    public static final String USER_ID_HEADER = "X-User-Id";

    private static final String WEB_SOCKET_MESSAGES_PATH = "/ws/messages";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public WebSocketAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!WEB_SOCKET_MESSAGES_PATH.equals(exchange.getRequest().getPath().value())) {
            return chain.filter(exchange);
        }

        try {
            String token = extractToken(exchange)
                    .orElseThrow(() -> new IllegalArgumentException("JWT token is required"));
            UUID userId = jwtService.extractUserId(token);

            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .headers(headers -> {
                        headers.remove(USER_ID_HEADER);
                        headers.set(USER_ID_HEADER, userId.toString());
                    })
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception exception) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private Optional<String> extractToken(ServerWebExchange exchange) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            return Optional.of(authorization.substring(BEARER_PREFIX.length()));
        }

        String token = exchange.getRequest().getQueryParams().getFirst("access_token");
        if (token == null || token.isBlank()) {
            token = exchange.getRequest().getQueryParams().getFirst("token");
        }
        return Optional.ofNullable(token).filter(value -> !value.isBlank());
    }
}
