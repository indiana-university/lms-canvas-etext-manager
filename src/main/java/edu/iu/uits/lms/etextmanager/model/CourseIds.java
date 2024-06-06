package edu.iu.uits.lms.etextmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class CourseIds implements Serializable {
    private String sisCourseId;
    private String canvasCourseId;
}
