package com.datn.datnbe.ai.dto;

import lombok.Data;

@Data
public class AIModificationResponse {
    private boolean success;
    private Object data;
    private String message;

    public static AIModificationResponse success(Object data) {
        AIModificationResponse response = new AIModificationResponse();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static AIModificationResponse success(Object data, String message) {
        AIModificationResponse response = new AIModificationResponse();
        response.setSuccess(true);
        response.setData(data);
        response.setMessage(message);
        return response;
    }

    public static AIModificationResponse error(String message) {
        AIModificationResponse response = new AIModificationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
