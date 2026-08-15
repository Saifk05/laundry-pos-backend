package com.laundry.pos.controller;

import com.laundry.pos.service.WhatsAppTemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp/templates")
public class WhatsAppTemplateController {

    private final WhatsAppTemplateService whatsappTemplateService;

    public WhatsAppTemplateController(
            WhatsAppTemplateService whatsappTemplateService
    ) {
        this.whatsappTemplateService =
                whatsappTemplateService;
    }

    @GetMapping
    public ResponseEntity<String> getTemplates() {

        return ResponseEntity.ok(
                whatsappTemplateService
                        .getTemplates()
        );
    }

    @PostMapping("/processing")
    public ResponseEntity<String> createProcessingTemplate() {

        return ResponseEntity.ok(
                whatsappTemplateService
                        .createProcessingTemplate()
        );
    }

    @PostMapping("/ready")
    public ResponseEntity<String> createReadyTemplate() {

        return ResponseEntity.ok(
                whatsappTemplateService
                        .createReadyTemplate()
        );
    }

    @PostMapping("/delivered")
    public ResponseEntity<String> createDeliveredTemplate() {

        return ResponseEntity.ok(
                whatsappTemplateService
                        .createDeliveredTemplate()
        );
    }

    @PostMapping("/cancelled")
    public ResponseEntity<String> createCancelledTemplate() {

        return ResponseEntity.ok(
                whatsappTemplateService
                        .createCancelledTemplate()
        );
    }
}