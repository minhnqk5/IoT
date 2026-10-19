package com.example.iotmonitoring.dto;

import jakarta.validation.constraints.NotBlank;

public class DeviceControlRequest {
    @NotBlank
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}