package ru.practicum.shareit.exceptiontest;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(ErrorHandler.class)
class ErrorHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {

        @GetMapping("/not-found")
        void notFound() {
            throw new NotFoundException("not found");
        }

        @GetMapping("/validation")
        void validation() {
            throw new ValidationException("validation error");
        }

        @GetMapping("/bad-request")
        void badRequest() throws BadRequestException {
            throw new BadRequestException("bad request");
        }

        @GetMapping("/server-error")
        void serverError() {
            throw new RuntimeException("boom");
        }
    }

    @Test
    void handleNotFoundException_shouldReturn404() throws Exception {
        mockMvc.perform(get("/not-found"))
                .andExpect(status().isNotFound());
    }

    @Test
    void handleValidationException_shouldReturn409() throws Exception {
        mockMvc.perform(get("/validation"))
                .andExpect(status().isConflict());
    }

    @Test
    void handleBadRequestException_shouldReturn400() throws Exception {
        mockMvc.perform(get("/bad-request"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleServerError_shouldReturn500() throws Exception {
        mockMvc.perform(get("/server-error"))
                .andExpect(status().isInternalServerError());
    }
}

