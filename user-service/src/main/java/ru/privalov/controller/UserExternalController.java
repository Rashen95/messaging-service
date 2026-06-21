package ru.privalov.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.privalov.constant.UrlConstants;
import ru.privalov.dto.login.JwtResponse;
import ru.privalov.dto.login.LoginRequest;
import ru.privalov.dto.registration.UserRegistrationRequest;
import ru.privalov.dto.registration.UserRegistrationResponse;
import ru.privalov.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping(UrlConstants.API + UrlConstants.EXTERNAL + UrlConstants.USERS)
public class UserExternalController {

    private final UserService userService;

    @PostMapping(UrlConstants.REGISTER)
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegistrationResponse register(@Valid @RequestBody UserRegistrationRequest request) {
        return userService.register(request);
    }

    @PostMapping(UrlConstants.LOGIN)
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
