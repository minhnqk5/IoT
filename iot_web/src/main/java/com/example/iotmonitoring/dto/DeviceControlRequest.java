package com.example.iotmonitoring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceControlRequest {
    @NotBlank
    private String action;
}
