package edu.iu.uits.lms.etextmanager.services;

/*-
 * #%L
 * etext-manager
 * %%
 * Copyright (C) 2024 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
import edu.iu.uits.lms.etextmanager.model.ETextResult;
import edu.iu.uits.lms.etextmanager.repository.ETextResultRepository;
import edu.iu.uits.lms.etextmanager.repository.ETextResultsBatchRepository;
import edu.iu.uits.lms.etextmanager.repository.ETextToolConfigRepository;
import edu.iu.uits.lms.etextmanager.service.CsvUtil;
import edu.iu.uits.lms.etextmanager.service.ETextService;
import edu.iu.uits.lms.iuonly.services.AuthorizedUserService;
import edu.iu.uits.lms.iuonly.services.BatchEmailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest
@ContextConfiguration(classes = {ToolConfig.class, PostgresDBConfig.class,
        ETextService.class, FreeMarkerConfigurer.class})
@Sql("/etext.sql")
@ActiveProfiles("etext")
@Slf4j
public class ETextServiceTest {

    @Autowired
    private ETextService eTextService;

    @MockBean
    private AuthorizedUserService authorizedUserService;

    @Autowired
    private ETextToolConfigRepository eTextToolConfigRepository;

    @Autowired
    private ETextResultsBatchRepository eTextResultsBatchRepository;

    @Autowired
    private ETextResultRepository eTextResultRepository;

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

    @BeforeEach
    void setUp() {
        when(emailService.getStandardHeader()).thenReturn("CI");
        freemarkerConfigurer.setTemplateLoaderPath("classpath:/templates");
    }

    @Test
    void testUploadNoConfig() throws Exception {
        Set<BackgroundMessage.FileGroup> fileGroups = new HashSet<>();
        fileGroups.add(buildFileGroup("normal.csv"));

        eTextService.processCsvData(USER1, fileGroups);
        verify(emailService).sendEmail(emailCaptor.capture());

        EmailDetails emailDetails = emailCaptor.getValue();
        Assertions.assertNotNull(emailDetails);

        Assertions.assertEquals(getEmailContent("no_config.txt"), emailDetails.getBody());
        Assertions.assertEquals("CI eText processing status for file(s) normal.csv", emailDetails.getSubject());
    }


    @Test
    void testUploadNoConfigMultipleFiles() throws Exception {
        Set<BackgroundMessage.FileGroup> fileGroups = new HashSet<>();
        fileGroups.add(buildFileGroup("normal.csv"));
        fileGroups.add(buildFileGroup("normal2.csv"));

        eTextService.processCsvData(USER1, fileGroups);
        verify(emailService).sendEmail(emailCaptor.capture());

        EmailDetails emailDetails = emailCaptor.getValue();
        Assertions.assertNotNull(emailDetails);

        Assertions.assertEquals(getEmailContent("no_config_multiple.txt"), emailDetails.getBody());
        Assertions.assertEquals("CI eText processing status for file(s) normal.csv, normal2.csv", emailDetails.getSubject());
    }

    private BackgroundMessage.FileGroup buildFileGroup(String fileName) throws IOException {
        InputStream fileStream = getUploadedFile(fileName);
        List<ETextCsv> parsedCsv = CsvUtil.parseCsv(fileStream, ETextCsv.class);
        BackgroundMessage.FileGroup fg = new BackgroundMessage.FileGroup(fileName, parsedCsv);
        return fg;
    }

    @Test
    void testStuff() {
//        List<ETextResult> failedActiveResults = eTextService.findFailedActiveResults();

//        List<ETextResult> intermediateResults = eTextResultRepository.findIntermediateResults();
//        log.debug("**********************");
//        log.debug("{}", intermediateResults);

        List<ETextResult> failedActiveResults = eTextResultRepository.findActiveResultsByStatus(ETextResult.STATUS.FAIL.name());
        log.debug("!!!!!!!!!!!!!!!!!!!!!!");
        log.debug("{}", failedActiveResults);
        Assertions.assertEquals(1, failedActiveResults.size());
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
        Assertions.assertEquals("asdf", emailDetails.getSubject());
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
