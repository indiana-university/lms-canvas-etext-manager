package edu.iu.uits.lms.etextmanager.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("swagger")
@Configuration
@OpenAPIDefinition(info = @Info(title = "eText Manager REST Endpoints", version = "${etextmanager.version}"),
        security = @SecurityRequirement(name = "security_auth_etextmanager"))
@SecurityScheme(name = "security_auth_etextmanager", type = SecuritySchemeType.OAUTH2,
      flows = @OAuthFlows(authorizationCode = @OAuthFlow(
            authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}",
            scopes = {@OAuthScope(name = "lms:rest")},
            tokenUrl = "${springdoc.oAuthFlow.tokenUrl}")))
public class SwaggerConfig {
   @Bean
   public GroupedOpenApi groupedOpenApi() {
      return GroupedOpenApi.builder()
            .group("toolorganizer")
            .packagesToScan("edu.iu.uits.lms.etextmanager.controller.rest")
            .build();
   }
}
