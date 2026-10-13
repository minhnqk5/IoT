package com.example.iotmonitoring.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.example.iotmonitoring.dto.Latest;
import com.example.iotmonitoring.dto.SensorRow;
import com.example.iotmonitoring.entity.DataSensor;
import com.example.iotmonitoring.entity.Sensor;
import com.example.iotmonitoring.repository.DataSensorRepository;
import com.example.iotmonitoring.repository.SensorRepository;

@RestController
@RequestMapping("/api/sensors")
@CrossOrigin
public class SensorController {

    private final SensorRepository sensors;
    private final DataSensorRepository data;

    public SensorController(
            SensorRepository sensors,
            DataSensorRepository data
    ) {
        this.sensors = sensors;
        this.data = data;
    }

    @GetMapping
    public List<Sensor> getAllSensors() {
        return sensors.findAll();
    }

    @GetMapping("/latest")
    public List<Latest> latest() {
        List<Latest> result = new ArrayList<>();

        for (Sensor sensor : sensors.findAll()) {
            List<DataSensor> rows =
                    data.findByTypeDesc(sensor.getSensorType());

            if (!rows.isEmpty()) {
                DataSensor latest = rows.get(0);

                result.add(
                        new Latest(
                                sensor.getSensorType(),
                                latest.getValue(),
                                latest.getUnit(),
                                latest.getCreatedAt()
                        )
                );
            }
        }

        return result;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        List<SensorRow> chart = new ArrayList<>();

        for (String type : List.of(
                "temperature",
                "humidity",
                "light"
        )) {
            chart.addAll(
                    data.findByTypeDesc(type)
                            .stream()
                            .limit(15)
                            .map(d -> new SensorRow(
                                    d.getId(),
                                    d.getSensor().getSensorId(),
                                    d.getSensor().getSensorType(),
                                    d.getValue(),
                                    d.getUnit(),
                                    d.getCreatedAt()
                            ))
                            .toList()
            );
        }

        return Map.of(
                "latest", latest(),
                "chart", chart
        );
    }
}
