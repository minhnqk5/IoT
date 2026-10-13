package com.example.iotmonitoring.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.iotmonitoring.entity.History;

public interface HistoryRepository
        extends JpaRepository<History, Long> {

        Optional<History>
        findFirstByDeviceDeviceIdAndStatusOrderByCreatedAtDesc(
                String deviceId,
                String status
        );
                
        List<History> findAllByOrderByCreatedAtDesc();

        @Query(
                value = """
                SELECT h
                FROM History h
                JOIN FETCH h.device d
                WHERE (:device IS NULL
                        OR d.deviceId = :device)
                AND (:action IS NULL
                        OR h.action = :action)
                AND (:status IS NULL
                        OR h.status = :status)
                AND (:fromTime IS NULL
                        OR h.createdAt >= :fromTime)
                AND (:toTime IS NULL
                        OR h.createdAt < :toTime)
                """,
                countQuery = """
                SELECT COUNT(h)
                FROM History h
                WHERE (:device IS NULL
                        OR h.device.deviceId = :device)
                AND (:action IS NULL
                        OR h.action = :action)
                AND (:status IS NULL
                        OR h.status = :status)
                AND (:fromTime IS NULL
                        OR h.createdAt >= :fromTime)
                AND (:toTime IS NULL
                        OR h.createdAt < :toTime)
                """)
                Page<History> searchPage(
                        @Param("device") String device,
                        @Param("action") String action,
                        @Param("status") String status,
                        @Param("fromTime") LocalDateTime fromTime,
                        @Param("toTime") LocalDateTime toTime,
                        Pageable pageable
                );


        List<History> findAllByOrderByCreatedAtAsc();

        @Query(
                "select h from History h " +
                "join fetch h.device d " +
                "where (:deviceId is null or lower(d.deviceId) = lower(:deviceId)) " +
                "and (:action is null or lower(h.action) = lower(:action)) " +
                "and (:status is null or lower(h.status) = lower(:status)) " +
                "and (:fromTime is null or h.createdAt >= :fromTime) " +
                "and (:toTime is null or h.createdAt <= :toTime) " +
                "order by h.createdAt desc"
        )
        List<History> searchDesc(
                @Param("deviceId") String deviceId,
                @Param("action") String action,
                @Param("status") String status,
                @Param("fromTime") LocalDateTime fromTime,
                @Param("toTime") LocalDateTime toTime
        );

        @Query(
                "select h from History h " +
                "join fetch h.device d " +
                "where (:deviceId is null or lower(d.deviceId) = lower(:deviceId)) " +
                "and (:action is null or lower(h.action) = lower(:action)) " +
                "and (:status is null or lower(h.status) = lower(:status)) " +
                "and (:fromTime is null or h.createdAt >= :fromTime) " +
                "and (:toTime is null or h.createdAt <= :toTime) " +
                "order by h.createdAt asc"
        )
        List<History> searchAsc(
                @Param("deviceId") String deviceId,
                @Param("action") String action,
                @Param("status") String status,
                @Param("fromTime") LocalDateTime fromTime,
                @Param("toTime") LocalDateTime toTime
        );


        Optional<History> findFirstByDeviceDeviceIdOrderByCreatedAtDesc(String deviceId);
}