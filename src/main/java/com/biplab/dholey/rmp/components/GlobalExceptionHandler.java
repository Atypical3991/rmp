package com.biplab.dholey.rmp.components;

import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
public class GlobalExceptionHandler {

    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(GlobalExceptionHandler.class));

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ResponseEntity<String> handleException(Exception e) {
        logger.error("An unknown error occurred!!", "handleException", GlobalExceptionHandler.class.toString(), e, null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unknown error occurred!!");
    }
}