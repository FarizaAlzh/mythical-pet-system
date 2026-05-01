package com.example.petadoptionservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mythicalPetOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mythical Pet Adoption System API")
                        .version("1.0.0")
                        .description("OpenAPI documentation for the Mythical Pet Adoption System backend."));
    }
}
