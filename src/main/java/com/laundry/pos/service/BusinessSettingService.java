package com.laundry.pos.service;

import com.laundry.pos.model.BusinessSetting;
import com.laundry.pos.repository.BusinessSettingRepository;
import com.laundry.pos.request.BusinessSettingRequest;
import com.laundry.pos.response.BusinessSettingResponse;
import org.springframework.stereotype.Service;

@Service
public class BusinessSettingService {

    private final BusinessSettingRepository
            businessSettingRepository;

    public BusinessSettingService(
            BusinessSettingRepository
                    businessSettingRepository
    ) {
        this.businessSettingRepository =
                businessSettingRepository;
    }

    public BusinessSettingResponse
    getSettings() {

        BusinessSetting setting =
                businessSettingRepository
                        .findFirstByOrderByIdAsc()
                        .orElseGet(
                                this::createDefaultSettings
                        );

        return toResponse(
                setting
        );
    }

    public BusinessSettingResponse
    updateSettings(
            BusinessSettingRequest request
    ) {

        BusinessSetting setting =
                businessSettingRepository
                        .findFirstByOrderByIdAsc()
                        .orElseGet(
                                this::createDefaultSettings
                        );

        setting.setBusinessName(
                request.businessName()
        );

        setting.setHeaderSubtitle(
                request.headerSubtitle()
        );

        setting.setAdminName(
                request.adminName()
        );

        setting.setAdminSubtitle(
                request.adminSubtitle()
        );

        setting.setLogoUrl(
                request.logoUrl()
        );

        BusinessSetting saved =
                businessSettingRepository.save(
                        setting
                );

        return toResponse(
                saved
        );
    }

    private BusinessSetting
    createDefaultSettings() {

        BusinessSetting setting =
                new BusinessSetting();

        setting.setBusinessName(
                "Venkateshwara Fabric Works"
        );

        setting.setHeaderSubtitle(
                "Operations"
        );

        setting.setAdminName(
                "Admin"
        );

        setting.setAdminSubtitle(
                "Laundry"
        );

        setting.setLogoUrl(
                null
        );

        return businessSettingRepository.save(
                setting
        );
    }

    private BusinessSettingResponse
    toResponse(
            BusinessSetting setting
    ) {

        return new BusinessSettingResponse(
                setting.getId(),
                setting.getBusinessName(),
                setting.getHeaderSubtitle(),
                setting.getAdminName(),
                setting.getAdminSubtitle(),
                setting.getLogoUrl()
        );
    }
}