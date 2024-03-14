package edu.iu.uits.lms.etextmanager.service;

import lombok.Getter;

import java.util.List;

public class MissingFieldException extends Exception {

    @Getter
    private List<String> missingFields;

    public MissingFieldException(String message, List<String> missingFields) {
        super(message);
        this.missingFields = missingFields;
    }

}
