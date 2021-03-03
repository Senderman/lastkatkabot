package com.senderman.lastkatkabot.pair.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

public class ResponseResult<T> {

    @JsonProperty("ok")
    private boolean isOk;

    @JsonProperty("result")
    @Nullable
    private T result;

    @JsonProperty("errcode")
    @Nullable
    private Integer errorCode;

    @JsonProperty("reason")
    @Nullable
    private String reason;

    public boolean isOk() {
        return isOk;
    }

    public T getResult() {
        return result;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getReason() {
        return reason;
    }
}
