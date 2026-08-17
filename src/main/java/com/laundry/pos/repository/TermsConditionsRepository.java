package com.laundry.pos.repository;

import com.laundry.pos.model.TermsConditions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermsConditionsRepository
        extends JpaRepository<TermsConditions, Long> {

    Optional<TermsConditions> findFirstByActiveTrueOrderByIdDesc();
}