package edu.iu.uits.lms.etextmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class ETextResult {

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

}
