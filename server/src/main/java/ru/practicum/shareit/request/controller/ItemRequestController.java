package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.web.HeaderConstants;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestPostDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createRequest(@RequestBody ItemRequestPostDto itemRequestDtoPost,
                                        @RequestHeader(HeaderConstants.USER_ID) Long requesterId) {
        return itemRequestService.createRequest(itemRequestDtoPost, requesterId);
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader(HeaderConstants.USER_ID) Long requestorId) {
        return itemRequestService.getUserRequests(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(HeaderConstants.USER_ID) Long requesterId) {
        return itemRequestService.getAllRequests(requesterId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@PathVariable Long requestId,
                                         @RequestHeader(HeaderConstants.USER_ID) Long userId) {
        return itemRequestService.getRequestById(requestId, userId);
    }
}