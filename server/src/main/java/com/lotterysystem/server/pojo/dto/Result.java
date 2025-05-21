package com.lotterysystem.server.pojo.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.lotterysystem.server.constant.ResultStatue;
import lombok.Data;

@Data
public class Result<T> {
    private int status = ResultStatue.SUCCESS.getCode();
    private String message;
    private T data;

    @JsonCreator
    public Result(@JsonProperty("status") int status,
                  @JsonProperty("message") String message,
                  @JsonProperty("data") T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Result(ResultStatue status, String message, T data) {
        this.status = status.getCode();
        this.message = message;
        this.data = data;
    }


}
