package com.saferoute.alert_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.saferoute.alert_service.dto.TelemetryDTO;
import com.saferoute.alert_service.model.Alert;
import com.saferoute.alert_service.repository.AlertRepository;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService();
        ReflectionTestUtils.setField(alertService, "tempMin", -2.0);
        ReflectionTestUtils.setField(alertService, "tempMax", 8.0);
        ReflectionTestUtils.setField(alertService, "alertRepository", alertRepository);
    }

    @Test
    void identifiesTemperatureBelowMinimumAsAlert() {
        assertThat(alertService.isTemperatureAlert(telemetry(-2.1))).isTrue();
    }

    @Test
    void identifiesTemperatureAboveMaximumAsAlert() {
        assertThat(alertService.isTemperatureAlert(telemetry(8.1))).isTrue();
    }

    @Test
    void acceptsTemperatureInsideRangeAndOnBoundaries() {
        assertThat(alertService.isTemperatureAlert(telemetry(5.0))).isFalse();
        assertThat(alertService.isTemperatureAlert(telemetry(-2.0))).isFalse();
        assertThat(alertService.isTemperatureAlert(telemetry(8.0))).isFalse();
    }

    @Test
    void ignoresNullTelemetryOrNullTemperature() {
        assertThat(alertService.isTemperatureAlert(null)).isFalse();
        assertThat(alertService.isTemperatureAlert(telemetry(null))).isFalse();

        alertService.handleAlert(null);
        alertService.handleAlert(telemetry(null));

        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void savesAlertWhenTemperatureIsBelowMinimum() {
        TelemetryDTO data = telemetry(-3.0);

        alertService.handleAlert(data);

        Alert saved = captureSavedAlert();
        assertThat(saved.getTruckId()).isEqualTo("TRK-001");
        assertThat(saved.getTemperature()).isEqualTo(-3.0);
        assertThat(saved.getLatitude()).isEqualTo(-23.55);
        assertThat(saved.getLongitude()).isEqualTo(-46.63);
        assertThat(saved.getTimestamp()).isNotNull();
    }

    @Test
    void savesAlertWhenTemperatureIsAboveMaximum() {
        TelemetryDTO data = telemetry(9.5);

        alertService.handleAlert(data);

        Alert saved = captureSavedAlert();
        assertThat(saved.getTruckId()).isEqualTo("TRK-001");
        assertThat(saved.getTemperature()).isEqualTo(9.5);
    }

    @Test
    void doesNotSaveAlertWhenTemperatureIsInsideRange() {
        alertService.handleAlert(telemetry(6.0));

        verify(alertRepository, never()).save(any(Alert.class));
    }

    private Alert captureSavedAlert() {
        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(captor.capture());
        return captor.getValue();
    }

    private TelemetryDTO telemetry(Double temperature) {
        return new TelemetryDTO("TRK-001", temperature, -23.55, -46.63, "2026-06-05T10:00:00");
    }
}
