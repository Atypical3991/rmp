package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BaseDBOperationsResponse extends BaseResponse {

    @JsonProperty("data")
    private BaseDBOperationsResponseResponseData data;

    @Data
    public static class BaseDBOperationsResponseResponseData {
        @JsonProperty("success")
        private Boolean success;
    }


}
