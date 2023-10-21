package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.DataAlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentFullDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @MockBean
    ItemService itemService;
    @Autowired
    private MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    private final EasyRandom random = new EasyRandom();
    private final String HEADER_USER_ID = "X-Sharer-User-Id";

    @Test
    @DisplayName("Получение всех вещей")
    void getItems() throws Exception {
        long userId = 1L;
        List<ItemDto> itemsDto = random.objects(ItemDto.class, 2).collect(Collectors.toList());
        when(itemService.getItems(Mockito.anyLong(), Mockito.any(Pageable.class))).thenReturn(itemsDto);

        mvc.perform(get("/items")
                        .header(HEADER_USER_ID, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemsDto.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value(itemsDto.get(0).getName()))
                .andExpect(jsonPath("$[0].description").value(itemsDto.get(0).getDescription()))
                .andExpect(jsonPath("$[0].available").value(itemsDto.get(0).getAvailable()))
                .andExpect(jsonPath("$[1].id").value(itemsDto.get(1).getId()))
                .andExpect(jsonPath("$[1].name").value(itemsDto.get(1).getName()))
                .andExpect(jsonPath("$[1].description").value(itemsDto.get(1).getDescription()))
                .andExpect(jsonPath("$[1].available").value(itemsDto.get(1).getAvailable()));

        Mockito.verify(itemService).getItems(Mockito.anyLong(), Mockito.any(Pageable.class));
    }

    @Test
    @DisplayName("Получение вещи по id")
    void getItemById() throws Exception {
        ItemDto itemDto = random.nextObject(ItemDto.class);
        when(itemService.getItemById(Mockito.anyLong(), Mockito.anyLong())).thenReturn(itemDto);

        mvc.perform(get("/items/{itemId}", itemDto.getId())
                        .header(HEADER_USER_ID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));

        Mockito.verify(itemService).getItemById(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("Получение ошибки при получении вещи по id, если вещи не найдено")
    void shouldThrowExceptionWhenGetItemByIdWithNonExistentItem() throws Exception {
        ItemDto itemDto = random.nextObject(ItemDto.class);
        when(itemService.getItemById(Mockito.anyLong(), Mockito.anyLong())).thenThrow(NotFoundException.class);

        mvc.perform(get("/items/{itemId}", itemDto.getId())
                        .header(HEADER_USER_ID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Создание вещи")
    void addItem() throws Exception {
        ItemDto itemDto = random.nextObject(ItemDto.class);
        long userId = 1L;
        when(itemService.addItem(Mockito.anyLong(), Mockito.any(ItemDto.class))).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header(HEADER_USER_ID, userId)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));

        Mockito.verify(itemService).addItem(Mockito.anyLong(), Mockito.any(ItemDto.class));
    }

    @Test
    @DisplayName("Получение ошибки при создании вещи, если вещь уже существует")
    void shouldThrowExceptionWhenAddItemIfItemAlreadyExist() throws Exception {
        ItemDto itemDto = random.nextObject(ItemDto.class);
        long userId = 1L;
        when(itemService.addItem(Mockito.anyLong(), Mockito.any(ItemDto.class))).thenThrow(DataAlreadyExistException.class);

        mvc.perform(post("/items")
                        .header(HEADER_USER_ID, userId)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Получение ошибки валидации при создании вещи")
    void shouldThrowExceptionWhenAddItemIfDescriptionEmpty() throws Exception {
        ItemDto itemDto = random.nextObject(ItemDto.class);
        itemDto.setDescription(null);
        long userId = 1L;
        when(itemService.addItem(Mockito.anyLong(), Mockito.any(ItemDto.class))).thenThrow(ConstraintViolationException.class);

        mvc.perform(post("/items")
                        .header(HEADER_USER_ID, userId)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение ошибки валидации при создании вещи")
    void shouldThrowValidationExceptionWhenAddItem() throws Exception {
        ItemDto itemDto = random.nextObject(ItemDto.class);
        long userId = 1L;
        when(itemService.addItem(Mockito.anyLong(), Mockito.any(ItemDto.class))).thenThrow(ValidationException.class);

        mvc.perform(post("/items")
                        .header(HEADER_USER_ID, userId)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Обновление вещи")
    void updateItem() throws Exception {
        ItemDto itemDto = random.nextObject(ItemDto.class);
        long userId = 1L;
        when(itemService.updateItem(Mockito.anyLong(), Mockito.any(ItemDto.class), Mockito.anyLong())).thenReturn(itemDto);

        mvc.perform(patch("/items/{itemId}", itemDto.getId())
                        .header(HEADER_USER_ID, userId)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));

        Mockito.verify(itemService).updateItem(Mockito.anyLong(), Mockito.any(ItemDto.class), Mockito.anyLong());
    }

    @Test
    @DisplayName("Поиск вещей по поисковой фразе")
    void searchItems() throws Exception {
        List<ItemDto> itemsDto = random.objects(ItemDto.class, 1).collect(Collectors.toList());
        String text = "text";
        itemsDto.get(0).setName("find " + text);
        itemsDto.get(0).setDescription("find " + text);
        when(itemService.search(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Pageable.class))).thenReturn(itemsDto);

        mvc.perform(get("/items/search")
                        .header(HEADER_USER_ID, itemsDto.get(0).getOwner().getId())
                        .param("text", text)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemsDto.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value(itemsDto.get(0).getName()))
                .andExpect(jsonPath("$[0].description").value(itemsDto.get(0).getDescription()))
                .andExpect(jsonPath("$[0].available").value(itemsDto.get(0).getAvailable()));

        Mockito.verify(itemService).search(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Pageable.class));
    }

    @Test
    @DisplayName("Создание комментария к вещи")
    void addComment() throws Exception {
        long userId = 1L;
        ItemDto itemDto = random.nextObject(ItemDto.class);
        CommentCreateDto commentCreateDto = random.nextObject(CommentCreateDto.class);
        Comment comment = CommentMapper.INSTANCE.toComment(commentCreateDto);
        CommentFullDto commentFullDto = CommentMapper.INSTANCE.toCommentFullDto(comment);
        when(itemService.addComment(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(CommentCreateDto.class))).thenReturn(commentFullDto);

        mvc.perform(post("/items/{itemId}/comment", itemDto.getId())
                        .header(HEADER_USER_ID, userId)
                        .content(mapper.writeValueAsString(commentCreateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(commentCreateDto.getText()));

        Mockito.verify(itemService).addComment(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(CommentCreateDto.class));
    }
}