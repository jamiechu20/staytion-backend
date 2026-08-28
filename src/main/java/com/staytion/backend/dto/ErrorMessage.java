package com.staytion.backend.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class ErrorMessage {
    private final String message;
}
