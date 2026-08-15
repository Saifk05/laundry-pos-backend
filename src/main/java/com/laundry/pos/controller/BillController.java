package com.laundry.pos.controller;

import com.laundry.pos.response.BillListResponse;
import com.laundry.pos.service.BillPdfService;
import com.laundry.pos.service.BillService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin
public class BillController {

    private final BillService billService;
    private final BillPdfService billPdfService;

    public BillController(
            BillService billService,
            BillPdfService billPdfService
    ) {
        this.billService =
                billService;

        this.billPdfService =
                billPdfService;
    }

    @GetMapping
    public ResponseEntity<BillListResponse> getBills() {

        return ResponseEntity.ok(
                billService
                        .getBills()
        );
    }

    @GetMapping("/{orderId}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable UUID orderId
    ) {

        byte[] pdf =
                billPdfService
                        .generateReceipt(
                                orderId
                        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"receipt.pdf\""
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .contentLength(
                        pdf.length
                )
                .body(
                        pdf
                );
    }

    @PostMapping("/{orderId}/whatsapp")
    public ResponseEntity<String> sendReceiptToWhatsApp(
            @PathVariable UUID orderId
    ) {

        return ResponseEntity.ok(
                billService
                        .sendReceiptToWhatsApp(
                                orderId
                        )
        );
    }
}