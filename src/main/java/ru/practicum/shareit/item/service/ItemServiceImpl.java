package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ItemDto> getAll(Long userId) {
        List<ItemDto> items = new ArrayList<>();
        for (Item item : itemRepository.getAllByUser(userId)) {
            items.add(toItemDto(item));
        }
        return items;
    }

    @Override
    public ItemDto getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена id: " + id));

        return toItemDto(item);
    }

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Нельзя создать вещь. Пользователь не найден id: " + userId));
        Item item = toItem(itemDto);
        item.setOwner(user);
        itemRepository.create(item);

        return toItemDto(item);
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена id: " + id));
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Не возможно обновить вещь с пользователя id: " + userId + " Не найдена вещь");
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return toItemDto(item);
    }

    @Override
    public void delete(Long id) {
        getById(id);
        itemRepository.delete(id);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (!text.isBlank()) {
            return itemRepository.findAll().stream().filter(i ->
                    isSearched(text, i)).map(ItemMapper::toItemDto).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private Boolean isSearched(String text, Item item) {
        return (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                item.getDescription().toLowerCase().contains(text.toLowerCase())) && item.isAvailable();
    }
}
