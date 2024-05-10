package com.biplab.dholey.rmp.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class CustomStringUtil {

    public static String MapToJSONString(Map<String, String> m) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(m);
    }
}
