package ru.privalov.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.privalov.constant.UrlConstants;
import ru.privalov.dto.exists.UsersExistsRequest;
import ru.privalov.service.UserService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(UrlConstants.API + UrlConstants.INTERNAL + UrlConstants.USERS)
public class UserInternalController {

    private final UserService userService;

    @PostMapping(UrlConstants.EXISTS)
    public Map<UUID, Boolean> usersExists(@Valid @RequestBody UsersExistsRequest request) {
        log.debug("Запрос на проверку наличия пользователей: {}", request);
        Map<UUID, Boolean> isUserExists = userService.usersExists(request.recipientIds());
        log.debug("Факт наличия пользователей {}", isUserExists);
        return isUserExists;
    }
}
