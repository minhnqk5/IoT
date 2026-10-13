package com.example.iotmonitoring.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HistoryRow {

    private Long id;
    private String deviceId;
    private String deviceName;
    private String action;
    private String status;
    private LocalDateTime createdAt;
}
