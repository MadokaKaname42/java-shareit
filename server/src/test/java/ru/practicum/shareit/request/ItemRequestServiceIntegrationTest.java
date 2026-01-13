package ru.practicum.shareit.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestPostDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemMapper itemMapper;

    private User requester;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setName("Requester");
        requester.setEmail("requester@mail.ru");
        requester = userRepository.save(requester);

        otherUser = new User();
        otherUser.setName("OtherUser");
        otherUser.setEmail("other@mail.ru");
        otherUser = userRepository.save(otherUser);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Cordless drill");
        item.setAvailable(true);
        item.setOwner(otherUser);
        itemRepository.save(item);
    }

    @AfterEach
    void tearDown() {
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createRequestShouldSaveRequest() {
        ItemRequestPostDto postDto = new ItemRequestPostDto("Need a drill");

        ItemRequestDto created = itemRequestService.createRequest(postDto, requester.getId());

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need a drill");
        System.out.println(created.getItems());
        assertThat(created.getItems()).isNull();
    }

    @Test
    void getUserRequestsShouldReturnRequesterRequests() {
        ItemRequestPostDto postDto1 = new ItemRequestPostDto("Need a drill");
        ItemRequestPostDto postDto2 = new ItemRequestPostDto("Need a hammer");
        itemRequestService.createRequest(postDto1, requester.getId());
        itemRequestService.createRequest(postDto2, requester.getId());

        List<ItemRequestDto> requests = itemRequestService.getUserRequests(requester.getId());

        assertThat(requests).hasSize(2);
        assertThat(requests).extracting(ItemRequestDto::getDescription)
                .containsExactly("Need a hammer", "Need a drill");
    }

    @Test
    void getAllRequestsShouldReturnRequestsOfOtherUsers() {
        ItemRequestPostDto postDto1 = new ItemRequestPostDto("Need a drill");
        itemRequestService.createRequest(postDto1, requester.getId());

        List<ItemRequestDto> requests = itemRequestService.getAllRequests(otherUser.getId());

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getDescription()).isEqualTo("Need a drill");
    }
}

