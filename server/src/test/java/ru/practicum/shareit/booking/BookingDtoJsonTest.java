package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeBookingDto() throws Exception {
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2026, 1, 15, 10, 0, 0))
                .end(LocalDateTime.of(2026, 1, 17, 10, 0, 0))
                .status(BookingStatus.WAITING)
                .build();

        String json = objectMapper.writeValueAsString(dto);

        // Проверяем формат ISO с T
        assertThat(json).contains("\"start\":\"2026-01-15T10:00:00\"");
        assertThat(json).contains("\"end\":\"2026-01-17T10:00:00\"");
        assertThat(json).contains("\"durationDays\":2");
    }

    @Test
    void testDeserializeBookingDto() throws Exception {
        String json = """
                {
                  "id": 1,
                  "start": "2026-01-15T10:00:00",
                  "end": "2026-01-17T10:00:00",
                  "status": "WAITING"
                }
                """;

        BookingDto dto = objectMapper.readValue(json, BookingDto.class);

        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 1, 15, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 1, 17, 10, 0, 0));
        assertThat(dto.getDurationDays()).isEqualTo(2);
    }
}