package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Builder
@Getter
@RequiredArgsConstructor
@ToString
public class CommentDto {
    @NotBlank
    private final String text;
}
