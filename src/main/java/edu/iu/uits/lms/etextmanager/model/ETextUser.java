package edu.iu.uits.lms.etextmanager.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.iu.uits.lms.common.date.DateFormatUtil;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "ETEXT_USERS",
        uniqueConstraints = @UniqueConstraint(name = "UK_ETEXT_USERS", columnNames = {"username"}))
@SequenceGenerator(name = "ETEXT_USERS_ID_SEQ", sequenceName = "ETEXT_USERS_ID_SEQ", allocationSize = 1)
@Data
public class ETextUser implements Serializable {

    @Id
    @GeneratedValue(generator = "ETEXT_USERS_ID_SEQ")
    @Column(name = "ETEXT_USER_ID")
    private Long id;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "DISPLAY_NAME")
    private String displayName;

    @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
    @Column(name = "CREATEDON")
    private Date createdOn;

    @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
    @Column(name = "MODIFIEDON")
    private Date modifiedOn;


    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        modifiedOn = new Date();
        if (createdOn==null) {
            createdOn = new Date();
        }
    }

}
