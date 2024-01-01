package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private final User user = User.builder()
            .name("name")
            .email("email@email.com")
            .build();

    private final User owner = User.builder()
            .name("name2")
            .email("email2@email.com")
            .build();

    private final Item item = Item.builder()
            .name("name")
            .description("description")
            .available(true)
            .owner(owner)
            .build();

    private final Booking booking = Booking.builder()
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .startDate(LocalDateTime.now().minusHours(1L))
            .endDate(LocalDateTime.now().plusDays(1L))
            .build();

    private final Booking pastBooking = Booking.builder()
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .startDate(LocalDateTime.now().minusDays(2L))
            .endDate(LocalDateTime.now().minusDays(1L))
            .build();

    private final Booking futureBooking = Booking.builder()
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .startDate(LocalDateTime.now().plusDays(1L))
            .endDate(LocalDateTime.now().plusDays(2L))
            .build();

    private final List<Booking> expected = List.of(booking, pastBooking, futureBooking);

    @BeforeEach
    public void init() {
        testEntityManager.persist(user);
        testEntityManager.persist(owner);
        testEntityManager.persist(item);
        testEntityManager.flush();
        bookingRepository.save(booking);
        bookingRepository.save(pastBooking);
        bookingRepository.save(futureBooking);
    }

    @AfterEach
    public void deleteAll() {
        bookingRepository.deleteAll();
    }

    @Test
    void findAllBookingsForBookerByStatus() {
        List<Booking> bookings = bookingRepository.findAllBookingsForBookerByStatus(1L, PageRequest.of(0, 10));

        assertThat(
                bookings.stream()
                        .sorted(Comparator.comparing(Booking::getId))
                        .collect(Collectors.toList()))
                .hasSize(3)
                .usingRecursiveAssertion()
                .isEqualTo(expected);
    }

    @Test
    void findByBookerId() {
        List<Booking> bookings = bookingRepository.findByBookerId(1L);

        assertThat(
                bookings.stream()
                        .sorted(Comparator.comparing(Booking::getId))
                        .collect(Collectors.toList()))
                .hasSize(3)
                .usingRecursiveAssertion()
                .isEqualTo(expected);
    }

    @Test
    void findByItemId() {
        List<Booking> bookings = bookingRepository.findByItemId(item.getId(), Sort.by(Sort.Order.asc("id")));

        assertThat(bookings)
                .hasSize(3)
                .usingRecursiveAssertion()
                .isEqualTo(expected);
    }

    @Test
    void findAll() {
        List<Booking> bookings = bookingRepository.findAll();

        assertThat(
                bookings.stream()
                        .sorted(Comparator.comparing(Booking::getId))
                        .collect(Collectors.toList()))
                .hasSize(3)
                .usingRecursiveAssertion()
                .isEqualTo(expected);
    }

    @Test
    void findByItemIdIn() {
        Collection<Long> collection = List.of(item.getId());
        List<Booking> bookings = bookingRepository.findByItemIdIn(collection, PageRequest.of(0, 10));

        assertThat(
                bookings.stream()
                        .sorted(Comparator.comparing(Booking::getId))
                        .collect(Collectors.toList()))
                .hasSize(3)
                .usingRecursiveAssertion()
                .isEqualTo(expected);
    }

    @Test
    void findAllByItemAndStatusOrderByStartDateAsc() {
    }

    @Test
    void findAllByUserBookings() {
        LocalDateTime dateTime = LocalDateTime.now();

        List<Booking> bookings = bookingRepository.findAllByUserBookings(user.getId(), item.getId(), dateTime);

        assertThat(bookings)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

        Booking found = bookings.get(0);

        assertThat(found.getId()).isEqualTo(2L);
        assertThat(found.getItem()).isEqualTo(item);
        assertThat(found.getBooker()).isEqualTo(user);
        assertThat(found.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }
}