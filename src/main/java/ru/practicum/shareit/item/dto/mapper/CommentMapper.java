package ru.practicum.shareit.item.dto.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;


@UtilityClass
public class CommentMapper {

    public Comment toComment(final CommentDto commentDto, final Item item,
                             final User user) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        return comment;
    }

    public CommentDtoResponse toCommentResponse(final Comment comment) {
        CommentDtoResponse commentDto = new CommentDtoResponse();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setCreated(comment.getCreated());
        return commentDto;
    }

    public CommentDto toCommentDto(final Comment comment) {
        return CommentDto.builder().text(comment.getText()).build();
    }
}
