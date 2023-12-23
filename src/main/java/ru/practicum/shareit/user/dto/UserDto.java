package ru.practicum.shareit.user.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Builder
@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class UserDto {
    private final Long id;

    @NotNull
    private final String name;

    @Email
    @NotNull
    private final String email;
}
