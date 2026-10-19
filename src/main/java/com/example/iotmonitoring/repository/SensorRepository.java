package com.example.iotmonitoring.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.iotmonitoring.entity.Sensor;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findBySensorId(String sensorId);
    Optional<Sensor> findBySensorTypeIgnoreCase(String sensorType);
}
