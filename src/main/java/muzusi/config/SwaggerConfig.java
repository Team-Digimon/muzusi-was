package config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Muzusi",
                description = "Muzusi API 명세서.",
                version = "v1")
)
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi OpenApi() {
        String[] paths = {"/**"};

        return GroupedOpenApi.builder()
                .group("Muzusi API v1")
                .pathsToMatch(paths)
                .build();
    }
}