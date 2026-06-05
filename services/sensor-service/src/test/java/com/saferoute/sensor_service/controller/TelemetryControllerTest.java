package com.saferoute.sensor_service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.saferoute.sensor_service.dto.TelemetryDTO;
import com.saferoute.sensor_service.service.TelemetryService;

@ExtendWith(MockitoExtension.class)
class TelemetryControllerTest {

    @Mock
    private TelemetryService telemetryService;

    @InjectMocks
    private TelemetryController telemetryController;

    @Test
    void processesReceivedTelemetry() {
        TelemetryDTO data = telemetry();

        ResponseEntity<String> response = telemetryController.receiveData(data);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Processado com sucesso.");
        verify(telemetryService).processTelemetry(data);
    }

    @Test
    void fallbackAcceptsTelemetryWhenAlertServiceFails() {
        ResponseEntity<String> response = telemetryController.fallbackAlert(telemetry(), new RuntimeException("down"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo("Dados recebidos, análise em espera (Modo de Segurança).");
    }

    private TelemetryDTO telemetry() {
        return new TelemetryDTO("TRK-001", 5.5, -23.55, -46.63, "2026-06-05T10:00:00");
    }
}
