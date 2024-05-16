package com.biplab.dholey.rmp.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class CustomLogger {

    private final Logger logger;

    public CustomLogger(Logger logger) {
        this.logger = logger;
    }

    public void info(String message, String method, String class_, Map<String, String> attr) {
        Map<String, String> m = new HashMap<>();
        m.put("message", message);
        m.put("method", method);
        m.put("class", class_);
        if (attr != null) {
            m.putAll(attr);
        }
        try {
            logger.info(CustomStringUtil.MapToJSONString(m));
        } catch (Exception e) {
            log.error("Inside info, exception raised", e);
        }
    }

    public void error(String message, String method, String class_, Exception ex, Map<String, String> attr) {
        //TODO: add a error logger channel (queue), which could later be pushed into Error Log Database.
        Map<String, String> m = new HashMap<>();
        m.put("message", message);
        m.put("method", method);
        m.put("class", class_);
        m.put("exception", ex.getMessage());
        m.put("stackTrace", Arrays.toString(ex.getStackTrace()));
        if (attr != null) {
            m.putAll(attr);
        }
        try {
            logger.error(CustomStringUtil.MapToJSONString(m));
        } catch (Exception e) {
            log.error("Inside error, exception raised", e);
        }
    }

    public void debug(String message, String method, String class_, Map<String, String> attr) {
        Map<String, String> m = new HashMap<>();
        m.put("message", message);
        m.put("method", method);
        m.put("class", class_);
        if (attr != null) {
            m.putAll(attr);
        }
        try {
            logger.debug(CustomStringUtil.MapToJSONString(m));
        } catch (Exception e) {
            log.error("Inside debug, exception raised", e);
        }
    }
}
