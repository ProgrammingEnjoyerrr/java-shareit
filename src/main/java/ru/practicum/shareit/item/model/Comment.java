package ru.practicum.shareit.item.model;

import lombok.*;

import javax.persistence.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "comments", schema = "public")
@ToString
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;
}

