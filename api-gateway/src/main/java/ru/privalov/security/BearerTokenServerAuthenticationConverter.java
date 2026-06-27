package ru.privalov.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class BearerTokenServerAuthenticationConverter implements ServerAuthenticationConverter {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String token = extractFromAuthorizationHeader(exchange);
        if (token == null || token.isBlank()) {
            token = extractFromQuery(exchange);
        }
        if (token == null || token.isBlank()) {
            return Mono.empty();
        }

        return Mono.just(UsernamePasswordAuthenticationToken.unauthenticated(token, token));
    }

    private String extractFromAuthorizationHeader(ServerWebExchange exchange) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length());
    }

    private String extractFromQuery(ServerWebExchange exchange) {
        String token = exchange.getRequest().getQueryParams().getFirst("access_token");
        if (token == null || token.isBlank()) {
            token = exchange.getRequest().getQueryParams().getFirst("token");
        }
        return token;
    }
}
