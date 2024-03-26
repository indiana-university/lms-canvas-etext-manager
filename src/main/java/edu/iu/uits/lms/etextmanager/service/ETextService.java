package edu.iu.uits.lms.etextmanager.service;

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

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.model.ExternalTool;
import edu.iu.uits.lms.canvas.model.LtiSettings;
import edu.iu.uits.lms.canvas.model.Module;
import edu.iu.uits.lms.canvas.model.ModuleCreateWrapper;
import edu.iu.uits.lms.canvas.model.ModuleItem;
import edu.iu.uits.lms.canvas.model.ModuleItemCreateWrapper;
import edu.iu.uits.lms.canvas.services.CanvasService;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.canvas.services.ExternalToolsService;
import edu.iu.uits.lms.canvas.services.ModuleService;
import edu.iu.uits.lms.email.model.EmailDetails;
import edu.iu.uits.lms.email.model.EmailServiceAttachment;
import edu.iu.uits.lms.email.service.EmailService;
import edu.iu.uits.lms.email.service.LmsEmailTooBigException;
import edu.iu.uits.lms.etextmanager.config.BackgroundMessage;
import edu.iu.uits.lms.etextmanager.config.BackgroundMessageSender;
import edu.iu.uits.lms.etextmanager.config.ToolConfig;
import edu.iu.uits.lms.etextmanager.model.CanvasTab;
import edu.iu.uits.lms.etextmanager.model.ConfigSettings;
import edu.iu.uits.lms.etextmanager.model.ETextCsv;
import edu.iu.uits.lms.etextmanager.model.ETextResult;
import edu.iu.uits.lms.etextmanager.model.ETextResultsBatch;
import edu.iu.uits.lms.etextmanager.model.ETextToolConfig;
import edu.iu.uits.lms.etextmanager.model.ETextUser;
import edu.iu.uits.lms.etextmanager.repository.ETextResultsBatchRepository;
import edu.iu.uits.lms.etextmanager.repository.ETextToolConfigRepository;
import edu.iu.uits.lms.etextmanager.repository.ETextUserRepository;
import edu.iu.uits.lms.iuonly.model.LmsBatchEmail;
import edu.iu.uits.lms.iuonly.services.BatchEmailServiceImpl;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ETextService {

    @Autowired
    private ETextUserRepository eTextUserRepository;

    @Autowired
    private ETextToolConfigRepository eTextToolConfigRepository;

    @Autowired
    private ETextResultsBatchRepository eTextResultsBatchRepository;

    @Autowired
    private BackgroundMessageSender backgroundMessageSender;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ExternalToolsService externalToolsService;

    @Autowired
    private CanvasService canvasService;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BatchEmailServiceImpl batchEmailService;

    @Autowired
    private ToolConfig toolConfig;

    @Autowired
    private FreeMarkerConfigurer freemarkerConfigurer;

    private static final DefaultPrettyPrinter PRETTY_PRINTER = new DefaultPrettyPrinter();

    static {
        // Setup a json "pretty printer" with an indenter (indenter has 4 spaces in this case)
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        PRETTY_PRINTER.indentObjectsWith(indenter);
        PRETTY_PRINTER.indentArraysWith(indenter);
    }

    /**
     * Lookup a user from the authorized user table
     * @param username Username of the user attempting to use the tool
     * @return Found ETextUser, or null
     */
    public ETextUser findByUsername(String username) {
        return eTextUserRepository.findByUsername(username);
    }

    /**
     * Send details to the background rabbit queue
     * @param username Username of the uploader
     * @param files List of uploaded files
     * @throws IOException IOException if the uploaded files can't be processed
     */
    public void sendToQueue(String username, MultipartFile[] files) throws IOException {
        BackgroundMessage message = new BackgroundMessage(username);
        Set<BackgroundMessage.FileGroup> fileGroups = new HashSet<>();

        for (MultipartFile file : files) {
            //Process file upload
            List<ETextCsv> parsedCsv = CsvUtil.parseCsv(file.getInputStream(), ETextCsv.class);
            fileGroups.add(new BackgroundMessage.FileGroup(file.getOriginalFilename(), parsedCsv));
        }
        message.setFileGroup(fileGroups);

        backgroundMessageSender.send(message);
    }

    /**
     * Process the uploaded csv data
     * @param username Username of the uploader
     * @param fileGroups Grouped data, ready to be processed
     */
    public void processCsvData(String username, Set<BackgroundMessage.FileGroup> fileGroups) {
        List<ETextResult> allResults = new ArrayList<>();
        Map<String, String> courseMap = new HashMap<>();

        // Create the batch entry
        ETextResultsBatch resultsBatch = new ETextResultsBatch(username, new Date());

        // Iterate through the files
        for (BackgroundMessage.FileGroup fileGroup: fileGroups){
            String filename = fileGroup.getFileName();
            List<ETextCsv> data = fileGroup.getFileContent();
            log.debug("Got data for {} via {}", username, filename);
            log.debug("{}", data);

            // Group the data by the tool
            Map<String, List<ETextCsv>> groupedTools = data.stream().collect(Collectors.groupingBy(ETextCsv::getTool));
            TreeMap<String, List<ETextCsv>> sorted = new TreeMap<>(groupedTools);

            for (Map.Entry<String, List<ETextCsv>> entry : sorted.entrySet()) {
                try {
                    ETextToolConfig eTextToolConfig = eTextToolConfigRepository.findByToolName(entry.getKey());
                    switch (eTextToolConfig.getToolType()) {
                        case COURSE_13_PLACEMENT -> {
                            allResults.addAll(handle13CoursePlacement(eTextToolConfig, entry.getValue(), filename, courseMap));
                        }
                        case ROOT_13_PLACEMENT -> {
                            allResults.addAll(handleRootPlacement(eTextToolConfig, entry.getValue(), filename, courseMap));
                        }
                        case MODULE_PLACEMENT -> {
                            allResults.addAll(handleModulePlacement(eTextToolConfig, entry.getValue(), filename, courseMap));
                        }
                        case COURSE_11_PLACEMENT -> {
                            allResults.addAll(handle11CoursePlacement(eTextToolConfig, entry.getValue(), filename, courseMap));
                        }
                        case COURSE_11_AND_MODULE_PLACEMENT -> {
                            allResults.addAll(handle11CoursePlacement(eTextToolConfig, entry.getValue(), filename, courseMap));
                            allResults.addAll(handleModulePlacement(eTextToolConfig, entry.getValue(), filename, courseMap));
                        }
                        default -> {
                            throw new IllegalStateException("Unexpected value: " + eTextToolConfig.getToolType());
                        }
                    }
                } catch (Exception e) {
                    String message = "Unable to process data for " + entry.getKey();
                    ETextResult result = new ETextResult("", filename);
                    result.setMessage(message);
                    allResults.add(result);
                    log.error(message, e);
                }
            }
        }
        resultsBatch.setResults(allResults);
        eTextResultsBatchRepository.save(resultsBatch);
        sendEmail(resultsBatch);
    }


    /**
     * Handle LTI 1.3 course placement
     * @param eTextToolConfig Config for this
     * @param data Data to process
     * @param filename Original uploaded filename
     * @param courseMap Course "cache"
     * @return List of results
     */
    private List<ETextResult> handle13CoursePlacement(ETextToolConfig eTextToolConfig, List<ETextCsv> data,
                                                      String filename, Map<String, String> courseMap) {
        ConfigSettings configSettings = eTextToolConfig.getJsonBody();
        List<ETextResult> results = new ArrayList<>();

        // Copy the config to the object we'll actually be working with
        LtiSettings ltiSettings = SerializationUtils.clone(configSettings.getLtiSettings());

        for (ETextCsv row : data) {
            List<String> resultMessages = new ArrayList<>();
            ETextResult result = new ETextResult(eTextToolConfig.getToolName(), filename);
            result.setSisCourseId(row.getSisCourseId());

            try {
                checkRequiredFields(new RequiredField("csv: sis course id", row.getSisCourseId()),
                        new RequiredField("csv: new tool name", row.getNewName()),
                        new RequiredField("config: context id", eTextToolConfig.getContextId()));

                ltiSettings.setName(row.getNewName());
                ExternalTool externalTool = null;
                try {
                    String courseId = courseIdLookup(row.getSisCourseId(), courseMap);
                    if (courseId != null) {
                        result.setCanvasCourseId(courseId);
                    }
                    externalTool = externalToolsService.getExternalToolByName("sis_course_id:" + row.getSisCourseId(), row.getNewName(), "course_navigation");
                    if (externalTool == null) {
                        externalTool = externalToolsService.createExternalToolForCourse("sis_course_id:" + row.getSisCourseId(), eTextToolConfig.getContextId());
                    } else {
                        resultMessages.add("Using existing course nav placement");
                    }
                    result.setToolId(externalTool.getId());
                } catch (Exception e) {
                    log.error("Error in step 1 of handleCoursePlacement", e);
                    resultMessages.add("Error in step 1: " + e.getMessage());
                }
                if (externalTool != null) {
                    try {
                        ExternalTool externalTool2 = externalToolsService.updateExternalToolForCourse(canvasService.getBaseUrl(), "sis_course_id:" + row.getSisCourseId(), externalTool.getId(), ltiSettings);
                        result.setDeploymentId(externalTool2.getDeploymentId());
                        resultMessages.add("Success");
                    } catch (Exception e) {
                        log.error("Error in step 2 of handleCoursePlacement", e);
                        resultMessages.add("Error in step 2: " + e.getMessage());
                    }
                }
            } catch (MissingFieldException mfe) {
                resultMessages.add("Missing required field(s): " + StringUtils.join(mfe.getMissingFields(), ", "));
            }
            result.setMessage(StringUtils.join(resultMessages, "; "));
            results.add(result);

        }
//        log.debug("{}", results);
        return results;
    }

    /**
     * Handle LTI 1.1 course placement
     * @param eTextToolConfig Config for this
     * @param data Data to process
     * @param filename Original uploaded filename
     * @param courseMap Course "cache"
     * @return List of results
     */
    private List<ETextResult> handle11CoursePlacement(ETextToolConfig eTextToolConfig, List<ETextCsv> data,
                                                      String filename, Map<String, String> courseMap) {
        ConfigSettings configSettings = eTextToolConfig.getJsonBody();
        List<ETextResult> results = new ArrayList<>();

        LtiSettings ltiSettings = SerializationUtils.clone(configSettings.getLtiSettings());

        for (ETextCsv row : data) {
            List<String> resultMessages = new ArrayList<>();
            ETextResult result = new ETextResult(eTextToolConfig.getToolName(), filename);
            result.setSisCourseId(row.getSisCourseId());

            try {
                checkRequiredFields(new RequiredField("csv: sis course id", row.getSisCourseId()),
                        new RequiredField("csv: new tool name", row.getNewName()),
                        new RequiredField("config: lti consumer key", ltiSettings.getConsumerKey()));

                ltiSettings.setName(row.getNewName());
                String secret = toolConfig.getToolSecrets().get(ltiSettings.getConsumerKey());
                if (secret != null) {
                    ltiSettings.setSharedSecret(secret);
                } else {
                    resultMessages.add("Unable to locate shared secret for the given consumer key: " + ltiSettings.getConsumerKey());
                }

                try {
                    String courseId = courseIdLookup(row.getSisCourseId(), courseMap);
                    if (courseId != null) {
                        result.setCanvasCourseId(courseId);
                    }
                    ExternalTool externalTool = externalToolsService.getExternalToolByName("sis_course_id:" + row.getSisCourseId(), row.getNewName(), "course_navigation");
                    if (externalTool == null) {
                        externalTool = externalToolsService.createExternalToolForCourse("sis_course_id:" + row.getSisCourseId(), ltiSettings);
                        if (externalTool != null) {
                            resultMessages.add("Success");
                        }
                    } else {
                        resultMessages.add("Using existing course nav placement");
                    }
                    if (externalTool != null) {
                        result.setToolId(externalTool.getId());
                    }
                } catch (Exception e) {
                    resultMessages.add("Error in step 1: " + e.getMessage());
                    log.error("Error in step 1 of handle11CoursePlacement", e);
                }
            } catch (MissingFieldException mfe) {
                resultMessages.add("Missing required field(s): " + StringUtils.join(mfe.getMissingFields(), ", "));
            }
            result.setMessage(StringUtils.join(resultMessages, "; "));
            results.add(result);

        }
//        log.debug("{}", results);
        return results;
    }

    /**
     * Handle root level placement
     * @param eTextToolConfig Config for this
     * @param data Data to process
     * @param filename Original uploaded filename
     * @param courseMap Course "cache"
     * @return List of results
     */
    private List<ETextResult> handleRootPlacement(ETextToolConfig eTextToolConfig, List<ETextCsv> data,
                                                  String filename, Map<String, String> courseMap) {
        ConfigSettings configSettings = eTextToolConfig.getJsonBody();
        List<ETextResult> results = new ArrayList<>();

        CanvasTab tab = SerializationUtils.clone(configSettings.getCanvasTab());

        for (ETextCsv row : data) {
            ETextResult result = new ETextResult(eTextToolConfig.getToolName(), filename);
            result.setSisCourseId(row.getSisCourseId());

            try {
                checkRequiredFields(new RequiredField("csv: sis course id", row.getSisCourseId()));

                try {
                    String courseId = courseIdLookup(row.getSisCourseId(), courseMap);
                    if (courseId != null) {
                        result.setCanvasCourseId(courseId);
                    }
                    if (tab.getHidden()) {
                        courseService.hideCourseTool("sis_course_id:" + row.getSisCourseId(), eTextToolConfig.getContextId());
                    } else {
                        courseService.showCourseTool("sis_course_id:" + row.getSisCourseId(), eTextToolConfig.getContextId());
                    }
                    result.setMessage("Success");
                } catch (Exception e) {
                    result.setMessage("Error enabling tool: " + e.getMessage());
                    log.error("error in handle", e);
                }
                result.setToolId(eTextToolConfig.getContextId());

            } catch (MissingFieldException mfe) {
                result.setMessage("Missing required field(s): " + StringUtils.join(mfe.getMissingFields(), ", "));
            }

            results.add(result);
        }
//        log.debug("{}", results);
        return results;
    }

    /**
     * Handle module placement
     * @param eTextToolConfig Config for this
     * @param data Data to process
     * @param filename Original uploaded filename
     * @param courseMap Course "cache"
     * @return List of results
     */
    private List<ETextResult> handleModulePlacement(ETextToolConfig eTextToolConfig, List<ETextCsv> data,
                                                    String filename, Map<String, String> courseMap) {
        ConfigSettings configSettings = eTextToolConfig.getJsonBody();
        List<ETextResult> results = new ArrayList<>();

        ModuleCreateWrapper mcw = new ModuleCreateWrapper();
        Module clonedMod = SerializationUtils.clone(configSettings.getModule());
        mcw.setModule(clonedMod);

        for (ETextCsv row : data) {
            List<String> resultMessages = new ArrayList<>();
            ETextResult result = new ETextResult(eTextToolConfig.getToolName(), filename);
            result.setSisCourseId(row.getSisCourseId());

            try {
                checkRequiredFields(new RequiredField("csv: sis course id", row.getSisCourseId()),
                        new RequiredField("config: module name", configSettings.getModule().getName()),
                        new RequiredField("config: module item type", configSettings.getModuleItem().getType()),
                        new RequiredField("csv: pressbook title or config: module item title", row.getPressbookTitle(), configSettings.getModuleItem().getTitle()),
                        new RequiredField("csv: pressbook link or config: module item external url", row.getPressbookLink(), configSettings.getModuleItem().getExternalUrl()));

                Module module = null;
                try {
                    String courseId = courseIdLookup(row.getSisCourseId(), courseMap);
                    if (courseId != null) {
                        result.setCanvasCourseId(courseId);
                    }
                    module = moduleService.getModuleByName("sis_course_id:" + row.getSisCourseId(), configSettings.getModule().getName());
                    if (module == null) {
                        module = moduleService.createModule("sis_course_id:" + row.getSisCourseId(), mcw);
                    } else {
                        resultMessages.add("Found existing module with matching title");
                    }
                    result.setToolId(module.getId());
                } catch (Exception e) {
                    resultMessages.add("Error in step 1: " + e.getMessage());
                    log.error("Error in step 1", e);
                }
                if (module != null) {
                    try {
                        ModuleItem mi = SerializationUtils.clone(configSettings.getModuleItem());
                        if (StringUtils.isNotBlank(row.getPressbookTitle())) {
                            mi.setTitle(row.getPressbookTitle());
                        }
                        if (StringUtils.isNotBlank(row.getPressbookLink())) {
                            mi.setExternalUrl(row.getPressbookLink());
                        }
                        ModuleItemCreateWrapper micw = new ModuleItemCreateWrapper();
                        micw.setModuleItem(mi);
                        ModuleItem moduleItem = moduleService.getModuleItemByTitle("sis_course_id:" + row.getSisCourseId(),
                                module.getId(), configSettings.getModuleItem().getTitle());

                        if (moduleItem == null) {
                            moduleItem = moduleService.createModuleItem("sis_course_id:" + row.getSisCourseId(), module.getId(), micw);

                            if (moduleItem != null) {
                                if (configSettings.getPublishModule() != null && configSettings.getPublishModule()) {
                                    module = moduleService.publishModule("sis_course_id:" + row.getSisCourseId(), module.getId(), true);
                                }
                                if (module != null) {
                                    result.setDeploymentId(moduleItem.getId());
                                    resultMessages.add("Success");
                                } else {
                                    resultMessages.add("Error in step 3");
                                }
                            }
                        } else {
                            result.setDeploymentId(moduleItem.getId());
                            resultMessages.add("Found existing module item with matching title");
                        }

                    } catch (Exception e) {
                        resultMessages.add("Error in step 2: " + e.getMessage());
                        log.error("Error in step 2", e);
                    }
                }
            } catch (MissingFieldException mfe) {
                resultMessages.add("Missing required field(s): " + StringUtils.join(mfe.getMissingFields(), ", "));
            }

            result.setMessage(StringUtils.join(resultMessages, "; "));
            results.add(result);

        }
//        log.debug("{}", results);
        return results;
    }

    /**
     * Send an email with status results information
     * @param batch ETextResultsBatch
     */
    private void sendEmail(ETextResultsBatch batch) {
        Map<String, Object> emailModel = new HashMap<>();

        emailModel.put("env" , toolConfig.getEnv());

        emailModel.put("batch", batch);

        LmsBatchEmail emails = batchEmailService.getBatchEmailFromGroupCode(toolConfig.getGroupCode());
        String[] emailAddresses = toolConfig.getDefaultEmails();
        if (emails != null) {
            emailAddresses = emails.getEmails().split(",");
        }
        if (emailAddresses != null && emailAddresses.length > 0) {

            try {
                Template freemarkerTemplate = freemarkerConfigurer.createConfiguration()
                        .getTemplate("emailResults.ftlh");

                String body = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, emailModel);

                EmailDetails details = new EmailDetails();
                details.setRecipients(emailAddresses);

                details.setSubject(emailService.getStandardHeader() + " eText processing status");
                details.setBody(body);
                details.setEnableHtml(true);

                Path createdPath = Files.createTempDirectory(null);
                String fileName = "etext_results.csv";
                String filePath = createdPath.toString() + "/" + fileName;
                CsvUtil.write(filePath, batch.getResults(), ETextResult.class);
                URL fileAttachmentURL = new File(filePath).toURI().toURL();


                EmailServiceAttachment attachment = new EmailServiceAttachment();
                attachment.setFilename(fileName);
                attachment.setUrl(fileAttachmentURL);
                details.setEmailServiceAttachmentList(Collections.singletonList(attachment));

                emailService.sendEmail(details);
            } catch (TemplateException | IOException | LmsEmailTooBigException | MessagingException e) {
                log.error("Unable to send email", e);
            }
        }
    }

    /**
     * Get all ETextToolConfig records
     * @return List of ETextToolConfig records
     */
    public List<ETextToolConfig> getToolConfigs() {
        List<ETextToolConfig> all = null;
        try {
            all = (List<ETextToolConfig>) eTextToolConfigRepository.findAll();
        } catch (Exception e) {
            log.error("uh oh", e);
        }
        return all;
    }

    /**
     * Get all ETextResultsBatch records
     * @return List of ETextResultsBatch records
     */
    public List<ETextResultsBatch> getResultBatches() {
        return (List<ETextResultsBatch>) eTextResultsBatchRepository.findAll();
    }

    /**
     * Get the configured json pretty printer
     * @return the configured json pretty printer
     */
    public static DefaultPrettyPrinter getJsonPrettyPrinter() {
        return PRETTY_PRINTER;
    }

    /**
     * Lookup a course from the "cache", or Canvas if not found in the local map
     * @param sisCourseId Sis Course id to use for the lookup
     * @param courseMap Map of already looked up courses
     * @return Canvas course id of the found course, null if no course exists
     */
    private String courseIdLookup(String sisCourseId, Map<String, String> courseMap) {
        return courseMap.computeIfAbsent(sisCourseId, key -> {
            Course course = courseService.getCourse("sis_course_id:" + key);
            if (course != null) {
                return course.getId();
            }
            return null;
        });
    }

    /**
     * Check if any of the passed required fields are missing values
     * @param requiredFields List of RequiredField objects
     * @throws MissingFieldException If any fields are missing
     */
    private void checkRequiredFields(RequiredField... requiredFields) throws MissingFieldException {
        List<String> missingFields = Arrays.stream(requiredFields)
                .filter(RequiredField::invalid)
                .map(RequiredField::getFieldName)
                .collect(Collectors.toList());

        if (!missingFields.isEmpty()) {
            throw new MissingFieldException("The following fields were required but missing", missingFields);
        }
    }

    /**
     * Delete a tool config
     * @param id Id to delete
     */
    public void deleteToolConfig(Long id) {
        eTextToolConfigRepository.deleteById(id);
    }

    /**
     * Add or edit a tool config
     * @param id Id to edit
     * @param eTextToolConfig Tool config to save
     */
    public void addEditToolConfig(Long id, ETextToolConfig eTextToolConfig) {
        // Lookup with existing id, create new one if none found
        ETextToolConfig existing = eTextToolConfigRepository.findById(id).orElse(new ETextToolConfig());

        // Merge the updated fields with the existing fields
        existing.mergeEditableFields(eTextToolConfig);
        eTextToolConfigRepository.save(existing);
    }

    /**
     * Class used to represent required fields
     */
    @Data
    @AllArgsConstructor
    private static class RequiredField implements Serializable {
        private String fieldName;
        private String fieldValue;

        /**
         *
         * @param fieldName Name of the field to use for the error message
         * @param fieldValues List of possible values.  Will use the first non-blank value found
         */
        public RequiredField(String fieldName, String... fieldValues) {
            this.fieldName = fieldName;
            this.fieldValue = StringUtils.firstNonBlank(fieldValues);
        }

        /**
         * If the fieldValue is blank, it is invalid
         * @return True/false for invalid/valid
         */
        public boolean invalid() {
            return StringUtils.isBlank(fieldValue);
        }
    }

}
