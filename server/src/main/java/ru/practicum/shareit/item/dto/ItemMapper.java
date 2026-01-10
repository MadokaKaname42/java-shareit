package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto itemModelToItemDto(Item item);

    @Mapping(target = "itemRequest", ignore = true)
    Item itemDtoPostToItemModel(ItemDtoPost itemDtoPost);

    List<ItemDto> mapItemsToDto(List<Item> item);

    ItemDtoForRequest toItemDtoForRequest(Item item);
}
