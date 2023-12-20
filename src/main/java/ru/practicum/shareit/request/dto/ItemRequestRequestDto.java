package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@ToString
@EqualsAndHashCode
@Builder
public class ItemRequestRequestDto {
    @NotBlank
    private String description;
}
