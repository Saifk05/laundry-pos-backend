package com.laundry.pos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "business_settings")
public class BusinessSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String headerSubtitle;

    @Column(nullable = false)
    private String adminName;

    @Column(nullable = false)
    private String adminSubtitle;

    private String logoUrl;

    public Long getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(
            String businessName
    ) {
        this.businessName =
                businessName;
    }

    public String getHeaderSubtitle() {
        return headerSubtitle;
    }

    public void setHeaderSubtitle(
            String headerSubtitle
    ) {
        this.headerSubtitle =
                headerSubtitle;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(
            String adminName
    ) {
        this.adminName =
                adminName;
    }

    public String getAdminSubtitle() {
        return adminSubtitle;
    }

    public void setAdminSubtitle(
            String adminSubtitle
    ) {
        this.adminSubtitle =
                adminSubtitle;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(
            String logoUrl
    ) {
        this.logoUrl =
                logoUrl;
    }
}