package com.laundry.pos.service;

import com.laundry.pos.model.TermsConditions;
import com.laundry.pos.repository.TermsConditionsRepository;
import com.laundry.pos.request.TermsConditionsRequest;
import com.laundry.pos.response.TermsConditionsResponse;

import org.springframework.stereotype.Service;

@Service
public class TermsConditionsService {

    private final TermsConditionsRepository
            termsConditionsRepository;


    public TermsConditionsService(
            TermsConditionsRepository termsConditionsRepository
    ) {

        this.termsConditionsRepository =
                termsConditionsRepository;
    }


    public TermsConditionsResponse getTermsConditions() {

        TermsConditions termsConditions =
                termsConditionsRepository
                        .findFirstByActiveTrueOrderByIdDesc()
                        .orElse(null);

        if (termsConditions == null) {

            TermsConditionsResponse response =
                    new TermsConditionsResponse();

            response.setTermsText("");
            response.setActive(false);
            response.setMessage(
                    "Terms & Conditions not configured"
            );

            return response;
        }

        return mapResponse(
                termsConditions,
                "Terms & Conditions fetched successfully"
        );
    }


    public TermsConditionsResponse updateTermsConditions(
            TermsConditionsRequest request
    ) {

        TermsConditions termsConditions =
                termsConditionsRepository
                        .findFirstByActiveTrueOrderByIdDesc()
                        .orElseGet(
                                TermsConditions::new
                        );

        termsConditions.setTermsText(
                request.getTermsText()
                        .trim()
        );

        termsConditions.setActive(
                true
        );

        TermsConditions savedTerms =
                termsConditionsRepository
                        .save(
                                termsConditions
                        );

        return mapResponse(
                savedTerms,
                "Terms & Conditions saved successfully"
        );
    }


    private TermsConditionsResponse mapResponse(
            TermsConditions termsConditions,
            String message
    ) {

        TermsConditionsResponse response =
                new TermsConditionsResponse();

        response.setId(
                termsConditions.getId()
        );

        response.setTermsText(
                termsConditions.getTermsText()
        );

        response.setActive(
                termsConditions.isActive()
        );

        response.setCreatedAt(
                termsConditions.getCreatedAt()
        );

        response.setUpdatedAt(
                termsConditions.getUpdatedAt()
        );

        response.setMessage(
                message
        );

        return response;
    }
}