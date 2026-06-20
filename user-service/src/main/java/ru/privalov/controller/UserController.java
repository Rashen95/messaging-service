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
import ru.privalov.dto.JwtResponse;
import ru.privalov.dto.LoginRequest;
import ru.privalov.dto.UserRegistrationRequest;
import ru.privalov.dto.UserResponse;
import ru.privalov.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping(UrlConstants.USERS_API)
public class UserController {

    private final UserService userService;

    @PostMapping(UrlConstants.REGISTER_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserRegistrationRequest request) {
        return userService.register(request);
    }

    @PostMapping(UrlConstants.LOGIN_URL)
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
