package edu.iu.uits.lms.etextmanager.job;

import edu.iu.uits.lms.common.batch.BatchJob;
import edu.iu.uits.lms.etextmanager.service.ETextService;
import edu.iu.uits.lms.iuonly.model.errorcontact.ErrorContactPostForm;
import edu.iu.uits.lms.iuonly.services.ErrorContactServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Profile("failedresultprocessing")
public class FailedResultProcessorJob implements BatchJob {

    @Autowired
    private ETextService eTextService;

    @Autowired
    private ConfigurableApplicationContext ctx;

    @Autowired
    private ErrorContactServiceImpl errorContactService;

    private void process() throws IOException {
        log.info("FailedResultProcessorJob running!");
        eTextService.processFailedResultBatchJob();
    }

    @Override
    public void run() {

        try {
            process();
        } catch (Exception e) {
            log.error("Caught exception during FailedResultProcessorJob processing", e);

            ErrorContactPostForm errorContactPostForm = new ErrorContactPostForm();
            errorContactPostForm.setJobCode(getJobCode());
            errorContactPostForm.setMessage("The eText FailedResultProcessorJob has unexpectedly failed");

            errorContactService.postEvent(errorContactPostForm);
        }

        ctx.close();
    }

    public String getJobCode() {
        return "eTextFailedResultProcessorJob";
    }
}
