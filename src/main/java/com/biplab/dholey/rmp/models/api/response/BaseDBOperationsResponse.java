package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class BaseDBOperationsResponse extends BaseResponse {

    @JsonProperty("data")
    private BaseDBOperationsResponseResponseData data;

    public BaseDBOperationsResponse getSuccessResponse(Object data, String message) {
        this.setMessage(message);
        this.setData((BaseDBOperationsResponse.BaseDBOperationsResponseResponseData) data);
        this.setStatusCode(HttpStatus.OK.value());
        return this;
    }

    @Override
    public BaseDBOperationsResponse getInternalServerErrorResponse(String message, Exception e) {
        this.setData(null);
        this.setMessage(message);
        this.setError(e.getMessage());
        this.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this;
    }

    @Override
    public BaseDBOperationsResponse getNotFoundServerErrorResponse(String message) {
        this.setData(null);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_FOUND.value());
        return this;
    }

    public BaseDBOperationsResponse getNotAcceptableServerErrorResponse(String message) {
        this.setData(null);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
        return this;
    }

    @Data
    public static class BaseDBOperationsResponseResponseData {
        @JsonProperty("success")
        private Boolean success = true;
    }

}
