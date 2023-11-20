package ru.practicum.shareit.request;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Disabled
class ItemRequestRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    private final EasyRandom random = new EasyRandom();

    @Test
    void findAllByRequestorId() {
        User requestor = createUser();
        ItemRequest itemRequest = createItemRequest(requestor);

        assertEquals(itemRequest, itemRequestRepository.findByRequestorId(requestor.getId()).get(0));
    }

    @Test
    void findAllByRequestorIdNot() {
        User requestor1 = createUser();
        User requestor2 = createUser();
        ItemRequest itemRequest = createItemRequest(requestor1);

        assertEquals(itemRequest, itemRequestRepository.findByRequestorIdNot(requestor2.getId()).get(0));
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
}