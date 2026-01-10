package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.web.HeaderConstants;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingShortDto bookingShortDto;
    private BookingDto bookingDto;

    @BeforeEach
    void setup() {
        bookingShortDto = BookingShortDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(1))
                .build();

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(bookingShortDto.getStart())
                .end(bookingShortDto.getEnd())
                .item(new ItemShortDto(1L, "Drill"))
                .booker(new UserShortDto(1L, "User"))
                .status(null)
                .build();
    }

    @Test
    void createBooking_ShouldReturnOk() throws Exception {
        when(bookingService.create(any(BookingShortDto.class), eq(1L)))
                .thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header(HeaderConstants.USER_ID, 1L)
                        .content(objectMapper.writeValueAsString(bookingShortDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()));

        Mockito.verify(bookingService).create(any(BookingShortDto.class), eq(1L));
    }

    @Test
    void approveBooking_ShouldReturnOk() throws Exception {
        when(bookingService.approve(eq(1L), eq(1L), eq(true)))
                .thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header(HeaderConstants.USER_ID, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()));

        Mockito.verify(bookingService).approve(eq(1L), eq(1L), eq(true));
    }

    @Test
    void getAllByOwner_ShouldReturnOk() throws Exception {
        when(bookingService.getAllByOwner(1L, "ALL"))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(HeaderConstants.USER_ID, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingDto.getId()));

        Mockito.verify(bookingService).getAllByOwner(1L, "ALL");
    }

    @Test
    void getAllByUser_ShouldReturnOk() throws Exception {
        when(bookingService.getAllByUser(1L, "ALL"))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(HeaderConstants.USER_ID, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingDto.getId()));

        Mockito.verify(bookingService).getAllByUser(1L, "ALL");
    }

    @Test
    void getBookingById_ShouldReturnOk() throws Exception {
        when(bookingService.getById(1L, 1L)).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(HeaderConstants.USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()));

        Mockito.verify(bookingService).getById(1L, 1L);
    }
}

