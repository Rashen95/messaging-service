package ru.privalov.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.privalov.dto.registration.UserRegistrationRequest;
import ru.privalov.dto.registration.UserRegistrationResponse;
import ru.privalov.model.User;

import java.util.Locale;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "username", expression = "java(normalize(request.username()))")
    @Mapping(target = "email", expression = "java(normalize(request.email()))")
    @Mapping(target = "passwordHash", expression = "java(passwordHash)")
    @Mapping(target = "firstName", expression = "java(request.firstName())")
    @Mapping(target = "lastName", expression = "java(request.lastName())")
    User toEntity(UserRegistrationRequest request, String passwordHash);

    @Mapping(target = "username", expression = "java(user.getUsername())")
    @Mapping(target = "email", expression = "java(user.getEmail())")
    @Mapping(target = "firstName", expression = "java(user.getFirstName())")
    @Mapping(target = "lastName", expression = "java(user.getLastName())")
    UserRegistrationResponse toRegistrationResponse(User user);

    default String normalize(String value) {
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
