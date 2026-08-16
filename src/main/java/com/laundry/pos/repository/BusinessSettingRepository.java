package com.laundry.pos.repository;

import com.laundry.pos.model.BusinessSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessSettingRepository
        extends JpaRepository<BusinessSetting, Long> {

    Optional<BusinessSetting>
    findFirstByOrderByIdAsc();
}