package ru.practicum.shareit.booking.validate;

import ru.practicum.shareit.booking.dto.BookingCreateDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class DateEndAfterDateStartValidator implements ConstraintValidator<DateEndAfterDateStart, BookingCreateDto> {

    @Override
    public void initialize(DateEndAfterDateStart parameters) {
    }

    @Override
    public boolean isValid(BookingCreateDto bookingCreateDto, ConstraintValidatorContext constraintValidatorContext) {
        LocalDateTime start = bookingCreateDto.getStart();
        LocalDateTime end = bookingCreateDto.getEnd();
        if (start == null || end == null) {
            return false;
        }
        return end.isAfter(start);
    }
}