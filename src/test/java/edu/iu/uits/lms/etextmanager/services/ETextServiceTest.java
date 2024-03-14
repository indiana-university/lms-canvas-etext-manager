package edu.iu.uits.lms.etextmanager.services;

import edu.iu.uits.lms.canvas.services.CanvasService;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.canvas.services.ExternalToolsService;
import edu.iu.uits.lms.canvas.services.ModuleService;
import edu.iu.uits.lms.email.model.EmailDetails;
import edu.iu.uits.lms.email.service.EmailService;
import edu.iu.uits.lms.etextmanager.config.BackgroundMessage;
import edu.iu.uits.lms.etextmanager.config.BackgroundMessageSender;
import edu.iu.uits.lms.etextmanager.config.PostgresDBConfig;
import edu.iu.uits.lms.etextmanager.config.ToolConfig;
import edu.iu.uits.lms.etextmanager.model.ETextCsv;
import edu.iu.uits.lms.etextmanager.repository.ETextResultsBatchRepository;
import edu.iu.uits.lms.etextmanager.repository.ETextToolConfigRepository;
import edu.iu.uits.lms.etextmanager.repository.ETextUserRepository;
import edu.iu.uits.lms.etextmanager.service.CsvUtil;
import edu.iu.uits.lms.etextmanager.service.ETextService;
import edu.iu.uits.lms.iuonly.services.BatchEmailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.verify;

@DataJpaTest
@Import({ToolConfig.class, PostgresDBConfig.class})
@Sql("/etext.sql")
@ActiveProfiles("etext")
@Slf4j
public class ETextServiceTest {

    @Autowired
    private ETextService eTextService;

    @Autowired
    private ETextUserRepository eTextUserRepository;

    @Autowired
    private ETextToolConfigRepository eTextToolConfigRepository;

    @Autowired
    private ETextResultsBatchRepository eTextResultsRepository;

    @MockBean
    private BackgroundMessageSender backgroundMessageSender;

    @MockBean
    private CourseService courseService;

    @MockBean
    private ExternalToolsService externalToolsService;

    @MockBean
    private CanvasService canvasService;

    @MockBean
    private ModuleService moduleService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private BatchEmailServiceImpl batchEmailService;

    @Autowired
    private FreeMarkerConfigurer freemarkerConfigurer;

    @Captor
    private ArgumentCaptor<EmailDetails> emailCaptor;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String USER1 = "user1";

//    @BeforeEach
//    void setUp() {
////        when(toolConfig.getDefaultEmails()).thenReturn(new String[] {"asdf@asdf.asdf"});
//    }

    @Test
    void testUploadNoConfig() throws Exception {
        Set<BackgroundMessage.FileGroup> fileGroups = new HashSet<>();
        String fileName = "normal.csv";
        InputStream fileStream = getUploadedFile(fileName);
        List<ETextCsv> parsedCsv = CsvUtil.parseCsv(fileStream, ETextCsv.class);
        BackgroundMessage.FileGroup fg = new BackgroundMessage.FileGroup(fileName, parsedCsv);
        fileGroups.add(fg);

        eTextService.processCsvData(USER1, fileGroups);
        verify(emailService).sendEmail(emailCaptor.capture());

        EmailDetails emailDetails = emailCaptor.getValue();
        Assertions.assertNotNull(emailDetails);

        Assertions.assertEquals(getEmailContent("no_config.txt"), emailDetails.getBody());
    }

    @Test
    @Disabled
    void testGoodFull() throws Exception {
        Set<BackgroundMessage.FileGroup> fileGroups = new HashSet<>();
        String fileName = "full.csv";
        InputStream fileStream = getUploadedFile(fileName);
        List<ETextCsv> parsedCsv = CsvUtil.parseCsv(fileStream, ETextCsv.class);
        BackgroundMessage.FileGroup fg = new BackgroundMessage.FileGroup(fileName, parsedCsv);
        fileGroups.add(fg);

//
//        when(eTextToolConfigRepository.findByToolName("root")).thenReturn(createRootToolConfig());
//        when(eTextToolConfigRepository.findByToolName("module")).thenReturn(createRootToolConfig());
//        when(eTextToolConfigRepository.findByToolName("course11")).thenReturn(createRootToolConfig());
//        when(eTextToolConfigRepository.findByToolName("course13")).thenReturn(createRootToolConfig());

        eTextService.processCsvData(USER1, fileGroups);
        verify(emailService).sendEmail(emailCaptor.capture());

        EmailDetails emailDetails = emailCaptor.getValue();
        Assertions.assertNotNull(emailDetails);

        Assertions.assertEquals(getEmailContent("good_full.txt"), emailDetails.getBody());
    }

//    private ETextToolConfig createRootToolConfig() throws Exception {
//        ETextToolConfig tool1 = new ETextToolConfig();
//        tool1.setToolName("Tool1");
//        tool1.setToolType(ETextToolConfig.TOOL_TYPE.ROOT_13_PLACEMENT);
//        tool1.setContextId("1234");
//        CanvasTab ct = new CanvasTab();
//        ct.setHidden(false);
//        ObjectMapper mapper = new ObjectMapper();
//        String json = mapper.writeValueAsString(ct);
//        tool1.setJsonBody(json);
//        return tool1;
//    }

    private InputStream getUploadedFile(String fileName) {
        InputStream fileStream = this.getClass().getResourceAsStream("/uploads/" + fileName);
        return fileStream;
    }

    private String getEmailContent(String fileName) throws IOException {
        InputStream fileStream = this.getClass().getResourceAsStream("/emails/" + fileName);
        String email = IOUtils.toString(fileStream, StandardCharsets.UTF_8);
        return email;
    }

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        public ETextService eTextService() {
            return new ETextService();
        }

        @Bean
        public FreeMarkerConfigurer freeMarkerConfigurer() {
            FreeMarkerConfigurer fmc = new FreeMarkerConfigurer();
            fmc.setTemplateLoaderPath("classpath:/templates");
            return fmc;
        }
    }

}
