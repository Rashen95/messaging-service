package ru.privalov.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.privalov.service.JwtService;

import java.util.UUID;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    public JwtAuthenticationManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.fromCallable(() -> {
            String token = authentication.getCredentials().toString();
            UUID userId = jwtService.extractUserId(token);
            return new UsernamePasswordAuthenticationToken(userId, token, AuthorityUtils.NO_AUTHORITIES);
        });
    }
}
