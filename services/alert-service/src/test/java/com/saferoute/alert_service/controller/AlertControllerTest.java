package com.saferoute.alert_service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.saferoute.alert_service.dto.TelemetryDTO;
import com.saferoute.alert_service.service.AlertService;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

    @Mock
    private AlertService alertService;

    @InjectMocks
    private AlertController alertController;

    @Test
    void delegatesTelemetryCheckToService() {
        TelemetryDTO data = new TelemetryDTO("TRK-001", 9.0, -23.55, -46.63, "2026-06-05T10:00:00");

        ResponseEntity<Void> response = alertController.checkTelemetry(data);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(alertService).handleAlert(data);
    }
}
