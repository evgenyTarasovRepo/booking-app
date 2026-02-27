package com.booking.property.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:property-service}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                    .title("Property Service API")
                    .version("1.0.0").description("REST API for Property Service")
                    .contact(new Contact()
                        .name("Evgeny Tarasov")
                        .email("myemail@yahoo.com")
                        .url("https://github.com/evgenyTarasovRepo/booking-app")))
                .servers(List.of(new Server()
                        .url("http://localhost:9093")
                        .description("Local development server")));
    }
}
