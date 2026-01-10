package ru.practicum.shareit.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlerTest {

    private ErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = new ErrorHandler();
    }


    @Test
    void handleValidationException_returnsErrorResponse() {
        ValidationException ex = new ValidationException("Invalid data");

        ErrorResponse response = errorHandler.handleValidationException(ex);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo("Invalid data");
    }

    @Test
    void handleNotFoundException_returnsErrorResponse() {
        NotFoundException ex = new NotFoundException("Object not found");

        ErrorResponse response = errorHandler.handleNotFoundException(ex);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo("Object not found");
    }

    @Test
    void handleBadRequestException_returnsErrorResponse() {
        BadRequestException ex = new BadRequestException("Bad request");

        ErrorResponse response = errorHandler.handleBadRequestException(ex);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo("Bad request");
    }

    @Test
    void handleServerError_returnsErrorResponse() {
        Throwable ex = new RuntimeException("Unexpected error");

        ErrorResponse response = errorHandler.handleServerError(ex);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo("Unexpected error");
    }
}
