package edu.iu.uits.lms.microservicestemplate.services;

import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.config.TestUtils;
import edu.iu.uits.lms.microservicestemplate.config.ToolConfig;
import edu.iu.uits.lms.microservicestemplate.controller.ToolController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ToolController.class, properties = {"oauth.tokenprovider.url=http://foo"})
@Import(ToolConfig.class)
@ActiveProfiles("none")
public class AppLaunchSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void appNoAuthnLaunch() throws Exception {
        //This is a secured endpoint and should not allow access without authn
        mvc.perform(get("/app/index/1234")
                        .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void appAuthnWrongContextLaunch() throws Exception {
        OidcAuthenticationToken token = TestUtils.buildToken("userId", "asdf", LTIConstants.BASE_USER_ROLE);

        SecurityContextHolder.getContext().setAuthentication(token);

        //This is a secured endpoint and should not allow access without authn
        ResultActions mockMvcAction = mvc.perform(get("/app/index/1234")
                .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                .contentType(MediaType.APPLICATION_JSON));

        mockMvcAction.andExpect(status().isInternalServerError());
        mockMvcAction.andExpect(MockMvcResultMatchers.view().name("error"));
        mockMvcAction.andExpect(MockMvcResultMatchers.model().attributeExists("error"));
    }

    @Test
    public void appAuthnLaunch() throws Exception {
        OidcAuthenticationToken token = TestUtils.buildToken("userId", "1234", LTIConstants.BASE_USER_ROLE);

        SecurityContextHolder.getContext().setAuthentication(token);

        //This is a secured endpoint and should not allow access without authn
        mvc.perform(get("/app/index/1234")
                        .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void randomUrlNoAuth() throws Exception {
        //This is a secured endpoint and should not allow access without authn
        mvc.perform(get("/asdf/foobar")
                        .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void randomUrlWithAuth() throws Exception {
        OidcAuthenticationToken token = TestUtils.buildToken("userId", "1234", LTIConstants.BASE_USER_ROLE);
        SecurityContextHolder.getContext().setAuthentication(token);

        //This is a secured endpoint and should not allow access without authn
        mvc.perform(get("/asdf/foobar")
                        .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
