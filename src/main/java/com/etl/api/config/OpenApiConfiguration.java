package com.etl.api.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * swagger 配置类
 */
@Configuration
public class OpenApiConfiguration {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    static {
        SpringDocUtils.getConfig()
                .replaceWithSchema(
                        LocalDateTime.class,
                        new StringSchema()
                                .example(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                                .pattern("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$")
                )
                .replaceWithSchema(
                        LocalDate.class,
                        new StringSchema()
                                .example(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                                .pattern("^\\d{4}-\\d{2}-\\d{2}$")
                ).replaceWithSchema(
                        LocalTime.class,
                        new StringSchema()
                                .example(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                                .pattern("^\\d{2}:\\d{2}:\\d{2}$")
                );
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, createAPIKeyScheme()))
                .info(new Info().title("Flink-ETL-API")
                        .description("基于 Flink 的 ETL 平台后端服务")
                        .version("0.0.1")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")
                        )
                );
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer");
    }
}
