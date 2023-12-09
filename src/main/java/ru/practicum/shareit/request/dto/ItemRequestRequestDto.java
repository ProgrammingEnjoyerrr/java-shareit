package ru.practicum.shareit.request.dto;

import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@ToString
public class ItemRequestRequestDto {
    @NotBlank
    private String description;
}
