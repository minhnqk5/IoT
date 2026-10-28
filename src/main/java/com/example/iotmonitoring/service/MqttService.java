package com.example.iotmonitoring.service;

import java.time.LocalDateTime;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.iotmonitoring.entity.DataSensor;
import com.example.iotmonitoring.entity.History;
import com.example.iotmonitoring.entity.Sensor;
import com.example.iotmonitoring.repository.DataSensorRepository;
import com.example.iotmonitoring.repository.HistoryRepository;
import com.example.iotmonitoring.repository.SensorRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;


@Service
public class MqttService {

        @Value("${mqtt.broker}")
        private String broker;

        @Value("${mqtt.username}")
        private String username;

        @Value("${mqtt.password}")
        private String password;

        @Value("${mqtt.client-id}")
        private String clientId;

        @Value("${mqtt.sensor-topic}")
        private String sensorTopic;

        @Value("${mqtt.control-topic}")
        private String controlTopic;

        @Value("${mqtt.feedback-topic}")
        private String feedbackTopic;

        private final SensorRepository sensorRepo;
        private final DataSensorRepository dataSensorRepo;
        private final HistoryRepository historyRepo;
        private final ObjectMapper mapper;

        // MQTT client ở đây là chỉ chương trình backend nối tới broker mqtt
        private MqttClient client;

        public MqttService(
                SensorRepository sensorRepo,
                DataSensorRepository dataSensorRepo,
                HistoryRepository historyRepo,
                ObjectMapper mapper
        ) {
                this.sensorRepo = sensorRepo;
                this.dataSensorRepo = dataSensorRepo;
                this.historyRepo = historyRepo;
                this.mapper = mapper;
        }


        @PostConstruct
        public void connect() {
                try {
                client = new MqttClient(
                        broker,
                        clientId + "-" + System.currentTimeMillis()
                );

                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(username);
                options.setPassword(password.toCharArray());
                options.setAutomaticReconnect(true);
                options.setCleanSession(true);


                // Neu MQTT co su kien:
                client.setCallback(new MqttCallback() {

                        //Neu mat mang bao loi
                        @Override
                        public void connectionLost(Throwable throwable) {
                                System.err.println(
                                        "MQTT connection lost: " +
                                        throwable.getMessage()
                                );
                        }

                        //Neu Backend pub message thi ko xu ly gi
                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                        }

                        //Neu co message den thi xu ly
                        @Override
                        public void messageArrived(
                                String topic,
                                MqttMessage message
                        ) {
                                //ham handle o duoi
                                handle(
                                        topic,
                                        new String(message.getPayload())
                                );
                        }
                });

                client.connect(options);

                client.subscribe(sensorTopic);
                client.subscribe(feedbackTopic);

                System.out.println("MQTT connected: " + broker);

                } catch (Exception e) {
                        System.err.println(
                                "MQTT unavailable: " + e.getMessage()
                        );
                }
        }

        public synchronized void publish(
                String deviceId,
                String action
        ) throws Exception {

                if (client == null || !client.isConnected()) {
                throw new IllegalStateException(
                        "MQTT broker chưa kết nối"
                );
                }

                String payload = String.format(
                        "{\"deviceId\":\"%s\",\"action\":\"%s\"}",
                        deviceId,
                        action
                );

                client.publish(
                        controlTopic,
                        payload.getBytes(),
                        1,
                        false
                );
        }

        private void handle(
                String topic,
                String payload
        ) {
                try {
                JsonNode node = mapper.readTree(payload);

                if (topic.equals(sensorTopic)) {
                        save(
                                "temperature",
                                node.get("temperature"),
                                "°C"
                        );

                        save(
                                "humidity",
                                node.get("humidity"),
                                "%"
                        );

                        save(
                                "light",
                                node.get("light"),
                                "lux"
                        );


                } else if (topic.equals(feedbackTopic)) {

                        String deviceId = text(node, "deviceId");
                        String status = text(node, "status");

                        if (deviceId == null || status == null) {
                                return;
                        }

                        History history = historyRepo
                                .findFirstByDeviceDeviceIdAndStatusOrderByCreatedAtDesc(
                                        deviceId,
                                        "PENDING"
                                )
                                .orElse(null);

                        if (history == null) {
                                return;
                        }

                        history.setStatus(
                                "SUCCESS".equalsIgnoreCase(status)
                                        ? "SUCCESS"
                                        : "FAIL"
                        );

                        historyRepo.save(history);
                        }


                } catch (Exception e) {
                System.err.println(
                        "Bad MQTT message: " + e.getMessage()
                );
                }
        }



        private void save(
                String type,
                JsonNode value,
                String unit
        ) {
                if (value == null || !value.isNumber()) {
                return;
                }

                //Thêm loại cảm biến nếu chưa có
                Sensor sensor = sensorRepo
                        .findBySensorTypeIgnoreCase(type)
                        .orElseGet(() ->
                                sensorRepo.save(
                                        new Sensor(
                                                null,
                                                type.toUpperCase(),
                                                type
                                        )
                                )
                        );

                //lưu datasensor 
                dataSensorRepo.save(
                        new DataSensor(
                                null,
                                sensor,
                                value.asDouble(),
                                unit,
                                LocalDateTime.now()
                        )
                );
        }



        private String text(
                JsonNode node,
                String key
        ) {
                return node.has(key)
                        ? node.get(key).asText()
                        : null;
        }

        @PreDestroy
        public void close() {
                try {
                if (client != null && client.isConnected()) {
                        client.disconnect();
                }
                } catch (Exception ignored) {
                }
        }

}
