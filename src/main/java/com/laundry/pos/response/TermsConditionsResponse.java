package com.laundry.pos.response;

import java.time.LocalDateTime;

public class TermsConditionsResponse {

    private Long id;

    private String termsText;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String message;


    public Long getId() {
        return id;
    }


    public void setId(
            Long id
    ) {
        this.id = id;
    }


    public String getTermsText() {
        return termsText;
    }


    public void setTermsText(
            String termsText
    ) {
        this.termsText =
                termsText;
    }


    public boolean isActive() {
        return active;
    }


    public void setActive(
            boolean active
    ) {
        this.active =
                active;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt =
                createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt =
                updatedAt;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(
            String message
    ) {
        this.message =
                message;
    }
}