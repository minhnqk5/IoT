package com.example.iotmonitoring.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.iotmonitoring.entity.Device;
import com.example.iotmonitoring.entity.Sensor;
import com.example.iotmonitoring.entity.User;
import com.example.iotmonitoring.repository.DeviceRepository;
import com.example.iotmonitoring.repository.SensorRepository;
import com.example.iotmonitoring.repository.UserRepository;

@Configuration
public class SeedConfig {

    @Bean
    CommandLineRunner init(
            UserRepository users,
            SensorRepository sensors,
            DeviceRepository devices,
            BCryptPasswordEncoder encoder
    ) {
        return args -> {
            if (users.findByUsername("admin").isEmpty()) {
                users.save(
                        new User(
                                null,
                                "admin",
                                encoder.encode("admin123"),
                                "ADMIN"
                        )
                );
            }

            if (sensors.findBySensorId("TEMP01").isEmpty()) {
                sensors.save(
                        new Sensor(null, "TEMP01", "temperature")
                );
            }

            if (sensors.findBySensorId("HUM01").isEmpty()) {
                sensors.save(
                        new Sensor(null, "HUM01", "humidity")
                );
            }

            if (sensors.findBySensorId("LIGHT01").isEmpty()) {
                sensors.save(
                        new Sensor(null, "LIGHT01", "light")
                );
            }

            if (devices.findByDeviceId("LED01").isEmpty()) {
                devices.save(
                        new Device(null, "LED01", "LED 1")
                );
            }

            if (devices.findByDeviceId("LED02").isEmpty()) {
                devices.save(
                        new Device(null, "LED02", "LED 2")
                );
            }
        };
    }
}
