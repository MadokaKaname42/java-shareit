package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.dto.CommentDtoPost;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoPost;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAll(Long userId);

    ItemDto getById(Long id);

    ItemDto create(ItemDtoPost itemDto, Long userId);

    ItemDto update(ItemDtoPost itemDto, Long id, Long userId);

    void delete(Long id);

    List<ItemDto> search(String text);

    CommentDto createComment(Long itemId, Long userId, CommentDtoPost commentDto);
}

