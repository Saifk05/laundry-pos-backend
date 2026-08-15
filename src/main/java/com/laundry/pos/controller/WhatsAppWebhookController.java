package com.laundry.pos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp/webhook")
public class WhatsAppWebhookController {

    private static final String VERIFY_TOKEN =
            "venkateshwara_whatsapp_webhook_2026";

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false)
            String mode,

            @RequestParam(name = "hub.verify_token", required = false)
            String verifyToken,

            @RequestParam(name = "hub.challenge", required = false)
            String challenge
    ) {

        if (
                "subscribe".equals(mode)
                        &&
                VERIFY_TOKEN.equals(
                        verifyToken
                )
        ) {

            return ResponseEntity.ok(
                    challenge
            );
        }

        return ResponseEntity
                .status(403)
                .body(
                        "Webhook verification failed"
                );
    }

    @PostMapping
    public ResponseEntity<String> receiveWebhook(
            @RequestBody Map<String, Object> payload
    ) {

        try {

            Object entryObject =
                    payload.get(
                            "entry"
                    );

            if (
                    !(entryObject instanceof List<?> entries)
            ) {

                return ResponseEntity.ok(
                        "EVENT_RECEIVED"
                );
            }

            for (
                    Object entryObjectItem :
                    entries
            ) {

                if (
                        !(entryObjectItem instanceof Map<?, ?> entry)
                ) {
                    continue;
                }

                Object changesObject =
                        entry.get(
                                "changes"
                        );

                if (
                        !(changesObject instanceof List<?> changes)
                ) {
                    continue;
                }

                for (
                        Object changeObject :
                        changes
                ) {

                    if (
                            !(changeObject instanceof Map<?, ?> change)
                    ) {
                        continue;
                    }

                    Object valueObject =
                            change.get(
                                    "value"
                            );

                    if (
                            !(valueObject instanceof Map<?, ?> value)
                    ) {
                        continue;
                    }

                    Object statusesObject =
                            value.get(
                                    "statuses"
                            );

                    if (
                            !(statusesObject instanceof List<?> statuses)
                    ) {
                        continue;
                    }

                    for (
                            Object statusObject :
                            statuses
                    ) {

                        if (
                                !(statusObject instanceof Map<?, ?> status)
                        ) {
                            continue;
                        }

                        String messageId =
                                getString(
                                        status,
                                        "id"
                                );

                        String deliveryStatus =
                                getString(
                                        status,
                                        "status"
                                );

                        String recipientId =
                                getString(
                                        status,
                                        "recipient_id"
                                );

                        System.out.println(
                                "================================="
                        );

                        System.out.println(
                                "WhatsApp Message ID: "
                                        + messageId
                        );

                        System.out.println(
                                "WhatsApp Status: "
                                        + deliveryStatus
                        );

                        System.out.println(
                                "Recipient: "
                                        + recipientId
                        );

                        Object errors =
                                status.get(
                                        "errors"
                                );

                        if (
                                errors != null
                        ) {

                            System.out.println(
                                    "WhatsApp Errors: "
                                            + errors
                            );
                        }

                        Object conversation =
                                status.get(
                                        "conversation"
                                );

                        if (
                                conversation != null
                        ) {

                            System.out.println(
                                    "Conversation: "
                                            + conversation
                            );
                        }

                        Object pricing =
                                status.get(
                                        "pricing"
                                );

                        if (
                                pricing != null
                        ) {

                            System.out.println(
                                    "Pricing: "
                                            + pricing
                            );
                        }

                        System.out.println(
                                "================================="
                        );
                    }
                }
            }

            return ResponseEntity.ok(
                    "EVENT_RECEIVED"
            );

        } catch (
                Exception exception
        ) {

            exception.printStackTrace();

            return ResponseEntity.ok(
                    "EVENT_RECEIVED"
            );
        }
    }

    private String getString(
            Map<?, ?> map,
            String key
    ) {

        Object value =
                map.get(
                        key
                );

        return value == null
                ? null
                : value.toString();
    }
}