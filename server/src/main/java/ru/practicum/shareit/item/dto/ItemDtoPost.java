package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemDtoPost {
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}
