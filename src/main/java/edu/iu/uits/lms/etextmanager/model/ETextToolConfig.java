package edu.iu.uits.lms.etextmanager.model;

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

    /**
     * Output the json in a "pretty" format
     * @return
     */
    public String prettyPrintJson() {
        ObjectMapper om = new ObjectMapper();
        try {
            om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return om.writer(ETextService.getJsonPrettyPrinter()).writeValueAsString(jsonBody);
        } catch (JsonProcessingException e) {
            return "ERROR PARSING JSON";
        }
    }

    /**
     * Merge fields from the input into this object
     * @param editable
     */
    public void mergeEditableFields(ETextToolConfig editable) {
        if (editable != null) {
            this.toolName = editable.getToolName().trim();
            this.toolType = editable.getToolType();
            this.contextId = editable.getContextId().trim();
            this.jsonBody = editable.getJsonBody();
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
