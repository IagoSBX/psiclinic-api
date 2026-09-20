package com.psiclinic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI psiclinicOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PsiClinic API")
                        .description("API REST para gerenciamento de pacientes, psicólogos e sessões de uma clínica de psicologia")
                        .version("1.0.0")
                        .contact(new Contact().name("PsiClinic").email("contato@psiclinic.com")));
    }
}
