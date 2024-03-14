package edu.iu.uits.lms.etextmanager.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.iu.uits.lms.common.date.DateFormatUtil;
import edu.iu.uits.lms.etextmanager.service.ETextService;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "ETEXT_TOOL_CONFIG",
        uniqueConstraints = @UniqueConstraint(name = "UK_ETEXT_TOOL_CONFIG", columnNames = {"TOOL_NAME"}))
@SequenceGenerator(name = "ETEXT_TOOL_CONFIG_ID_SEQ", sequenceName = "ETEXT_TOOL_CONFIG_ID_SEQ", allocationSize = 1)
@Data
@AllArgsConstructor
@NoArgsConstructor
@TypeDef(name = "json", typeClass = JsonType.class)
public class ETextToolConfig implements Serializable {

    @Id
    @GeneratedValue(generator = "ETEXT_TOOL_CONFIG_ID_SEQ")
    @Column(name = "ETEXT_TOOL_CONFIG_ID")
    private Long id;

    @Column(name = "TOOL_NAME")
    private String toolName;

    @Enumerated(EnumType.STRING)
    @Column(name = "TOOL_TYPE")
    private TOOL_TYPE toolType;

    @Column(name = "CONTEXT_ID")
    private String contextId;

    @Column(name = "JSON_BODY", columnDefinition = "json")
    @Type(type = "json")
    private ConfigSettings jsonBody;

    @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
    @Column(name = "CREATEDON")
    private Date createdOn;

    @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
    @Column(name = "MODIFIEDON")
    private Date modifiedOn;

    public String prettyPrintJson() {
        ObjectMapper om = new ObjectMapper();
        try {
            om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return om.writer(ETextService.getJsonPrettyPrinter()).writeValueAsString(jsonBody);
        } catch (JsonProcessingException e) {
            return "ERROR PARSING JSON";
        }
    }

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        modifiedOn = new Date();
        if (createdOn==null) {
            createdOn = new Date();
        }
    }

    @AllArgsConstructor
    @Getter
    public enum TOOL_TYPE {
        COURSE_13_PLACEMENT("1.3 Course Placement"),
        ROOT_13_PLACEMENT("1.3 Root Placement"),
        MODULE_PLACEMENT("Module Placement"),
        COURSE_11_PLACEMENT("1.1 Course Placement"),
        COURSE_11_AND_MODULE_PLACEMENT("1.1 Course and Module Placement");

        private String value;
    }

}
