package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item itemDtoToItemModel(ItemDto itemDto);

    ItemDto itemModelToItemDto(Item item);

    List<ItemDto> mapItemsToDtos(List<Item> item);

    ItemShortDto itemModelToItemShortDto(Item item);
}
