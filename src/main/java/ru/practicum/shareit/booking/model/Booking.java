package ru.practicum.shareit.booking.model;

import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "bookings", schema = "public")
@ToString
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date")
    LocalDateTime startDate;

    @Column(name = "end_date")
    LocalDateTime endDate;

    @Column(name = "item_id")
    Long itemId;

    @Column(name = "booker_id")
    Long bookerId;

    @Column(name = "booking_status")
    BookingStatus status;
}
