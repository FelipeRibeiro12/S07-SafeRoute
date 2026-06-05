package com.saferoute.sensor_service.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.saferoute.sensor_service.client.AlertClient;
import com.saferoute.sensor_service.dto.TelemetryDTO;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceTest {

    @Mock
    private AlertClient alertClient;

    private TelemetryService telemetryService;

    @BeforeEach
    void setUp() {
        telemetryService = new TelemetryService();
        ReflectionTestUtils.setField(telemetryService, "alertClient", alertClient);
    }

    @Test
    void forwardsTelemetryToAlertService() {
        TelemetryDTO data = new TelemetryDTO("TRK-001", 5.5, -23.55, -46.63, "2026-06-05T10:00:00");

        telemetryService.processTelemetry(data);

        verify(alertClient).sendToAnalysis(data);
    }
}
