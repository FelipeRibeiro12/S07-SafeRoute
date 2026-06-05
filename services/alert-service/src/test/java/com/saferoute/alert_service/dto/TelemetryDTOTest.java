package com.saferoute.alert_service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TelemetryDTOTest {

    @Test
    void constructorAndAccessorsExposeTelemetryFields() {
        TelemetryDTO dto = new TelemetryDTO("TRK-001", 5.5, -23.55, -46.63, "2026-06-05T10:00:00");

        assertThat(dto.getTruckId()).isEqualTo("TRK-001");
        assertThat(dto.getTemperature()).isEqualTo(5.5);
        assertThat(dto.getLatitude()).isEqualTo(-23.55);
        assertThat(dto.getLongitude()).isEqualTo(-46.63);
        assertThat(dto.getTimestamp()).isEqualTo("2026-06-05T10:00:00");
        assertThat(dto.isValid()).isTrue();
    }

    @Test
    void settersUpdateTelemetryFields() {
        TelemetryDTO dto = new TelemetryDTO();

        dto.setTruckId("TRK-002");
        dto.setTemperature(7.0);
        dto.setLatitude(-22.9);
        dto.setLongitude(-43.2);
        dto.setTimestamp("2026-06-05T11:00:00");

        assertThat(dto.getTruckId()).isEqualTo("TRK-002");
        assertThat(dto.getTemperature()).isEqualTo(7.0);
        assertThat(dto.getLatitude()).isEqualTo(-22.9);
        assertThat(dto.getLongitude()).isEqualTo(-43.2);
        assertThat(dto.getTimestamp()).isEqualTo("2026-06-05T11:00:00");
    }

    @Test
    void validRequiresTruckIdAndTemperature() {
        assertThat(new TelemetryDTO(null, 5.0, null, null, null).isValid()).isFalse();
        assertThat(new TelemetryDTO("", 5.0, null, null, null).isValid()).isFalse();
        assertThat(new TelemetryDTO("TRK-001", null, null, null, null).isValid()).isFalse();
    }
}
