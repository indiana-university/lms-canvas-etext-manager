package edu.iu.uits.lms.etextmanager.controller;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.iu.uits.lms.common.session.CourseSessionService;
import edu.iu.uits.lms.etextmanager.model.ConfigSettings;
import edu.iu.uits.lms.etextmanager.model.ETextResult;
import edu.iu.uits.lms.etextmanager.model.ETextToolConfig;
import edu.iu.uits.lms.etextmanager.service.ETextService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.OidcTokenAwareController;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Controller
@RequestMapping("/app")
@Slf4j
public class EtextManagerController extends OidcTokenAwareController {

    @Autowired
    private ETextService eTextService = null;

    @Autowired
    private ObjectMapper objectMapper = null;

    @Autowired
    private CourseSessionService courseSessionService = null;

    private static final String COURSE_ID = "COURSE-123";

    private static final String SHOW_ARCHIVED_KEY = "show-type";

    @RequestMapping(value = "/launch")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public String launch(Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();

        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        String pathIdentifier = "index";

        model.addAttribute("hideFooter", true);
        model.addAttribute("toolPath", "/app/" + pathIdentifier);

        return "loading";
    }

    @RequestMapping(value = {"/index"})
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public ModelAndView index(Model model, @RequestParam(name = "activeTab", defaultValue = "upload-panel", required = false) String activeTab) {
        OidcAuthenticationToken token = getTokenWithoutContext();

        // Set the active tab
        model.addAttribute("activeTab", activeTab);

        return new ModelAndView("index");
    }

    @PostMapping("/upload")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public ModelAndView upload(@RequestParam("csv-file-input") MultipartFile[] files, Model model) {
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

        return index(model, null);
    }

    @GetMapping(value = "/toolConfigs")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public String loadToolConfigs(Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        List<ETextToolConfig> toolConfigs = eTextService.getToolConfigs();
        model.addAttribute("toolConfigs", toolConfigs);
        model.addAttribute("toolTypes", ETextToolConfig.TOOL_TYPE.values());

        return "fragments/toolConfigs :: toolConfig";
    }

    @GetMapping(value = "/reports")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public String loadReports(Model model, HttpSession httpSession) {
        OidcAuthenticationToken token = getTokenWithoutContext();

        String showArchiveType = courseSessionService.getAttributeFromSession(httpSession, COURSE_ID, SHOW_ARCHIVED_KEY, String.class);
        if (showArchiveType == null) {
            showArchiveType = ETextService.ARCHIVED_SHOW_UNARCHIVED;
            courseSessionService.addAttributeToSession(httpSession, COURSE_ID, SHOW_ARCHIVED_KEY, showArchiveType);
        }

        List<ETextResult> results = eTextService.getResults(showArchiveType);
        model.addAttribute("results", results);
        model.addAttribute("showArchiveType", showArchiveType);

        return "fragments/reports :: reports";
    }

    @PostMapping("/reports/archive")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public ResponseEntity<PageReload> archive(@RequestParam("bulkArchive") List<Long> resultIds, Model model) {
        log.debug("in /reports/archive");
        OidcAuthenticationToken token = getTokenWithoutContext();

        // do the archivals
        try {
            eTextService.archiveResults(resultIds, true);
        } catch (Exception e) {
            log.error("Error archiving results", e);
            return ResponseEntity.badRequest().body(new PageReload(null, e.getMessage()));
        }

        return ResponseEntity.ok(new PageReload("/app/index?activeTab=report-panel", "success"));
    }

    @PostMapping("/reports/showarchived")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public ResponseEntity<PageReload> showArchived(@RequestParam("showArchivedAction") String showArchived, Model model, HttpSession httpSession) {
        log.debug("in /reports/showarchived");
        OidcAuthenticationToken token = getTokenWithoutContext();

        courseSessionService.addAttributeToSession(httpSession, COURSE_ID, SHOW_ARCHIVED_KEY, showArchived);

        return ResponseEntity.ok(new PageReload("/app/index?activeTab=report-panel", "success"));
    }

    @PostMapping(value = "/config/delete/{id}")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public ResponseEntity<PageReload> deleteConfig(@PathVariable Long id, Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();

        try {
            eTextService.deleteToolConfig(id);
        } catch (Exception e) {
            log.error("unable to delete config", e);
            return ResponseEntity.badRequest().body(new PageReload(null, e.getMessage()));
        }
        return ResponseEntity.ok(new PageReload("/app/index?activeTab=config-panel", "success"));
    }

    @PostMapping(value = "/config/edit/{id}")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public ResponseEntity<PageReload> editConfig(@PathVariable Long id, @ModelAttribute ETextToolConfig submittedToolConfig,
                             @RequestParam(name = "jsonBodyString") String jsonBodyString, Model model) {
        return addEdit(id, submittedToolConfig, jsonBodyString, model);
    }

    @PostMapping(value = "/config/add")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public ResponseEntity<PageReload> addConfig(@ModelAttribute ETextToolConfig submittedToolConfig,
                                  @RequestParam(name = "jsonBodyString") String jsonBodyString, Model model) {
        return addEdit(-1L, submittedToolConfig, jsonBodyString, model);
    }

    private ResponseEntity<PageReload> addEdit(Long id, ETextToolConfig submittedToolConfig, String jsonBodyString, Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        try {
            submittedToolConfig.setJsonBody(objectMapper.readValue(jsonBodyString, ConfigSettings.class));
            eTextService.addEditToolConfig(id, submittedToolConfig);
        } catch (JsonProcessingException e) {
            log.error("unable to save form", e);
            return ResponseEntity.badRequest().body(new PageReload(null, e.getMessage()));
        }
        return ResponseEntity.ok(new PageReload("/app/index?activeTab=config-panel", "success"));
    }

    @Data
    @AllArgsConstructor
    public static class PageReload implements Serializable {
        private String location;
        private String message;
    }

}
