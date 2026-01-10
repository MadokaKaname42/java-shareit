package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.web.HeaderConstants;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.dto.CommentDtoPost;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoPost;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader(HeaderConstants.USER_ID) Long ownerId) {
        log.info("Get /items/ - getAllItemsByOwner -> {}", ownerId);
        return itemClient.getItemsByOwner(ownerId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@PathVariable Long itemId) {
        log.info("Get /items/{}", itemId);
        return itemClient.getItemById(itemId);
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody ItemDtoPost itemDtoPost,
                                         @RequestHeader(HeaderConstants.USER_ID) Long ownerId) {
        log.info("Post /items{} owner: {}", itemDtoPost, ownerId);
        return itemClient.createItem(itemDtoPost, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@PathVariable Long itemId,
                                         @RequestBody ItemDto itemDto,
                                         @RequestHeader(HeaderConstants.USER_ID) Long ownerId) {
        log.info("Patch /items/{} - {} owner: {}", itemId, itemDto, ownerId);
        return itemClient.updateItem(itemId, itemDto, ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        log.info("Get /items/search - text: {}", text);
        return itemClient.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@PathVariable Long itemId,
                                                @Valid @RequestBody CommentDtoPost commentDto,
                                                @RequestHeader(HeaderConstants.USER_ID) Long authorId) {
        log.info("Post /items/{}/comment by user {}", itemId, authorId);
        return itemClient.addComment(itemId, commentDto, authorId);
    }
}
