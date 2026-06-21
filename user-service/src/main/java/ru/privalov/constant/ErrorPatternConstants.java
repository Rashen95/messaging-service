package ru.privalov.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorPatternConstants {
    public final String USERNAME_ALREADY_EXISTS = "Пользователь %s уже зарегистрирован в системе";
    public final String EMAIL_ALREADY_EXISTS = "Email %s уже используется";
    public final String INVALID_USERNAME_OR_PASSWORD = "Неверное имя пользователя или пароль";
    public final String INVALID_REFRESH_TOKEN = "Невалидный refresh токен";
}
