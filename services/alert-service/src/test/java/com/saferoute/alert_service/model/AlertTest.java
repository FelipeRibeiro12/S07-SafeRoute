package com.saferoute.alert_service.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class AlertTest {

    @Test
    void constructorAndAccessorsExposeAlertFields() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 6, 5, 10, 0);
        Alert alert = new Alert(1L, "TRK-001", 9.0, -23.55, -46.63, timestamp);

        assertThat(alert.getId()).isEqualTo(1L);
        assertThat(alert.getTruckId()).isEqualTo("TRK-001");
        assertThat(alert.getTemperature()).isEqualTo(9.0);
        assertThat(alert.getLatitude()).isEqualTo(-23.55);
        assertThat(alert.getLongitude()).isEqualTo(-46.63);
        assertThat(alert.getTimestamp()).isEqualTo(timestamp);
        assertThat(Alert.getSerialversionuid()).isEqualTo(1L);
    }

    @Test
    void settersUpdateAlertFields() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 6, 5, 11, 0);
        Alert alert = new Alert();

        alert.setId(2L);
        alert.setTruckId("TRK-002");
        alert.setTemperature(1.0);
        alert.setLatitude(-22.9);
        alert.setLongitude(-43.2);
        alert.setTimestamp(timestamp);

        assertThat(alert).isEqualTo(new Alert(2L, "TRK-002", 1.0, -22.9, -43.2, timestamp));
        assertThat(alert.hashCode()).isEqualTo(new Alert(2L, "TRK-002", 1.0, -22.9, -43.2, timestamp).hashCode());
    }

    @Test
    void equalityHandlesDifferentObjectsAndNullFields() {
        Alert empty = new Alert();

        assertThat(empty).isEqualTo(empty);
        assertThat(empty).isEqualTo(new Alert());
        assertThat(empty).isNotEqualTo(null);
        assertThat(empty).isNotEqualTo("alert");
        assertThat(empty).isNotEqualTo(new Alert(1L, null, null, null, null, null));
        assertThat(new Alert(1L, "TRK-001", 9.0, -23.55, -46.63, LocalDateTime.MIN))
                .isNotEqualTo(new Alert(2L, "TRK-001", 9.0, -23.55, -46.63, LocalDateTime.MIN));
    }
}
