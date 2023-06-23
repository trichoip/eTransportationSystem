package com.etransportation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@SecurityScheme(name = "token_auth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@OpenAPIDefinition(
    info = @Info(title = "Swagger for System", description = "This is list of endpoints and documentations of REST API for System", version = "1.0"),
    // servers = {
    //     @Server(url = "https://etransportation-webapp-api.azurewebsites.net", description = "web server"),
    //     @Server(url = "http://localhost:8080", description = "Local development server"),
    //     @Server(url = "http://localhost:5000", description = "Local production server"),
    // },
    tags = {
        @Tag(name = "authentication", description = "REST API endpoints for authentication"),
        @Tag(name = "timekeeping", description = "REST API endpoints for timekeeping"),
    }
)
@EnableScheduling
public class EtransportationApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtransportationApplication.class, args);
    }
}
