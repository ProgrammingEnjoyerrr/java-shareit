package ru.practicum.shareit.request.dto;

import lombok.Getter;
import lombok.ToString;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@ToString
public class ItemWithRequestIdDto extends ItemDto {

    private final Long requestId;

    public ItemWithRequestIdDto(Long id, @NotBlank String name, @NotBlank String description, @NotNull Boolean available,
                                Long requestId) {
        super(id, name, description, available);
        this.requestId = requestId;
    }
}
