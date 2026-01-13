package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.dto.CommentDtoPost;
import ru.practicum.shareit.comments.dto.CommentMapper;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    private CommentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CommentMapper.class);
    }

    @Test
    void commentModelToCommentDto_shouldMapAuthorName() {
        User author = User.builder().id(1L).name("Alice").email("alice@mail.com").build();
        Item item = Item.builder().id(1L).name("Item1").description("Desc").available(true).owner(author).build();
        Comment comment = Comment.builder()
                .id(10L)
                .text("Nice item")
                .author(author)
                .item(item)
                .created(LocalDateTime.now())
                .build();

        CommentDto dto = mapper.commentModelToCommentDto(comment);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(comment.getId());
        assertThat(dto.getText()).isEqualTo(comment.getText());
        assertThat(dto.getAuthorName()).isEqualTo(author.getName());
        assertThat(dto.getCreated()).isEqualTo(comment.getCreated());
    }

    @Test
    void toDto_shouldMapListOfComments() {
        User author = User.builder().id(1L).name("Alice").email("alice@mail.com").build();
        Item item = Item.builder().id(1L).name("Item1").description("Desc").available(true).owner(author).build();
        Comment comment1 = Comment.builder().id(1L).text("Comment1").author(author).item(item).created(LocalDateTime.now()).build();
        Comment comment2 = Comment.builder().id(2L).text("Comment2").author(author).item(item).created(LocalDateTime.now()).build();

        List<CommentDto> dtos = mapper.toDto(List.of(comment1, comment2));

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getText()).isEqualTo("Comment1");
        assertThat(dtos.get(1).getText()).isEqualTo("Comment2");
        assertThat(dtos.get(0).getAuthorName()).isEqualTo("Alice");
    }

    @Test
    void toComment_shouldMapFieldsAndSetCreatedNow() {
        User author = User.builder().id(1L).name("Alice").email("alice@mail.com").build();
        Item item = Item.builder().id(1L).name("Item1").description("Desc").available(true).owner(author).build();
        CommentDtoPost dtoPost = CommentDtoPost.builder().text("New comment").build();

        Comment comment = mapper.toComment(dtoPost, item, author);

        assertThat(comment.getId()).isNull(); // игнорируется
        assertThat(comment.getText()).isEqualTo(dtoPost.getText());
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getItem()).isEqualTo(item);
        assertThat(comment.getCreated()).isNotNull();
        assertThat(comment.getCreated()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}

