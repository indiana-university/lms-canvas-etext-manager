package edu.iu.uits.lms.etextmanager.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import edu.iu.uits.lms.canvas.model.LtiSettings;
import edu.iu.uits.lms.canvas.model.Module;
import edu.iu.uits.lms.canvas.model.ModuleItem;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigSettings implements Serializable {

    private CanvasTab canvasTab;
    private LtiSettings ltiSettings;
    private Module module;
    private ModuleItem moduleItem;
    private Boolean publishModule;

}
