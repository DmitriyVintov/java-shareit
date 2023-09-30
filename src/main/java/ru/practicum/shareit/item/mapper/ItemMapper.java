package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

@Mapper(uses = {CommentMapper.class})
public interface ItemMapper {
    ItemMapper INSTANCE = Mappers.getMapper(ItemMapper.class);

    @Mapping(source = "nextBooking.booker.id", target = "nextBooking.bookerId")
    @Mapping(source = "lastBooking.booker.id", target = "lastBooking.bookerId")
    ItemDto toItemDto(Item item);

    Item toItem(ItemDto itemDto);
}
