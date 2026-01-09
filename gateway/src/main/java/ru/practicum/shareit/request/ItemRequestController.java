package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.web.HeaderConstants;
import ru.practicum.shareit.request.dto.ItemRequestPostDto;


@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@Valid @RequestBody ItemRequestPostDto itemRequestDtoPost,
                                                @RequestHeader(HeaderConstants.USER_ID) Long requesterId) {
        log.info("POST /requests by user {}", requesterId);
        return itemRequestClient.createRequest(itemRequestDtoPost, requesterId);
    }

    // получение всех запросов текущего пользователя
    @GetMapping
    public ResponseEntity<Object> getMyRequests(@RequestHeader(HeaderConstants.USER_ID) Long requesterId) {
        log.info("GET /requests by user {}", requesterId);
        return itemRequestClient.getRequestsByUser(requesterId);
    }

    // получение всех запросов
    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long requesterId) {
        log.info("GET /requests/all by user {}", requesterId);
        return itemRequestClient.getAllRequests(requesterId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable Long requestId,
                                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /requests/{} by user {}", requestId, userId);
        return itemRequestClient.getRequestById(requestId, userId);
    }
}