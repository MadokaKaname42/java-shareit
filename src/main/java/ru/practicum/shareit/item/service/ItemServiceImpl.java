package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.dto.CommentMapper;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.comments.repository.CommentRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Comparator;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;


@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final BookingMapper bookingMapper;


    @Override
    public List<ItemDto> getAll(Long ownerId) {
        List<ItemDto> itemDtos = itemMapper.mapItemsToDtos(itemRepository.findAllByOwnerId(ownerId));

        User owner = userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<BookingForItemDto> bookingDtos = bookingMapper.mapBookingsToBookingForItemDtos(bookingRepository.findAllByBookerAndStatusEquals(owner, BookingStatus.APPROVED, Sort.by("id")));
        enrichItemsWithBookingInfo(itemDtos, bookingDtos);
        return itemDtos;
    }

    @Override
    public ItemDto getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена id: " + id));
        ItemDto itemDto = itemMapper.itemModelToItemDto(item);
        itemDto.setComments(commentMapper.toDto(commentRepository.findAllByItemId(id)));
        return itemDto;
    }

    @Transactional
    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Нельзя создать вещь. Пользователь не найден id: " + userId));
        Item item = itemMapper.itemDtoToItemModel(itemDto);
        item.setOwner(user);
        itemRepository.save(item);

        return itemMapper.itemModelToItemDto(item);
    }

    @Transactional
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

        return itemMapper.itemModelToItemDto(item);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена id: " + id));
        itemRepository.delete(item);
    }

    @Override
    public List<ItemDto> search(String text) {
    if (text == null || text.isBlank()) {
        return Collections.emptyList();
    }
    List<Item> items = itemRepository.findBySearchText(text);
    return items.stream()
                .map(itemMapper::itemModelToItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto createComment(Long itemId, Long userId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Does not exist User with Id " + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Does not exist Item with Id " + itemId));
        if (bookingRepository.findAllByBookerIdAndItemIdAndStatusEqualsAndEndIsBefore(userId, itemId, BookingStatus.APPROVED,
                now()).isEmpty()) {
            throw new BadRequestException("Item has not been rented by the user or the rental of the item has not yet been completed");
        }
        Comment comment = commentMapper.commentDtoToCommentModel(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(now());
        commentRepository.save(comment);

        return commentMapper.commentModelToCommentDto(comment);
    }

    public void enrichItemsWithBookingInfo(List<ItemDto> itemDtos, List<BookingForItemDto> bookings) {
        Map<Long, List<BookingForItemDto>> bookingsByItemId = bookings.stream()
            .collect(Collectors.groupingBy(BookingForItemDto::getItemId));
        LocalDateTime now = LocalDateTime.now();
        for (ItemDto item : itemDtos) {
            Long itemId = item.getId();
            List<BookingForItemDto> itemBookings = bookingsByItemId.getOrDefault(itemId, Collections.emptyList());
            Optional<BookingForItemDto> lastBookingOpt = itemBookings.stream()
                .filter(b -> b.getStart().isBefore(now))
                .max(Comparator.comparing(BookingForItemDto::getStart));
            Optional<BookingForItemDto> nextBookingOpt = itemBookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(BookingForItemDto::getStart));
            lastBookingOpt.ifPresent(lastBooking -> {
                item.setLastBooking(lastBooking);
            });
            nextBookingOpt.ifPresent(nextBooking -> {
                item.setNextBooking(nextBooking);
            });
        }
    }
}
