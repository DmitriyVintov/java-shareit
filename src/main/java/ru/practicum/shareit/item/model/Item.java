package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "items", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    private String description;
    private Boolean available;
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;
    @Transient
    private Booking lastBooking;
    @Transient
    private Booking nextBooking;
    @Transient
    private List<Comment> comments;
    @OneToOne
    private ItemRequest request;
}
