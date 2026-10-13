package com.example.iotmonitoring.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.iotmonitoring.dto.SensorRow;
import com.example.iotmonitoring.entity.DataSensor;
import com.example.iotmonitoring.repository.DataSensorRepository;
import com.example.iotmonitoring.util.RequestFilters;

import org.springframework.data.domain.Page;

import com.example.iotmonitoring.dto.PageResponse;

@RestController
@RequestMapping("/api/sensors/data")
@CrossOrigin
public class DataSensorController {

    private final DataSensorRepository repo;

    public DataSensorController(DataSensorRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public PageResponse<SensorRow> getSensorData(

            @RequestParam(required = false)
            String type,

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

        Page<DataSensor> result =
                repo.searchPage(
                        RequestFilters.blank(type),
                        range[0],
                        range[1],
                        pageable
                );

        List<SensorRow> content =
                result.getContent()
                        .stream()
                        .map(this::convert)
                        .toList();

        return new PageResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalPages(),
                result.getTotalElements()
        );
    }

    private SensorRow convert(DataSensor data) {
        return new SensorRow(
                data.getId(),
                data.getSensor().getSensorId(),
                data.getSensor().getSensorType(),
                data.getValue(),
                data.getUnit(),
                data.getCreatedAt()
        );
    }

}