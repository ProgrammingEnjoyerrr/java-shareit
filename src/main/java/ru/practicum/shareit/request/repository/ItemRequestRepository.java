package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collection;
import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    Collection<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(long userId);

    Collection<ItemRequest> findAllByRequesterId(long userId);

    List<ItemRequest> findAllByRequester_IdNotOrderByCreatedDesc(long userId, Pageable pageable);
}
