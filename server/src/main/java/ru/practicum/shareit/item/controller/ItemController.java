package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.dto.CommentDtoPost;
import ru.practicum.shareit.common.web.HeaderConstants;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoPost;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader(HeaderConstants.USER_ID) long userId) {
        return itemService.getAll(userId);
    }

    @GetMapping("/{id}")
    public ItemDto getById(@PathVariable long id) {
        return itemService.getById(id);
    }

    @PostMapping
    public ItemDto createItem(@RequestBody ItemDtoPost itemDtoPost,
                              @RequestHeader(HeaderConstants.USER_ID) Long ownerId) {
        return itemService.create(itemDtoPost, ownerId);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestBody ItemDtoPost itemDto, @PathVariable Long id,
                          @RequestHeader(HeaderConstants.USER_ID) Long userId) {
        return itemService.update(itemDto, id, userId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        itemService.delete(id);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@PathVariable Long itemId, @RequestHeader(HeaderConstants.USER_ID) Long userId,
                                    @RequestBody CommentDtoPost commentDto) {
        return itemService.createComment(itemId, userId, commentDto);
    }
}
