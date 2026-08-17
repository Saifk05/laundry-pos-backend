package com.laundry.pos.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "terms_conditions")
public class TermsConditions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "terms_text",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String termsText;

    @Column(
            name = "active",
            nullable = false
    )
    private boolean active = true;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        this.createdAt =
                now;

        this.updatedAt =
                now;
    }


    @PreUpdate
    protected void onUpdate() {

        this.updatedAt =
                LocalDateTime.now();
    }


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
}