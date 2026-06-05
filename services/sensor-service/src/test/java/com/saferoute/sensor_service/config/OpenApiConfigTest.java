package com.saferoute.sensor_service.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;

class OpenApiConfigTest {

    @Test
    void customOpenApiContainsExpectedMetadata() {
        OpenAPI openAPI = new OpenApiConfig().customOpenAPI();

        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Book Microservice API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0");
        assertThat(openAPI.getInfo().getLicense().getName()).isEmpty();
        assertThat(openAPI.getInfo().getLicense().getUrl()).isEmpty();
    }
}
