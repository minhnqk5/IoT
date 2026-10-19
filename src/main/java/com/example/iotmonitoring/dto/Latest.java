package com.example.iotmonitoring.dto;

import java.time.LocalDateTime;

public class Latest {

    private String sensorType;
    private Double value;
    private String unit;
    private LocalDateTime createdAt;

    public Latest(String sensorType, Double value, String unit, LocalDateTime createdAt) {
        this.sensorType = sensorType;
        this.value = value;
        this.unit = unit;
        this.createdAt = createdAt;
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