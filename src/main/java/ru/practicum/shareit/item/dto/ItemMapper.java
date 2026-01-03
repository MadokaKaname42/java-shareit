package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item itemDtoToItemModel(ItemDto itemDto);

    ItemDto itemModelToItemDto(Item item);
}
