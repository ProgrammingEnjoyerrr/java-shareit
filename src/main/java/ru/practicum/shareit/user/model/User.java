package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
@Getter
public class User {
    private final Long id;
    private final String name;
    private final String email;
}
