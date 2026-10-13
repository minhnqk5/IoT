package com.example.iotmonitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ControlResponse {
    private Long historyId;
    private String deviceId;
    private String action;
    private String status;
}
