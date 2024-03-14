package edu.iu.uits.lms.etextmanager.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.iu.uits.lms.common.date.DateFormatUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ETEXT_RESULTS", joinColumns = @JoinColumn(name = "ETEXT_RESULTS_BATCH_ID"),
            foreignKey = @ForeignKey(name = "FK_etext_results_batch"))
    @EqualsAndHashCode.Exclude
    private List<ETextResult> results;

    @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
    @Column(name = "RUN_DATE")
    private Date runDate;

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        runDate = new Date();
    }

}
