package com.polime.dto;

import com.polime.enums.EResponseCode;

public class BaseResponseDto<T> {
    private EResponseCode code;
    private String message;
    private T result;

    public BaseResponseDto() {
    }

    public BaseResponseDto(String message, EResponseCode code) {
        this.message = message;
        this.code = code;
    }

    public BaseResponseDto(String message, EResponseCode code, T result) {
        this.message = message;
        this.code = code;
        this.result = result;
    }

    public EResponseCode getCode() {
        return code;
    }

    public void setCode(EResponseCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
