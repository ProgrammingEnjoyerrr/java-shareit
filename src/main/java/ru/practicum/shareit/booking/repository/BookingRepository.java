package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends PagingAndSortingRepository<Booking, Long> {
    @Query("select b " +
            "from Booking as b " +
            "where b.bookerId = ?1 " +
            "order by b.startDate desc")
    List<Booking> findAllBookingsForBookerByStatus(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndEndDateIsBefore(Long bookerId, LocalDateTime endDate, Sort sort);

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerId(Long bookerId);

    List<Booking> findByItemId(Long itemId, Sort sort);

    List<Booking> findAll();

    List<Booking> findByItemIdIn(Collection<Long> ids, Pageable pageable);
}
