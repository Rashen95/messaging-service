package ru.privalov.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.privalov.constant.UrlConstants;
import ru.privalov.dto.login.JwtResponse;
import ru.privalov.dto.login.LoginRequest;
import ru.privalov.dto.refresh.AccessTokenResponse;
import ru.privalov.dto.refresh.RefreshTokenRequest;
import ru.privalov.dto.registration.UserRegistrationRequest;
import ru.privalov.dto.registration.UserRegistrationResponse;
import ru.privalov.service.UserService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(UrlConstants.API + UrlConstants.EXTERNAL + UrlConstants.USERS)
public class UserExternalController {

    private final UserService userService;

    @PostMapping(UrlConstants.REGISTER)
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegistrationResponse register(@Valid @RequestBody UserRegistrationRequest request) {
        log.debug("Запрос на регистрацию пользователя: {}", request);
        UserRegistrationResponse response = userService.register(request);
        log.debug("Ответ по регистрации пользователя: {}", response);
        return response;
    }

    @PostMapping(UrlConstants.LOGIN)
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        log.debug("Запрос на логин пользователя: {}", request);
        JwtResponse response = userService.login(request);
        log.debug("Ответ по регистрации пользователя: {}", response);
        return response;
    }

    @PostMapping(UrlConstants.REFRESH)
    public AccessTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Запрос на обновление accessToken: {}", request);
        AccessTokenResponse response = userService.refresh(request);
        log.debug("Обновленный acсessToken: {}", response);
        return response;
    }

    @PostMapping(UrlConstants.LOGOUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Запрос на logout: {}", request);
        userService.logout(request);
    }

    @GetMapping(UrlConstants.EXISTS)
    public Boolean userExists(@PathVariable UUID userId) {
        log.debug("Запрос на проверку наличия пользователя: {}", userId);
        Boolean isUserExists = userService.userExists(userId);
        log.debug("Факт наличия пользователя {}: {}", userId, isUserExists);
        return isUserExists;
    }
}
