package com.example.iotmonitoring.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SensorRow {

    private Long id;
    private String sensorId;
    private String sensorType;
    private Double value;
    private String unit;
    private LocalDateTime createdAt;
}
