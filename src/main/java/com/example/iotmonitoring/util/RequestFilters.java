package com.example.iotmonitoring.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Cac ham dung chung cho loc du lieu va phan trang.
 */
public final class RequestFilters {

    private RequestFilters() {
    }

    public static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * Doc so thuc tu o tim kiem (chap nhan ca dau phay: 27,5).
     * De trong thi tra ve null (khong loc theo gia tri).
     */
    public static Double number(String value) {
        String text = blank(value);

        if (text == null) {
            return null;
        }

        try {
            return Double.valueOf(text.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá trị tìm kiếm phải là số");
        }
    }

    public static Pageable pageable(int page, int size, String sort) {
        int safePage = Math.max(0, page);
        int safeSize = size < 1 ? 10 : size;
        Sort.Direction direction = "asc".equalsIgnoreCase(sort)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(safePage, safeSize,
                Sort.by(direction, "createdAt"));
    }

    /**
     * Ho tro dd/MM/yyyy, HH:mm dd/MM/yyyy va HH:mm:ss dd/MM/yyyy.
     * Tra ve moc bat dau va moc ket thuc (khong bao gom moc ket thuc).
     */
    public static LocalDateTime[] timeRange(String value) {
        if (value == null || value.isBlank()) {
            return new LocalDateTime[]{null, null};
        }

        String text = value.trim().replaceAll("\\s+", " ");

        try {
            LocalDate date = LocalDate.parse(
                    text, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            return new LocalDateTime[]{
                    date.atStartOfDay(), date.plusDays(1).atStartOfDay()
            };
        } catch (Exception ignored) {
            // Thu dinh dang co gio.
        }

        try {
            LocalDateTime start = LocalDateTime.parse(
                    text, DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
            return new LocalDateTime[]{start, start.plusMinutes(1)};
        } catch (Exception ignored) {
            // Thu dinh dang co giay.
        }

        try {
            LocalDateTime start = LocalDateTime.parse(
                    text, DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
            return new LocalDateTime[]{start, start.plusSeconds(1)};
        } catch (Exception ignored) {
            throw new IllegalArgumentException(
                    "Thời gian phải dạng dd/MM/yyyy, HH:mm dd/MM/yyyy hoặc HH:mm:ss dd/MM/yyyy");
        }
    }
}