package ru.practicum.shareit.user.dto;

import lombok.*;

import javax.validation.constraints.Email;

@Builder
@Getter
@AllArgsConstructor
@ToString
public class UserUpdateDto {
    @Setter
    private Long id;

    private final String name;

    @Email
    private final String email;
}
