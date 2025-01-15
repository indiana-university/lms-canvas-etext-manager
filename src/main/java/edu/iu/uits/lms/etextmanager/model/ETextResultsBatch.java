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
import edu.iu.uits.lms.common.date.DateFormatUtil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ETEXT_RESULTS_BATCH")
@SequenceGenerator(name = "ETEXT_RESULTS_BATCH_ID_SEQ", sequenceName = "ETEXT_RESULTS_BATCH_ID_SEQ", allocationSize = 1)
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class ETextResultsBatch {

    @Id
    @GeneratedValue(generator = "ETEXT_RESULTS_BATCH_ID_SEQ")
    @Column(name = "ETEXT_RESULTS_BATCH_ID")
    private Long id;

    @NonNull
    @Column(name = "UPLOADER")
    private String uploader;

    @OneToMany(cascade = CascadeType.ALL, targetEntity = ETextResult.class, mappedBy = "batch", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ETextResult> results;

    @NonNull
    @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
    @Column(name = "RUN_DATE")
    private Date runDate;

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        runDate = new Date();
    }

}
