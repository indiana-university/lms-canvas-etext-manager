package edu.iu.uits.lms.etextmanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ETextCsv implements Serializable {

    @JsonProperty("Tool")
    private String tool;

    @JsonProperty("New Name")
    private String newName;

    @JsonProperty("SIS Course ID")
    private String sisCourseId;

    @JsonProperty("Pressbook Title")
    private String pressbookTitle;

    @JsonProperty("Pressbook Link")
    private String pressbookLink;

}
