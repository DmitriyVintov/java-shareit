package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Mapper(uses = {ItemMapper.class})
public interface ItemRequestMapper {
    ItemRequestMapper INSTANCE = Mappers.getMapper(ItemRequestMapper.class);

    ItemRequestFullDto toItemRequestFullDto(ItemRequest itemRequest);

    ItemRequest toItemRequestFromItemRequestCreateDto(ItemRequestCreateDto itemRequestCreateDto);

    List<ItemRequestFullDto> toItemRequestsFullDto(List<ItemRequest> itemRequests);
}
