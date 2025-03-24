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

import edu.iu.uits.lms.common.cors.CorsSwaggerConfig;
import edu.iu.uits.lms.common.session.CourseSessionService;
import edu.iu.uits.lms.etextmanager.WebApplication;
import edu.iu.uits.lms.etextmanager.service.ETextService;
import edu.iu.uits.lms.lti.config.TestUtils;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.mail.MailHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ox.ctl.lti13.nrps.NamesRoleService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {WebApplication.class})
@AutoConfigureMockMvc
@ActiveProfiles({"it12"})
@EnableAutoConfiguration(exclude = {HealthContributorAutoConfiguration.class, HealthEndpointAutoConfiguration.class,
        MailHealthContributorAutoConfiguration.class})
@Slf4j
public class ETextItLoggingTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ETextService eTextService;

    @MockBean
    private CourseSessionService courseSessionService;

    @MockBean
    private DefaultInstructorRoleRepository defaultInstructorRoleRepository;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private BufferingApplicationStartup bufferingApplicationStartup;

    @MockBean
    private NamesRoleService namesRoleService;

    @MockBean
    private CorsSwaggerConfig corsSwaggerConfig;

    @Test
    public void testLmsEnhancementToIt12LogExistence() throws Exception {
        String auditLoggerClassName = "edu.iu.es.esi.audit.AuditLogger";

        Class<?> clazz = null;

        try {
            clazz = Class.forName(auditLoggerClassName);
        } catch (ClassNotFoundException classNotFoundException) {
            log.info("Skipping test because AuditLogger not found");
        }

        Assumptions.assumeTrue(clazz != null);

        try (LogCaptor logCaptor = LogCaptor.forClass(clazz)) {
            final Jwt jwt = createJwtToken("asdf");

            final Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_lms:rest", "ROLE_LMS_REST_ADMINS");
            final JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

            final String uriToCall = "/rest/etext_results_batch";

            mvc.perform(get(uriToCall)
                            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(authentication(token)))
                    .andExpect(status().is5xxServerError());

            final List<String> it12LogEntries = logCaptor.getInfoLogs();

            Assertions.assertNotNull(it12LogEntries);
            Assertions.assertEquals(1, it12LogEntries.size());

            final String it12LogEntry = it12LogEntries.getFirst();

            Assertions.assertNotNull(it12LogEntry);
            Assertions.assertFalse(it12LogEntry.isEmpty());

            Assertions.assertTrue(it12LogEntry.contains("\"type\":\"successful authorization\""));
            Assertions.assertTrue(it12LogEntry.contains("\"user\":\"asdf\""));
            Assertions.assertTrue(it12LogEntry.contains("\"ipAddress\":\"127.0.0.1\""));
            Assertions.assertTrue(it12LogEntry.contains("\"message\":\"Successful access to " + uriToCall + "\""));
        }
    }

    public static Jwt createJwtToken(String username) {
        Jwt jwt = Jwt.withTokenValue("fake-token")
                .header("typ", "JWT")
                .header("alg", SignatureAlgorithm.RS256.getValue())
                .claim("user_name", username)
                .claim("client_id", username)
                .notBefore(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .subject(username)
                .build();

        return jwt;
    }
}