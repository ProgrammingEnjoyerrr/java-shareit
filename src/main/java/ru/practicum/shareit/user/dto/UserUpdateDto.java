package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Email;

@Builder
@Getter
@RequiredArgsConstructor
public class UserUpdateDto {
    private final Long id;

    private final String name;

    @Email
    private final String email;
}
