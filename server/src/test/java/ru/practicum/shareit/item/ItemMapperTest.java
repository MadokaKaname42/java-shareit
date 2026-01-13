package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.dto.ItemDtoPost;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemMapperTest {

    @Autowired
    private ItemMapper itemMapper;

    @Test
    void itemModelToItemDtoShouldMapFields() {
        Item item = new Item(1L, null, "Drill", "Powerful drill", true, null);

        ItemDto dto = itemMapper.itemModelToItemDto(item);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Drill");
        assertThat(dto.getDescription()).isEqualTo("Powerful drill");
        assertThat(dto.getAvailable()).isTrue();
    }

    @Test
    void itemDtoPostToItemModelShouldMapFields() {
        ItemDtoPost post = new ItemDtoPost("Saw", "Electric saw", true, null);

        Item item = itemMapper.itemDtoPostToItemModel(post);

        assertThat(item).isNotNull();
        assertThat(item.getId()).isNull();
        assertThat(item.getName()).isEqualTo("Saw");
        assertThat(item.getDescription()).isEqualTo("Electric saw");
        assertThat(item.isAvailable()).isTrue();
        assertThat(item.getItemRequest()).isNull();
    }

    @Test
    void mapItemsToDtoShouldMapListCorrectly() {
        Item item1 = new Item(1L, null, "Drill", "Powerful drill", true, null);
        Item item2 = new Item(2L, null, "Saw", "Electric saw", false, null);
        List<Item> items = List.of(item1, item2);

        List<ItemDto> dtos = itemMapper.mapItemsToDto(items);

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getName()).isEqualTo("Drill");
        assertThat(dtos.get(1).getName()).isEqualTo("Saw");
    }

    @Test
    void toItemDtoForRequestShouldMapFields() {
        Item item = new Item(10L, null, "Hammer", "Steel hammer", true, null);

        ItemDtoForRequest dto = itemMapper.toItemDtoForRequest(item);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("Hammer");
    }
}

