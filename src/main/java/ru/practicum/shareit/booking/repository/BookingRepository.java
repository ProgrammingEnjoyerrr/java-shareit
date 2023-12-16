package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.util.Collection;
import java.util.List;

public interface BookingRepository extends PagingAndSortingRepository<Booking, Long> {
    @Query("select b " +
            "from Booking as b " +
            "where b.booker.id = ?1 " +
            "order by b.startDate desc")
    List<Booking> findAllBookingsForBookerByStatus(Long bookerId, Pageable pageable);

    List<Booking> findByBookerId(Long bookerId);

    List<Booking> findByItemId(Long itemId, Sort sort);

    List<Booking> findAll();

    List<Booking> findByItemIdIn(Collection<Long> ids, Pageable pageable);
}
