package ru.practicum.shareit.request.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ItemRequestResponseDto {
    private Long id;

    private String name;

    private String description;

    private LocalDateTime created;

//    private List<Item> items;
}
