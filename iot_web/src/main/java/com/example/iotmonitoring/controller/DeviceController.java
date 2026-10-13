package com.example.iotmonitoring.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.example.iotmonitoring.dto.ControlResponse;
import com.example.iotmonitoring.dto.DeviceControlRequest;
import com.example.iotmonitoring.dto.HistoryRow;
import com.example.iotmonitoring.entity.Device;
import com.example.iotmonitoring.entity.History;
import com.example.iotmonitoring.repository.DeviceRepository;
import com.example.iotmonitoring.repository.HistoryRepository;
import com.example.iotmonitoring.service.MqttService;
import com.example.iotmonitoring.util.RequestFilters;
import com.example.iotmonitoring.dto.HistoryResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.data.domain.Page;

import com.example.iotmonitoring.dto.PageResponse;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin
public class DeviceController {

        private final DeviceRepository devices;
        private final HistoryRepository histories;
        private final MqttService mqtt;

        public DeviceController(
                DeviceRepository devices,
                HistoryRepository histories,
                MqttService mqtt
        ) {
                this.devices = devices;
                this.histories = histories;
                this.mqtt = mqtt;
        }


        @PostMapping("/{deviceId}/control")
        public ControlResponse control(
                @PathVariable String deviceId,
                @Valid @RequestBody DeviceControlRequest request
        ) throws Exception {

                String action = request.getAction().toUpperCase();

                if (!Set.of("ON", "OFF").contains(action)) {
                throw new IllegalArgumentException(
                        "Action chỉ được là ON hoặc OFF"
                );
                }

                Device device = devices.findByDeviceId(deviceId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy thiết bị"
                                )
                        );

                History history = new History();
                history.setDevice(device);
                history.setAction(action);
                history.setStatus("PENDING");
                history.setCreatedAt(LocalDateTime.now());

                history = histories.save(history);
                
                try {
                        mqtt.publish(
                                deviceId,
                                action
                        );
                        
                        
                        long historyId = history.getId();
                        
                        CompletableFuture.delayedExecutor(
                                5,
                                TimeUnit.SECONDS
                        ).execute(() -> {
                        
                                histories.findById(historyId)
                                        .ifPresent(h -> {
                        
                                        if ("PENDING".equals(h.getStatus())) {
                                                h.setStatus("FAIL");
                                                histories.save(h);
                                        }
                        
                                        });
                        });

                } catch (Exception e) {
                        history.setStatus("FAIL");
                        histories.save(history);
                        throw e;
                }

                return new ControlResponse(
                        history.getId(),
                        deviceId,
                        action,
                        "PENDING"
                );
        }


        @GetMapping("/history")
        public PageResponse<HistoryRow> history(

                @RequestParam(required = false)
                String device,

                @RequestParam(required = false)
                String action,

                @RequestParam(required = false)
                String status,

                @RequestParam(required = false)
                String time,

                @RequestParam(defaultValue = "desc")
                String sort,

                @RequestParam(defaultValue = "0")
                int page,

                @RequestParam(defaultValue = "10")
                int size
        ) {

                var range = RequestFilters.timeRange(time);
                var pageable = RequestFilters.pageable(page, size, sort);

                Page<History> result =
                        histories.searchPage(
                                RequestFilters.blank(device),
                                RequestFilters.blank(action),
                                RequestFilters.blank(status),
                                range[0],
                                range[1],
                                pageable
                        );

                List<HistoryRow> content =
                        result.getContent()
                                .stream()
                                .map(h -> new HistoryRow(
                                        h.getId(),
                                        h.getDevice().getDeviceId(),
                                        h.getDevice().getDeviceName(),
                                        h.getAction(),
                                        h.getStatus(),
                                        h.getCreatedAt()
                                ))
                                .toList();

                return new PageResponse<>(
                        content,
                        result.getNumber(),
                        result.getSize(),
                        result.getTotalPages(),
                        result.getTotalElements()
                );
        }

    //lay status moi nhat cho frontend
    @GetMapping("/{deviceId}/latest")
        public HistoryResponse latest(
                @PathVariable String deviceId
        ) {

                History history = histories
                        .findFirstByDeviceDeviceIdOrderByCreatedAtDesc(deviceId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy lịch sử"
                                )
                        );

                return new HistoryResponse(
                        history.getId(),
                        deviceId,
                        history.getAction(),
                        history.getStatus()
                );
        }




    

}