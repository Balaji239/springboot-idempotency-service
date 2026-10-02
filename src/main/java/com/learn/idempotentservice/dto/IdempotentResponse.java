package com.learn.idempotentservice.dto;

import java.io.Serializable;

public class IdempotentResponse implements Serializable {

    private int statusCode;
    private String body;
    private String status; // "IN_PROGRESS" or "COMPLETED"

    public IdempotentResponse(int statusCode, String body, String status) {
        this.statusCode = statusCode;
        this.body = body;
        this.status = status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
