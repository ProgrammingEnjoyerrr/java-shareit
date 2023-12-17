package ru.practicum.shareit.item.dto.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.model.Comment;

@UtilityClass
public class CommentMapper {
    public static CommentDtoResponse toCommentResponse(final Comment comment) {
        CommentDtoResponse commentDto = new CommentDtoResponse();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setCreated(comment.getCreated());
        return commentDto;
    }
}
