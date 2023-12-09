package ru.practicum.shareit.request.controller;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.common.BaseErrorHandler;

@RestControllerAdvice(value = "ru.practicum.shareit.request.controller")
public class ItemRequestErrorHandler extends BaseErrorHandler {

}
