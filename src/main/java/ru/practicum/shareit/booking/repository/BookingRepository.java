package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b " +
            "from Booking as b " +
            "where b.bookerId = ?1 " +
            "order by b.startDate desc")
    List<Booking> findAllBookingsForBookerByStatus(Long bookerId);

    List<Booking> findByBookerIdAndEndDateIsBefore(Long bookerId, LocalDateTime endDate, Sort sort);

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerId(Long bookerId);

    List<Booking> findByItemId(Long itemId, Sort sort);
}
