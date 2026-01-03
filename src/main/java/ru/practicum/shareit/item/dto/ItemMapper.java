package ru.practicum.shareit.item.dto;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.comments.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item itemDtoToItemModel(ItemDto itemDto);

    ItemDto itemModelToItemDto(Item item);


    @Mapping(target = "comments", expression = "java(getComments(item.getId(), commentRepository))")
    ItemDto itemModelToItemDto(Item item, @Context CommentRepository commentRepository);

    default List<CommentDto> getComments(Long itemId, CommentRepository commentRepository) {
        return commentRepository.findAllByItemId(itemId)
                .stream()
                .map(this::commentModelToCommentDto)
                .collect(toList());
    }

    CommentDto commentModelToCommentDto(Comment comment);
}
