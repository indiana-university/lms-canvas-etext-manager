package edu.iu.uits.lms.microservicestemplate.controller;

import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.OidcTokenAwareController;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import edu.iu.uits.lms.microservicestemplate.config.ToolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

@Controller
@RequestMapping("/app")
@Slf4j
public class ToolController extends OidcTokenAwareController {

    @Autowired
    private ToolConfig toolConfig = null;

    @RequestMapping(value = "/launch")
    @Secured(LTIConstants.BASE_USER_AUTHORITY)
    public ModelAndView launch(Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();

        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);
        String courseId = oidcTokenUtils.getCourseId();

        return index(courseId, model);
    }

    @RequestMapping("/index/{courseId}")
    @Secured(LTIConstants.BASE_USER_AUTHORITY)
    public ModelAndView index(@PathVariable("courseId") String courseId, Model model) {
        log.debug("in /index");
        OidcAuthenticationToken token = getValidatedToken(courseId);
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);
        String userLoginId = oidcTokenUtils.getUserLoginId();

        model.addAttribute("userLoginId", userLoginId);

        return new ModelAndView("index");
    }
}
