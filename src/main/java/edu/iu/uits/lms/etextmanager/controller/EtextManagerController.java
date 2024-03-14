package edu.iu.uits.lms.etextmanager.controller;

import edu.iu.uits.lms.etextmanager.model.ETextResultsBatch;
import edu.iu.uits.lms.etextmanager.model.ETextToolConfig;
import edu.iu.uits.lms.etextmanager.service.ETextService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.OidcTokenAwareController;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/app")
@Slf4j
public class EtextManagerController extends OidcTokenAwareController {

    @Autowired
    private ETextService eTextService = null;

    @RequestMapping(value = "/launch")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public String launch(Model model, HttpSession httpSession) {
        OidcAuthenticationToken token = getTokenWithoutContext();

        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        String pathIdentifier = "index";

        model.addAttribute("hideFooter", true);
        model.addAttribute("toolPath", "/app/" + pathIdentifier);

        return "loading";
    }

    @RequestMapping(value = {"/index"})
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public ModelAndView index(Model model, HttpSession httpSession) {
        OidcAuthenticationToken token = getTokenWithoutContext();

        return new ModelAndView("index");
    }

    @PostMapping("/upload")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public ModelAndView upload(@RequestParam("csv-file-input") MultipartFile[] files,
                               Model model, HttpSession session) {
        log.debug("/upload");
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils tokenUtils = new OidcTokenUtils(token);
        String username = tokenUtils.getUserLoginId();

        try {
            eTextService.sendToQueue(username, files);
            model.addAttribute("uploadSuccess", true);

        } catch (IOException e) {
            model.addAttribute("fileErrors", true);
        }

        return index(model, session);
    }

    @GetMapping(value = "/toolConfigs")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public String loadToolConfigs(Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        List<ETextToolConfig> toolConfigs = eTextService.getToolConfigs();
        model.addAttribute("toolConfigs", toolConfigs);

        return "fragments/toolConfigs :: toolConfig";
    }

    @GetMapping(value = "/reports")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public String loadReports(Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        List<ETextResultsBatch> resultBatches = eTextService.getResultBatches();
        model.addAttribute("batches", resultBatches);

        return "fragments/reports :: reports";
    }

}
