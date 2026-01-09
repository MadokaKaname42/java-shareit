package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.dto.CommentDtoPost;
import ru.practicum.shareit.common.web.HeaderConstants;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoPost;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemDtoPost itemDtoPost;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        itemDtoPost = new ItemDtoPost("Drill", "Cordless drill", true, null);
        itemDto = new ItemDto(1L, "Drill", "Cordless drill", true, null, null, new ArrayList<>());
    }

    // ---------------------- GET ALL ----------------------
    @Test
    @SneakyThrows
    void getAllShouldReturnItems() {
        Mockito.when(itemService.getAll(1L)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header(HeaderConstants.USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()));

        Mockito.verify(itemService).getAll(1L);
    }

    // ---------------------- GET BY ID ----------------------
    @Test
    @SneakyThrows
    void getByIdShouldReturnItem() {
        Mockito.when(itemService.getById(1L)).thenReturn(itemDto);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()));

        Mockito.verify(itemService).getById(1L);
    }

    // ---------------------- CREATE ITEM ----------------------
    @Test
    @SneakyThrows
    void createItemShouldReturnCreatedItem() {
        Mockito.when(itemService.create(any(ItemDtoPost.class), eq(1L))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(HeaderConstants.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoPost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()));

        Mockito.verify(itemService).create(any(ItemDtoPost.class), eq(1L));
    }

    // ---------------------- UPDATE ITEM ----------------------
    @Test
    @SneakyThrows
    void updateShouldReturnUpdatedItem() {
        ItemDto updated = new ItemDto(1L, "Drill Updated", "Updated", true, null, null, new ArrayList<>());
        Mockito.when(itemService.update(any(ItemDtoPost.class), eq(1L), eq(1L))).thenReturn(updated);

        mockMvc.perform(patch("/items/1")
                        .header(HeaderConstants.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoPost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updated.getName()))
                .andExpect(jsonPath("$.description").value(updated.getDescription()));

        Mockito.verify(itemService).update(any(ItemDtoPost.class), eq(1L), eq(1L));
    }

    // ---------------------- DELETE ITEM ----------------------
    @Test
    @SneakyThrows
    void deleteShouldCallService() {
        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isOk());

        Mockito.verify(itemService).delete(1L);
    }

    // ---------------------- SEARCH ITEMS ----------------------
    @Test
    @SneakyThrows
    void searchShouldReturnItems() {
        Mockito.when(itemService.search("drill")).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()));

        Mockito.verify(itemService).search("drill");
    }

    // ---------------------- CREATE COMMENT ----------------------
    @Test
    @SneakyThrows
    void createCommentShouldReturnComment() {
        CommentDtoPost post = new CommentDtoPost("Great tool");
        CommentDto commentDto = new CommentDto(1L, "Great tool", "Alice", LocalDateTime.now());

        Mockito.when(itemService.createComment(eq(1L), eq(1L), any(CommentDtoPost.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(HeaderConstants.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()));

        Mockito.verify(itemService).createComment(eq(1L), eq(1L), any(CommentDtoPost.class));
    }
}