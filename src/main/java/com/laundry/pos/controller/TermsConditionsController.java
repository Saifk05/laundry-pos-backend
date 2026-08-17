package com.laundry.pos.controller;

import com.laundry.pos.request.TermsConditionsRequest;
import com.laundry.pos.response.TermsConditionsResponse;
import com.laundry.pos.service.TermsConditionsService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/terms-conditions")
public class TermsConditionsController {

    private final TermsConditionsService
            termsConditionsService;


    public TermsConditionsController(
            TermsConditionsService termsConditionsService
    ) {

        this.termsConditionsService =
                termsConditionsService;
    }


    @GetMapping
    public ResponseEntity<TermsConditionsResponse>
    getTermsConditions() {

        return ResponseEntity.ok(
                termsConditionsService
                        .getTermsConditions()
        );
    }


    @PutMapping
    public ResponseEntity<TermsConditionsResponse>
    updateTermsConditions(
            @RequestBody
            TermsConditionsRequest request
    ) {

        return ResponseEntity.ok(
                termsConditionsService
                        .updateTermsConditions(
                                request
                        )
        );
    }
}