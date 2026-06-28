package ru.privalov.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.privalov.constant.UrlConstants;
import ru.privalov.dto.login.LoginRequest;
import ru.privalov.dto.login.LoginResponse;
import ru.privalov.dto.refresh.RefreshTokenRequest;
import ru.privalov.dto.refresh.RefreshTokenResponse;
import ru.privalov.dto.registration.UserRegistrationRequest;
import ru.privalov.dto.registration.UserRegistrationResponse;
import ru.privalov.service.UserService;

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
        log.debug("Ответ на регистрацию пользователя: {}", response);
        return response;
    }

    @PostMapping(UrlConstants.LOGIN)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        log.debug("Запрос на логин пользователя: {}", request);
        LoginResponse response = userService.login(request);
        log.debug("Ответ на логин пользователя: {}", response);
        return response;
    }

    @PostMapping(UrlConstants.REFRESH)
    public RefreshTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Запрос на обновление accessToken: {}", request);
        RefreshTokenResponse response = userService.refresh(request);
        log.debug("Ответ на обновление accessToken: {}", response);
        return response;
    }

    @PostMapping(UrlConstants.LOGOUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Запрос на logout: {}", request);
        userService.logoutUser(request);
    }
}
