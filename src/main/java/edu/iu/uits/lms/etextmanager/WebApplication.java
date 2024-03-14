package edu.iu.uits.lms.etextmanager;

import edu.iu.uits.lms.canvas.config.EnableCanvasClient;
import edu.iu.uits.lms.common.samesite.EnableCookieFilter;
import edu.iu.uits.lms.common.server.GitRepositoryState;
import edu.iu.uits.lms.common.server.ServerInfo;
import edu.iu.uits.lms.common.server.ServerUtils;
import edu.iu.uits.lms.email.config.EnableEmailClient;
import edu.iu.uits.lms.etextmanager.config.ToolConfig;
import edu.iu.uits.lms.iuonly.config.EnableIuOnlyClient;
import edu.iu.uits.lms.lti.config.EnableGlobalErrorHandler;
import edu.iu.uits.lms.lti.config.EnableLtiClient;
import edu.iu.uits.lms.redis.config.EnableRedisConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Date;

@SpringBootApplication
@EnableGlobalErrorHandler
@Slf4j
@EnableRedisConfiguration
@EnableCookieFilter(ignoredRequestPatterns = {"/rest/**"})
@EnableLtiClient(toolKeys = {"lms_etext_manager"})
@EnableCanvasClient
@EnableEmailClient
@EnableIuOnlyClient
@EnableConfigurationProperties(GitRepositoryState.class)
public class WebApplication {

    @Autowired
    private ToolConfig toolConfig;

    private final static int STEP_CAPACITY = 2048;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(WebApplication.class);
        BufferingApplicationStartup startup = new BufferingApplicationStartup(STEP_CAPACITY);
        startup.addFilter(startupStep -> startupStep.getName().matches("spring.boot.application.ready"));
        app.setApplicationStartup(startup);
        app.run(args);
    }

    @Autowired
    private GitRepositoryState gitRepositoryState;

    @Bean(name = ServerInfo.BEAN_NAME)
    ServerInfo serverInfo() {
        return ServerInfo.builder()
              .serverName(ServerUtils.getServerHostName())
              .environment(toolConfig.getEnv())
              .buildDate(new Date())
              .gitInfo(gitRepositoryState.getBranch() + "@" + gitRepositoryState.getCommitIdAbbrev())
              .artifactVersion(toolConfig.getVersion()).build();
    }

}
