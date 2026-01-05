package ru.practicum.shareit.comments.dto;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.comments.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment commentDtoToCommentModel(CommentDto commentDto);

    @Mapping(source = "author.name", target = "authorName")
    CommentDto commentModelToCommentDto(Comment comment);

    @Mapping(source = "author.name", target = "authorName")
    List<CommentDto> toDto(List<Comment> comments);
}
