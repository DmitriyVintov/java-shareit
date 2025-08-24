package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@Disabled
class ItemRequestControllerTest {
    @MockBean
    private ItemRequestService itemRequestService;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    private final EasyRandom random = new EasyRandom();

    @Test
    @DisplayName("Создание запроса вещи")
    void addItemRequest() throws Exception {
        long userId = 1L;
        ItemRequestCreateDto itemRequestCreateDto = random.nextObject(ItemRequestCreateDto.class);
        ItemRequest itemRequest = ItemRequestMapper.INSTANCE.toItemRequestFromItemRequestCreateDto(itemRequestCreateDto);
        ItemRequestFullDto itemRequestFullDto = ItemRequestMapper.INSTANCE.toItemRequestFullDto(itemRequest);

        when(itemRequestService.addItemRequest(userId, itemRequestCreateDto)).thenReturn(itemRequestFullDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemRequestCreateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(itemRequestCreateDto.getDescription()));

        Mockito.verify(itemRequestService).addItemRequest(Mockito.anyLong(), Mockito.any(ItemRequestCreateDto.class));
    }

    @Test
    @DisplayName("Получение списка своих запросов")
    void getItemRequestsByRequestorId() throws Exception {
        long requestorId = 1L;
        User requestor = random.nextObject(User.class);
        requestor.setId(requestorId);
        ItemRequest itemRequest = random.nextObject(ItemRequest.class);
        itemRequest.setRequestor(requestor);
        ItemRequestFullDto itemRequestFullDto = ItemRequestMapper.INSTANCE.toItemRequestFullDto(itemRequest);
        List<ItemRequestFullDto> itemRequests = List.of(itemRequestFullDto);
        itemRequests.get(0).setItems(null);

        when(itemRequestService.getItemRequestsByRequestorId(Mockito.anyLong())).thenReturn(itemRequests);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", requestorId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemRequests.get(0).getId()))
                .andExpect(jsonPath("$[0].description").value(itemRequests.get(0).getDescription()));

        Mockito.verify(itemRequestService).getItemRequestsByRequestorId(Mockito.anyLong());
    }

    @Test
    @DisplayName("Получение списка запросов других пользователей")
    void getItemRequestsAll() throws Exception {
        long userId = 2L;
        User user = random.nextObject(User.class);
        user.setId(userId);
        ItemRequest itemRequest = random.nextObject(ItemRequest.class);
        itemRequest.setRequestor(user);
        ItemRequestFullDto itemRequestFullDto = ItemRequestMapper.INSTANCE.toItemRequestFullDto(itemRequest);
        List<ItemRequestFullDto> itemRequests = List.of(itemRequestFullDto);
        itemRequests.get(0).setItems(null);

        when(itemRequestService.getItemRequestsAll(Mockito.anyLong(), Mockito.any(Pageable.class))).thenReturn(itemRequests);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemRequests.get(0).getId()))
                .andExpect(jsonPath("$[0].description").value(itemRequests.get(0).getDescription()));

        Mockito.verify(itemRequestService).getItemRequestsAll(Mockito.anyLong(), Mockito.any(Pageable.class));
    }

    @Test
    @DisplayName("Получение запроса вещи по id")
    void getItemRequestById() throws Exception {
        long requestorId = 1L;
        long requestId = 1L;
        ItemRequestFullDto itemRequestFullDto = random.nextObject(ItemRequestFullDto.class);

        when(itemRequestService.getItemRequestById(Mockito.anyLong(), Mockito.anyLong())).thenReturn(itemRequestFullDto);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", requestorId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestFullDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestFullDto.getDescription()));

        Mockito.verify(itemRequestService).getItemRequestById(Mockito.anyLong(), Mockito.anyLong());
    }
}