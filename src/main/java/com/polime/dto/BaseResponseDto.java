package com.polime.dto;

public class BaseResponseDto<T> {
    private String code;
    private String message;
    private T result;

    public BaseResponseDto() {
    }

    public BaseResponseDto(String message, String code) {
        this.message = message;
        this.code = code;
    }

    public BaseResponseDto(String message, String code, T result) {
        this.message = message;
        this.code = code;
        this.result = result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
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
