package com.laundry.pos.controller;

import com.laundry.pos.service.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppTestController {

    private final WhatsAppService whatsAppService;

    public WhatsAppTestController(
            WhatsAppService whatsAppService
    ) {
        this.whatsAppService =
                whatsAppService;
    }

    @PostMapping("/test")
    public ResponseEntity<String> sendTestMessage(
            @RequestParam String mobile,
            @RequestParam String customerName,
            @RequestParam String orderNumber
    ) {

        String response =
                whatsAppService
                        .sendProcessingMessage(
                                mobile,
                                customerName,
                                orderNumber
                        );

        return ResponseEntity.ok(
                response
        );
    }

    @PostMapping("/text")
    public ResponseEntity<String> sendTextMessage(
            @RequestParam String mobile
    ) {

        String response =
                whatsAppService
                        .sendTextMessage(
                                mobile,
                                "Your Venkateshwara Fabric Works WhatsApp integration is working."
                        );

        return ResponseEntity.ok(
                response
        );
    }
}