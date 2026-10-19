package com.example.iotmonitoring.dto;

import java.time.LocalDateTime;

public class SensorRow {

    private Long id;
    private String sensorId;
    private String sensorType;
    private Double value;
    private String unit;
    private LocalDateTime createdAt;

    public SensorRow(Long id, String sensorId, String sensorType, Double value, String unit, LocalDateTime createdAt) {
        this.id = id;
        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.value = value;
        this.unit = unit;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getSensorType() {
        return sensorType;
    }

    public Double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}