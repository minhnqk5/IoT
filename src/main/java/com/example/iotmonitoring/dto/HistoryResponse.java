package com.example.iotmonitoring.dto;


public class HistoryResponse {

    private Long id;
    private String deviceId;
    private String action;
    private String status;

   public HistoryResponse(Long id, String deviceId, String action, String status) {
        this.id = id;
        this.deviceId = deviceId;
        this.action = action;
        this.status = status;
    }

    public Long getId() {
        return id;
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