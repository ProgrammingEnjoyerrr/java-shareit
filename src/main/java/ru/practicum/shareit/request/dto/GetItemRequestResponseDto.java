package ru.practicum.shareit.request.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class GetItemRequestResponseDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    List<ItemWithRequestIdDto> items;
}
