package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestPostDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestMapper itemRequestMapper;
    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestServiceImpl service;

    private User user;
    private ItemRequest request;
    private ItemRequestDto dto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Alice", "alice@mail.com");
        request = new ItemRequest(10L, "Need a drill", user, LocalDateTime.now(), null);
        dto = new ItemRequestDto(10L, "Need a drill", request.getCreated(), List.of());
    }

    // ---------------- CREATE ----------------

    @Test
    void createRequest_whenUserNotFound_thenThrowNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.createRequest(new ItemRequestPostDto("Need a drill"), 1L)
        );

        assertEquals("Пользователь с id=1 не существует", ex.getMessage());
        verifyNoInteractions(itemRequestRepository);
    }

    @Test
    void createRequest_whenValid_thenReturnDto() {
        ItemRequestPostDto postDto = new ItemRequestPostDto("Need a drill");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestMapper.toItemRequest(postDto, user)).thenReturn(request);
        when(itemRequestRepository.save(request)).thenReturn(request);
        when(itemRequestMapper.toItemRequestDto(request)).thenReturn(dto);

        ItemRequestDto result = service.createRequest(postDto, 1L);

        assertEquals(dto, result);
        verify(itemRequestRepository).save(request);
    }

    // ---------------- GET USER REQUESTS ----------------

    @Test
    void getUserRequests_whenUserNotFound_thenThrowNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.getUserRequests(1L)
        );
    }

    @Test
    void getUserRequests_whenRequestsExist_thenReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(request));
        when(itemRequestMapper.toItemRequestDto(request)).thenReturn(dto);

        List<ItemRequestDto> result = service.getUserRequests(1L);

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
    }

    @Test
    void getAllRequests_whenUserNotFound_thenThrowNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.getAllRequests(1L)
        );
    }

    @Test
    void getAllRequests_whenNoRequests_thenReturnEmptyList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(1L))
                .thenReturn(List.of());

        List<ItemRequestDto> result = service.getAllRequests(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllRequests_whenRequestsExist_thenReturnDtosWithItems() {
        Item item = new Item(5L, user, "Drill", "desc", true, request);
        ItemDtoForRequest itemDto = new ItemDtoForRequest(5L, "Drill", user.getId());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(1L))
                .thenReturn(List.of(request));
        when(itemRepository.findAllByItemRequestIdInOrderById(List.of(10L)))
                .thenReturn(List.of(item));
        when(itemMapper.toItemDtoForRequest(item)).thenReturn(itemDto);
        when(itemRequestMapper.toItemRequestDto(request))
                .thenReturn(new ItemRequestDto(10L, "Need a drill", request.getCreated(), new ArrayList<>()));

        List<ItemRequestDto> result = service.getAllRequests(1L);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getItems().size());
    }

    @Test
    void getRequestById_whenUserNotFound_thenThrowNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.getRequestById(10L, 1L)
        );
    }

    @Test
    void getRequestById_whenRequestNotFound_thenThrowNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.getRequestById(10L, 1L)
        );
    }

    @Test
    void getRequestById_whenExists_thenReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRequestMapper.toItemRequestDto(request)).thenReturn(dto);

        ItemRequestDto result = service.getRequestById(10L, 1L);

        assertEquals(dto, result);
    }
}

