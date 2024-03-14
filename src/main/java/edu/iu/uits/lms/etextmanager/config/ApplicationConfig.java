package edu.iu.uits.lms.etextmanager.config;

import edu.iu.uits.lms.etextmanager.model.ETextCsv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.HashSet;

@Configuration
@EnableWebMvc
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Slf4j
public class ApplicationConfig implements WebMvcConfigurer, RepositoryRestConfigurer {

   @Autowired
   private ToolConfig toolConfig;

   public ApplicationConfig() {
      log.debug("ApplicationConfig()");
   }

   @Override
   // used to read in various directories to add resources for the templates to use
   public void addResourceHandlers(ResourceHandlerRegistry registry) {
      registry.addResourceHandler("/app/css/**").addResourceLocations("classpath:/static/css/");
      registry.addResourceHandler("/app/js/**").addResourceLocations("classpath:/static/js/");
      registry.addResourceHandler("/app/images/**").addResourceLocations("classpath:/static/images/");
      registry.addResourceHandler("/app/webjars/**").addResourceLocations("/webjars/").resourceChain(true);
      registry.addResourceHandler("/app/jsreact/**").addResourceLocations("classpath:/META-INF/resources/jsreact/").resourceChain(true);
      registry.addResourceHandler("/app/jsrivet/**").addResourceLocations("classpath:/META-INF/resources/jsrivet/").resourceChain(true);
   }

   @Bean
   public ResourceBundleMessageSource messageSource() {
      ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
      messageSource.setBasename("bundles/etextmanager");
      return messageSource;
   }

   @Override
   public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
      RepositoryRestConfigurer.super.configureRepositoryRestConfiguration(config, cors);
      config.setRepositoryDetectionStrategy(RepositoryDetectionStrategy.RepositoryDetectionStrategies.ANNOTATED);
   }

   @Bean(name = "backgroundQueue")
   Queue backgroundQueue() {
      return new Queue(toolConfig.getBackgroundQueueName());
   }

   @Bean
   public SimpleMessageConverter converter() {
      SimpleMessageConverter converter = new SimpleMessageConverter();
      converter.addAllowedListPatterns(BackgroundMessage.class.getName(), ArrayList.class.getName(), HashSet.class.getName(),
              ETextCsv.class.getName(), BackgroundMessage.FileGroup.class.getName());
      return converter;
   }
}
