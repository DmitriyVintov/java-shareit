package ru.practicum.shareit.item;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    private final EasyRandom random = new EasyRandom();

    @Test
    void findAllByOwnerIdOrderById() {
        User owner = createUser();
        User requestor = createUser();
        ItemRequest itemRequest = createItemRequest(requestor);
        Item item = createItem(owner, itemRequest);
        Pageable pageable = Pageable.ofSize(20);

        assertEquals(item, itemRepository.findAllByOwnerIdOrderById(owner.getId(), pageable).get(0));
    }

    @Test
    void findAllByIdAndOwnerId() {
        User owner = createUser();
        User requestor = createUser();
        ItemRequest itemRequest = createItemRequest(requestor);
        Item item = createItem(owner, itemRequest);

        assertEquals(item, itemRepository.findByIdAndOwnerId(item.getId(), owner.getId()).get());
    }

    @Test
    void search() {
        User owner = createUser();
        User requestor = createUser();
        ItemRequest itemRequest = createItemRequest(requestor);
        Item item = createItem(owner, itemRequest);
        item.setName("шУруПоверТ");
        item.setDescription("Классный шУрУповерт");
        Pageable pageable = Pageable.ofSize(20);

        assertEquals(item, itemRepository.search("ШуРуп", pageable).get(0));
    }

    @Test
    void findItemsByRequestId() {
        User owner = createUser();
        User requestor = createUser();
        ItemRequest itemRequest = createItemRequest(requestor);
        Item item = createItem(owner, itemRequest);

        assertEquals(item, itemRepository.findItemsByRequestId(itemRequest.getId()).get(0));
    }

    @Test
    void findItemsByRequestIn() {
        User owner = createUser();
        User requestor = createUser();
        ItemRequest itemRequest = createItemRequest(requestor);
        List<ItemRequest> itemRequests = List.of(itemRequest);
        Item item = createItem(owner, itemRequest);

        assertEquals(item, itemRepository.findItemsByRequestIn(itemRequests).get(0));
    }

    private User createUser() {
        User owner = random.nextObject(User.class);
        return userRepository.save(owner);
    }

    private ItemRequest createItemRequest(User requestor) {
        ItemRequest itemRequest = random.nextObject(ItemRequest.class);
        itemRequest.setRequestor(requestor);
        return itemRequestRepository.save(itemRequest);
    }

    private Item createItem(User owner, ItemRequest itemRequest) {
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        item.setRequest(itemRequest);
        return itemRepository.save(item);
    }
}