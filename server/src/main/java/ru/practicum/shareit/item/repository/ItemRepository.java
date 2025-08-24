package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerIdOrderById(Long userId, Pageable pageable);

    Optional<Item> findByIdAndOwnerId(Long itemId, Long userId);

    boolean existsByIdAndOwnerId(Long itemId, Long userId);

    @Query(" select i from Item i " +
            "where (lower(i.name) like lower(concat('%', ?1, '%')) " +
            " or lower(i.description) like lower(concat('%', ?1, '%')) ) and i.available = true ")
    List<Item> search(String text, Pageable pageable);

    List<Item> findItemsByRequestId(long requestId);

    List<Item> findItemsByRequestIn(List<ItemRequest> itemRequests);
}