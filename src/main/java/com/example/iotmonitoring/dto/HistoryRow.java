package com.example.iotmonitoring.dto;

import java.time.LocalDateTime;

public class HistoryRow {

    private Long id;
    private String deviceId;
    private String deviceName;
    private String action;
    private String status;
    private LocalDateTime createdAt;

    public HistoryRow(Long id, String deviceId, String deviceName, String action, String status, LocalDateTime createdAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.action = action;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getAction() {
        return action;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}