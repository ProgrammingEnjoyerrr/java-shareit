package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
//    @Query(" select i from Item i " +
//            "where upper(i.name) like upper(concat('%', ?1, '%')) " +
//            " or upper(i.description) like upper(concat('%', ?1, '%'))")
//    List<Booking> search(String text);

    @Query("select b " +
            "from Booking as b " +
            "where b.bookerId = ?1 " +
            "order by b.startDate desc")
    List<Booking> findAllBookingsForBookerByStatus(Long bookerId);

//    @Query("select i " +
//            "from Item as i " +
//            "where i.ownerId = ?1 " +
//            ";" +
//            "select b " +
//            "from Booking as b " +
//            "where b.itemId = ?1 " +
//            "order by b.startDate desc")
//    List<Booking> findAllBookingsForItemsOwner(Long ownerId);
}
