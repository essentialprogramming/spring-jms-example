package com.spring.jms.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration of swagger.
 * Swagger config will be used both for exporting Swagger UI and for OpenAPI specification generation.
 */
@Configuration
public class SwaggerConfig {

    /**
     * JMS API
     */
    @Bean
    public GroupedOpenApi jmsApi() {
        final String[] packagesToScan = {"com.spring.jms.controller"};
        return GroupedOpenApi
                .builder()
                .group("JMS API")
                .packagesToScan(packagesToScan)
                .pathsToMatch("/jms/**")
                .addOpenApiCustomiser(jmsApiCustomizer())
                .build();
    }

    private OpenApiCustomiser jmsApiCustomizer() {
        return openAPI -> openAPI
                .info(new Info()
                        .title("JMS API")
                        .description("This is a sample JMS service using OpenAPI")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Essential Programming")
                                .email("razvan.prichici@gmail.com")));


    }
}