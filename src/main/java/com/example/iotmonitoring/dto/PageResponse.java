package com.example.iotmonitoring.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        int totalPages,
        long totalElements
) {
}