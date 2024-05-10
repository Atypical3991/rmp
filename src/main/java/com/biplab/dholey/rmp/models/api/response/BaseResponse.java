package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public abstract class BaseResponse {
    @JsonProperty("error")
    private String error;
    @JsonProperty("message")
    private String message;
    @JsonProperty("statusCode")
    private Integer statusCode;


    public abstract BaseResponse getInternalServerErrorResponse(String message, Exception e);

    public abstract BaseResponse getNotFoundServerErrorResponse(String message);

    public abstract BaseResponse getSuccessResponse(Object data, String message);

}
