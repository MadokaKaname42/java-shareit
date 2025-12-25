package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
@Slf4j
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Item>> userItemIndex = new LinkedHashMap<>();

    private int id;

    public ItemRepositoryImpl() {
        id = 0;
    }

    @Override
    public List<Item> findAll() {
        log.info("Все вещи были найдены!");
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> getAllByUser(Long id) {
        return userItemIndex.getOrDefault(id, List.of());
    }

    @Override
    public Optional<Item> findById(Long id) {
        log.info("Вещь с ID {} найдена.", id);
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Item create(Item item) {
        item.setId(generateId());
        items.put(item.getId(), item);
        log.info("Вещь с ID {} добавлена.", item.getId());
        List<Item> items = userItemIndex.computeIfAbsent(item.getOwner().getId(), k -> new ArrayList<>());
        items.add(item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        log.info("Вещь с ID {} обновлена.", item.getId());
        return item;
    }

    @Override
    public void delete(Long id) {
        items.remove(id);
        log.info("Вещь с ID {} удалена.", id);
    }


    private long generateId() {
        return id++;
    }
}
