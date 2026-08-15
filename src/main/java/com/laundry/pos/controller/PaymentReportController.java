package com.laundry.pos.controller;

import com.laundry.pos.response.PaymentReportResponse;
import com.laundry.pos.service.PaymentReportService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentReportController {

    private final PaymentReportService paymentReportService;

    public PaymentReportController(
            PaymentReportService paymentReportService
    ) {

        this.paymentReportService =
                paymentReportService;
    }


    @GetMapping("/report")
    public ResponseEntity<PaymentReportResponse>
    getPaymentReport(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate
    ) {

        return ResponseEntity.ok(
                paymentReportService
                        .getPaymentReport(
                                fromDate,
                                toDate
                        )
        );
    }
}