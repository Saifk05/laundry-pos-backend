package com.laundry.pos.controller;

import com.laundry.pos.response.SalesReportResponse;
import com.laundry.pos.service.SalesReportService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/reports")
public class SalesReportController {

    private final SalesReportService salesReportService;


    public SalesReportController(
            SalesReportService salesReportService
    ) {
        this.salesReportService =
                salesReportService;
    }


    @GetMapping("/sales")
    public ResponseEntity<SalesReportResponse> getSalesReport(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate endDate
    ) {

        return ResponseEntity.ok(
                salesReportService
                        .getSalesReport(
                                startDate,
                                endDate
                        )
        );
    }
}