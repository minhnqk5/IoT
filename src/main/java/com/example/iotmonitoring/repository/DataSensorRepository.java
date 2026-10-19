package com.example.iotmonitoring.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.iotmonitoring.entity.DataSensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DataSensorRepository
        extends JpaRepository<DataSensor, Long> {

        List<DataSensor> findTop15ByOrderByCreatedAtDesc();

        @Query(
        value = """
        SELECT d
        FROM DataSensor d
        JOIN FETCH d.sensor s
        WHERE (:type IS NULL
                OR LOWER(s.sensorType)
                = LOWER(:type))
        AND (:sensorValue IS NULL
                OR d.value = :sensorValue)
        AND (:fromTime IS NULL
                OR d.createdAt >= :fromTime)
        AND (:toTime IS NULL
                OR d.createdAt < :toTime)
        """,
        countQuery = """
        SELECT COUNT(d)
        FROM DataSensor d
        WHERE (:type IS NULL
                OR LOWER(d.sensor.sensorType)
                = LOWER(:type))
        AND (:sensorValue IS NULL
                OR d.value = :sensorValue)
        AND (:fromTime IS NULL
                OR d.createdAt >= :fromTime)
        AND (:toTime IS NULL
                OR d.createdAt < :toTime)
        """)
        Page<DataSensor> searchPage(
                @Param("type") String type,
                @Param("sensorValue") Double sensorValue,
                @Param("fromTime") LocalDateTime fromTime,
                @Param("toTime") LocalDateTime toTime,
                Pageable pageable
        );


        @Query(
                "select d from DataSensor d " +
                "join fetch d.sensor s " +
                "where lower(s.sensorType) = lower(:type) " +
                "order by d.createdAt desc"
        )
        List<DataSensor> findByTypeDesc(
                @Param("type") String type
        );



        @Query(
                "select d from DataSensor d " +
                "join fetch d.sensor s " +
                "where (:type is null or lower(s.sensorType) = lower(:type)) " +
                "and (:fromTime is null or d.createdAt >= :fromTime) " +
                "and (:toTime is null or d.createdAt <= :toTime) " +
                "order by d.createdAt desc"
        )
        List<DataSensor> searchDesc(
                @Param("type") String type,
                @Param("fromTime") LocalDateTime fromTime,
                @Param("toTime") LocalDateTime toTime
        );

        

        @Query(
                "select d from DataSensor d " +
                "join fetch d.sensor s " +
                "where (:type is null or lower(s.sensorType) = lower(:type)) " +
                "and (:fromTime is null or d.createdAt >= :fromTime) " +
                "and (:toTime is null or d.createdAt <= :toTime) " +
                "order by d.createdAt asc"
        )
        List<DataSensor> searchAsc(
                @Param("type") String type,
                @Param("fromTime") LocalDateTime fromTime,
                @Param("toTime") LocalDateTime toTime
        );
}