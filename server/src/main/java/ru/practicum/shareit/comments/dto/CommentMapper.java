package ru.practicum.shareit.comments.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(source = "author.name", target = "authorName")
    CommentDto commentModelToCommentDto(Comment comment);

    @Mapping(source = "author.name", target = "authorName")
    List<CommentDto> toDto(List<Comment> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    Comment toComment(CommentDtoPost commentDtoPost, Item item, User author);
}
