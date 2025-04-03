package com.api.gateway.devhub.dto;

import org.springframework.http.HttpStatus;

public class TokenResponse {
    private String email;
    private HttpStatus httpStatus;


    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
    @Override
    public String toString() {
        return "TokenResponse [email=" + email + ", httpStatus=" + httpStatus + "]";
    }
}
