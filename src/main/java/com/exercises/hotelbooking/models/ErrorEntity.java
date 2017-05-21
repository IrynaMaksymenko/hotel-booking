package com.exercises.hotelbooking.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor @AllArgsConstructor
public class ErrorEntity {

    private @Getter @Setter int status;
    private @Getter @Setter String message;
    private @Getter @Setter List<String> details;

    public ErrorEntity(int status, String message) {
        this.status = status;
        this.message = message;
    }

}
