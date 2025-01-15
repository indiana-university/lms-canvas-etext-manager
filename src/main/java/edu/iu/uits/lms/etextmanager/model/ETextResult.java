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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.data.rest.core.annotation.RestResource;

@Entity
@Table(name = "ETEXT_RESULTS")
@SequenceGenerator(name = "ETEXT_RESULT_ID_SEQ", sequenceName = "ETEXT_RESULT_ID_SEQ", allocationSize = 1)
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = {"batch"})
@ToString(exclude = {"batch"})
public class ETextResult {

    public enum STATUS {
        SUCCESS,
        FAIL,
        LEGACY
    }

    @Id
    @GeneratedValue(generator = "ETEXT_RESULT_ID_SEQ")
    @Column(name = "ETEXT_RESULT_ID")
    private Long id;

    @NonNull
    @Column(name = "TOOL")
    private String tool;

    @NonNull
    @Column(name = "FILENAME")
    private String filename;

    @Column(name = "TOOL_ID")
    private String toolId;

    @Column(name = "SIS_COURSE_ID")
    private String sisCourseId;

    @Column(name = "CANVAS_COURSE_ID")
    private String canvasCourseId;

    @Column(name = "DEPLOYMENT_ID")
    private String deploymentId;

    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "ARCHIVED")
    private boolean archived;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private STATUS status;

    @Column(name = "INPUT_NEW_NAME")
    private String inputNewName;

    @Column(name = "INPUT_PRESSBOOK_TITLE")
    private String inputPressbookTitle;

    @Column(name = "INPUT_PRESSBOOK_LINK")
    private String inputPressbookLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ETEXT_RESULTS_BATCH_ID", foreignKey = @ForeignKey(name = "FK_etext_results_batch"))
    @RestResource(exported = false)
    @JsonIgnore
    @NonNull
    private ETextResultsBatch batch;

    /**
     * Gets called from the UI
     * @return
     */
    public String iconCssClass() {
        String cssClass = "rvt-color-gold-300";
        switch (status) {
            case FAIL -> cssClass = "rvt-color-crimson-400";
            case SUCCESS -> cssClass = "rvt-color-green-400";
        }
        return cssClass;
    }

}
