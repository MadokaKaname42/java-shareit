package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestPostDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemRequestMapperTest {

    @Autowired
    private ItemRequestMapper mapper;

    @Test
    void toItemRequestDtoShouldMapAllFields() {
        User requestor = new User(1L, "Alice", "alice@mail.com");
        ItemRequest request = new ItemRequest(10L, "Need a drill", requestor, LocalDateTime.now(), null);

        ItemRequestDto dto = mapper.toItemRequestDto(request);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getDescription()).isEqualTo("Need a drill");
        assertThat(dto.getCreated()).isEqualTo(request.getCreated());
    }

    @Test
    void toItemRequestShouldMapFieldsAndSetDefaults() {
        User requestor = new User(2L, "Bob", "bob@mail.com");
        ItemRequestPostDto postDto = new ItemRequestPostDto("Need a chair");

        ItemRequest request = mapper.toItemRequest(postDto, requestor);

        assertThat(request).isNotNull();
        assertThat(request.getId()).isNull(); // id игнорируется
        assertThat(request.getDescription()).isEqualTo("Need a chair");
        assertThat(request.getRequestor()).isEqualTo(requestor);
        assertThat(request.getCreated()).isNotNull();
        assertThat(request.getCreated()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void toItemRequestDtoShouldHandleNullRequestor() {
        ItemRequest request = new ItemRequest(11L, "Empty requestor", null, LocalDateTime.now(), null);

        ItemRequestDto dto = mapper.toItemRequestDto(request);

        assertThat(dto).isNotNull();
    }
}

