package com.exercises.hotelbooking.controllers;

import com.exercises.hotelbooking.models.ErrorEntity;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class GeneralErrorController implements ErrorController {

    private static final String PATH = "/error";

    @RequestMapping(value = PATH, produces = {"application/json"})
    public ErrorEntity errorJson(HttpServletRequest request, HttpServletResponse response) {
        Integer statusCode = (Integer)request.getAttribute("javax.servlet.error.status_code");
        response.setStatus(statusCode);
        return new ErrorEntity(statusCode, "Unexpected error");
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
