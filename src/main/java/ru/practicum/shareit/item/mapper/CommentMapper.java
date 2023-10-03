package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentFullDto;
import ru.practicum.shareit.item.model.Comment;

@Mapper
public interface CommentMapper {
    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    @Mapping(source = "comment.author.name", target = "authorName")
    CommentFullDto toCommentFullDto(Comment comment);

    Comment toComment(CommentCreateDto commentCreateDto);
}
