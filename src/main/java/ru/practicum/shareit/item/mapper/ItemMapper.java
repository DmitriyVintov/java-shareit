package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;

import java.util.List;

@Mapper(uses = {CommentMapper.class, ItemRequestMapper.class})
public interface ItemMapper {
    ItemMapper INSTANCE = Mappers.getMapper(ItemMapper.class);

    @Mapping(source = "nextBooking.booker.id", target = "nextBooking.bookerId")
    @Mapping(source = "lastBooking.booker.id", target = "lastBooking.bookerId")
    @Mapping(source = "request.id", target = "requestId")
    ItemDto toItemDto(Item item);

    Item toItem(ItemDto itemDto);

    List<ItemDto> toItemsDto(List<Item> items);

    List<Item> toItems(List<ItemDto> itemsDto);
}
