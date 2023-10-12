package ru.practicum.shareit.item.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentCreateResponseDto {
    private Long id;
    private String text;
    private String authorName;
    private Boolean created;
}
