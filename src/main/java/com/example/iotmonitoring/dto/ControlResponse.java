package com.example.iotmonitoring.dto;

public class ControlResponse {
    private Long historyId;
    private String deviceId;
    private String action;
    private String status;

    public ControlResponse(Long historyId, String deviceId, String action, String status) {
        this.historyId = historyId;
        this.deviceId = deviceId;
        this.action = action;
        this.status = status;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getAction() {
        return action;
    }

    public String getStatus() {
        return status;
    }
}