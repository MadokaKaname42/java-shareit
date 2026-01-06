package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long ownerId);

    @Query("SELECT i FROM Item i WHERE " +
           "(LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
           "AND i.available = true")
    List<Item> findBySearchText(@Param("text") String text);
    }
}
