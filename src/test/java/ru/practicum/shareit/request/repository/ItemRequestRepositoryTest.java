package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private final User owner = User.builder()
            .name("name")
            .email("example@email.com")
            .build();

    private final User user = User.builder()
            .name("user_name")
            .email("exampleEmail@email.com")
            .build();

    private final ItemRequest itemRequest1 = ItemRequest.builder()
            .requester(owner)
            .description("description")
            .created(LocalDateTime.now())
            .items(new ArrayList<>())
            .build();

    private final ItemRequest itemRequest2 = ItemRequest.builder()
            .requester(user)
            .description("some description")
            .created(LocalDateTime.now())
            .items(new ArrayList<>())
            .build();

    private final ItemRequest itemRequest3 = ItemRequest.builder()
            .requester(owner)
            .description("new description")
            .created(LocalDateTime.now().plusHours(2))
            .items(new ArrayList<>())
            .build();

    @BeforeEach
    public void init() {
        testEntityManager.persist(user);
        testEntityManager.persist(owner);

        testEntityManager.persist(itemRequest1);
        testEntityManager.persist(itemRequest2);
        testEntityManager.persist(itemRequest3);

        testEntityManager.flush();

        userRepository.save(user);
        userRepository.save(owner);

        itemRequestRepository.save(itemRequest1);
        itemRequestRepository.save(itemRequest2);
        itemRequestRepository.save(itemRequest3);
    }

    @AfterEach
    public void deInit() {
        itemRequestRepository.deleteAll();
    }

    @Test
    void findAllByRequesterId() {
        Collection<ItemRequest> requests = itemRequestRepository.findAllByRequesterId(user.getId());

        assertThat(requests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .usingRecursiveAssertion()
                .isEqualTo(List.of(itemRequest2));
    }

    @Test
    void findAllByRequester_IdNotOrderByCreatedDesc() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequester_IdNotOrderByCreatedDesc(user.getId(), PageRequest.of(0, 10));

        assertThat(
                requests.stream()
                        .sorted(Comparator.comparing(ItemRequest::getId))
                        .collect(Collectors.toList()))
                .isNotNull()
                .hasSize(2)
                .usingRecursiveAssertion()
                .isEqualTo(List.of(itemRequest1, itemRequest3));
    }
}