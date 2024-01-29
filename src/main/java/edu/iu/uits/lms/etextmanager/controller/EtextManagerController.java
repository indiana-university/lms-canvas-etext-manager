package edu.iu.uits.lms.etextmanager.controller;

import edu.iu.uits.lms.common.session.CourseSessionService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.OidcTokenAwareController;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/app")
@Slf4j
public class EtextManagerController extends OidcTokenAwareController {

    @Autowired
    private ResourceBundleMessageSource messageSource = null;

    @Autowired
    private CourseSessionService courseSessionService = null;

    @RequestMapping(value = "/launch")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public String launch(Model model, HttpSession httpSession) {
        OidcAuthenticationToken token = getTokenWithoutContext();

        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);
        String courseId = oidcTokenUtils.getCourseId();

        String pathIdentifier = "index";

//        courseSessionService.addAttributeToSession(httpSession, courseId, ReportConstants.VARIABLE_REPLACEMENT_DATA_KEY, setupMacroVariableReplacement(token));
        model.addAttribute("hideFooter", true);
        model.addAttribute("toolPath", "/app/" + pathIdentifier);

        return "loading";
    }

    @RequestMapping(value = {"/index"})
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public String index(Model model, HttpSession httpSession, SecurityContextHolderAwareRequestWrapper request) {
        OidcAuthenticationToken token = getTokenWithoutContext();

        //For session tracking
        model.addAttribute("customId", httpSession.getId());

        return "index";
    }

}
