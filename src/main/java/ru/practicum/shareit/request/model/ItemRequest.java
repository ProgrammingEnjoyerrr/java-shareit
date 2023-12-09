package ru.practicum.shareit.request.model;

import lombok.*;
import ru.practicum.shareit.item.model.Item;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "name", schema = "public")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime created;

    @Column
    @OneToMany
    @JoinColumn(name = "item_id")
    private List<Item> items;
}
