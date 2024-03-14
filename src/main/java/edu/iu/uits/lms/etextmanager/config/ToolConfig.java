package edu.iu.uits.lms.etextmanager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "etextmanager")
@Getter
@Setter
public class ToolConfig {

   private String version;
   private String env;
   private String backgroundQueueName;
   private String groupCode;
   private String[] defaultEmails;
   private Map<String, String> toolSecrets = new HashMap<>();
}
