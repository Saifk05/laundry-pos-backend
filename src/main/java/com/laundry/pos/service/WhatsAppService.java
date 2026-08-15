package com.laundry.pos.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class WhatsAppService {

    private final RestClient whatsappRestClient;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${whatsapp.api-url}")
    private String apiUrl;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    public WhatsAppService(
            RestClient whatsappRestClient,
            ObjectMapper objectMapper
    ) {
        this.whatsappRestClient =
                whatsappRestClient;

        this.objectMapper =
                objectMapper;

        this.restClient =
                RestClient.create();
    }

    public String sendTextMessage(
            String mobile,
            String message
    ) {

        String formattedMobile =
                formatMobile(
                        mobile
                );

        System.out.println();
        System.out.println("========================================");
        System.out.println("WHATSAPP TEXT MESSAGE REQUEST");
        System.out.println("Original Mobile: " + mobile);
        System.out.println("Formatted Mobile: " + formattedMobile);
        System.out.println("Message: " + message);
        System.out.println("========================================");

        Map<String, Object> text =
                Map.of(
                        "preview_url",
                        false,
                        "body",
                        message
                );

        Map<String, Object> requestBody =
                Map.of(
                        "messaging_product",
                        "whatsapp",
                        "recipient_type",
                        "individual",
                        "to",
                        formattedMobile,
                        "type",
                        "text",
                        "text",
                        text
                );

        System.out.println(
                "Request Body: "
                        + requestBody
        );

        try {

            String response =
                    whatsappRestClient
                            .post()
                            .uri("/messages")
                            .body(
                                    requestBody
                            )
                            .retrieve()
                            .body(
                                    String.class
                            );

            System.out.println();
            System.out.println("========================================");
            System.out.println("WHATSAPP TEXT MESSAGE ACCEPTED BY META");
            System.out.println("Recipient: " + formattedMobile);
            System.out.println("Meta Response: " + response);
            System.out.println("========================================");
            System.out.println();

            return response;

        } catch (Exception exception) {

            System.out.println();
            System.out.println("========================================");
            System.out.println("WHATSAPP TEXT MESSAGE FAILED");
            System.out.println("Recipient: " + formattedMobile);
            System.out.println("Error: " + exception.getMessage());
            System.out.println("========================================");
            System.out.println();

            exception.printStackTrace();

            throw exception;
        }
    }

    public String sendTemplate(
            String mobile,
            String templateName,
            String languageCode,
            List<String> parameters
    ) {

        String formattedMobile =
                formatMobile(
                        mobile
                );

        System.out.println();
        System.out.println("========================================");
        System.out.println("WHATSAPP TEMPLATE MESSAGE REQUEST");
        System.out.println("Original Mobile: " + mobile);
        System.out.println("Formatted Mobile: " + formattedMobile);
        System.out.println("Template: " + templateName);
        System.out.println("Language: " + languageCode);
        System.out.println("Parameters: " + parameters);
        System.out.println("========================================");

        List<Map<String, Object>> templateParameters =
                parameters
                        .stream()
                        .map(value ->
                                Map.<String, Object>of(
                                        "type",
                                        "text",
                                        "text",
                                        value
                                )
                        )
                        .toList();

        Map<String, Object> bodyComponent =
                Map.of(
                        "type",
                        "body",
                        "parameters",
                        templateParameters
                );

        Map<String, Object> template =
                Map.of(
                        "name",
                        templateName,
                        "language",
                        Map.of(
                                "code",
                                languageCode
                        ),
                        "components",
                        List.of(
                                bodyComponent
                        )
                );

        Map<String, Object> requestBody =
                Map.of(
                        "messaging_product",
                        "whatsapp",
                        "recipient_type",
                        "individual",
                        "to",
                        formattedMobile,
                        "type",
                        "template",
                        "template",
                        template
                );

        try {

            String response =
                    whatsappRestClient
                            .post()
                            .uri("/messages")
                            .body(
                                    requestBody
                            )
                            .retrieve()
                            .body(
                                    String.class
                            );

            System.out.println();
            System.out.println("========================================");
            System.out.println("WHATSAPP TEMPLATE ACCEPTED BY META");
            System.out.println("Template: " + templateName);
            System.out.println("Recipient: " + formattedMobile);
            System.out.println("Meta Response: " + response);
            System.out.println("========================================");
            System.out.println();

            return response;

        } catch (Exception exception) {

            System.out.println();
            System.out.println("========================================");
            System.out.println("WHATSAPP TEMPLATE FAILED");
            System.out.println("Template: " + templateName);
            System.out.println("Recipient: " + formattedMobile);
            System.out.println("Error: " + exception.getMessage());
            System.out.println("========================================");
            System.out.println();

            exception.printStackTrace();

            throw exception;
        }
    }

    public String sendProcessingMessage(
            String mobile,
            String customerName,
            String orderNumber
    ) {

        return sendTextMessage(
                mobile,
                "Hi "
                        + customerName
                        + ", your order "
                        + orderNumber
                        + " is now being processed at our store."
        );
    }

    public String sendReadyMessage(
            String mobile,
            String customerName,
            String orderNumber
    ) {

        return sendTextMessage(
                mobile,
                "Hi "
                        + customerName
                        + ", your order "
                        + orderNumber
                        + " is ready for pickup."
        );
    }

    public String sendDeliveredMessage(
            String mobile,
            String customerName,
            String orderNumber
    ) {

        return sendTextMessage(
                mobile,
                "Hi "
                        + customerName
                        + ", your order "
                        + orderNumber
                        + " has been delivered successfully. Thank you for choosing Venkateshwara Fabric Works."
        );
    }

    public String sendCancelledMessage(
            String mobile,
            String customerName,
            String orderNumber
    ) {

        return sendTextMessage(
                mobile,
                "Hi "
                        + customerName
                        + ", your order "
                        + orderNumber
                        + " has been cancelled. Please contact Venkateshwara Fabric Works if you need assistance."
        );
    }

    public String sendReceipt(
            String mobile,
            byte[] pdfBytes,
            String orderNumber
    ) {

        System.out.println();
        System.out.println("========================================");
        System.out.println("WHATSAPP RECEIPT PROCESS STARTED");
        System.out.println("Mobile: " + mobile);
        System.out.println("Order Number: " + orderNumber);
        System.out.println("PDF Size: " + pdfBytes.length + " bytes");
        System.out.println("========================================");

        String mediaId =
                uploadReceipt(
                        pdfBytes,
                        orderNumber
                );

        return sendDocument(
                mobile,
                mediaId,
                orderNumber
        );
    }

    private String uploadReceipt(
            byte[] pdfBytes,
            String orderNumber
    ) {

        System.out.println();
        System.out.println("========================================");
        System.out.println("WHATSAPP MEDIA UPLOAD STARTED");
        System.out.println("Order Number: " + orderNumber);
        System.out.println("PDF Size: " + pdfBytes.length + " bytes");
        System.out.println("Phone Number ID: " + phoneNumberId);
        System.out.println("========================================");

        ByteArrayResource fileResource =
                new ByteArrayResource(
                        pdfBytes
                ) {

                    @Override
                    public String getFilename() {

                        return orderNumber
                                + "-receipt.pdf";
                    }
                };

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        body.add(
                "messaging_product",
                "whatsapp"
        );

        body.add(
                "type",
                "application/pdf"
        );

        body.add(
                "file",
                fileResource
        );

        try {

            String response =
                    restClient
                            .post()
                            .uri(
                                    apiUrl
                                            + "/"
                                            + phoneNumberId
                                            + "/media"
                            )
                            .header(
                                    "Authorization",
                                    "Bearer "
                                            + accessToken
                            )
                            .contentType(
                                    MediaType.MULTIPART_FORM_DATA
                            )
                            .body(
                                    body
                            )
                            .retrieve()
                            .body(
                                    String.class
                            );

            System.out.println();
            System.out.println("========================================");
            System.out.println("WHATSAPP MEDIA UPLOAD RESPONSE");
            System.out.println("Meta Response: " + response);
            System.out.println("========================================");

            JsonNode json =
                    objectMapper
                            .readTree(
                                    response
                            );

            JsonNode id =
                    json.get(
                            "id"
                    );

            if (
                    id == null ||
                    id.asText()
                            .isBlank()
            ) {

                throw new RuntimeException(
                        "WhatsApp media ID not returned"
                );
            }

            String mediaId =
                    id.asText();

            System.out.println(
                    "WhatsApp receipt uploaded successfully"
            );

            System.out.println(
                    "Media ID: "
                            + mediaId
            );

            System.out.println(
                    "========================================"
            );

            System.out.println();

            return mediaId;

        } catch (Exception exception) {

            System.out.println();
            System.out.println("========================================");
            System.out.println("WHATSAPP MEDIA UPLOAD FAILED");
            System.out.println("Order Number: " + orderNumber);
            System.out.println("Error: " + exception.getMessage());
            System.out.println("========================================");
            System.out.println();

            exception.printStackTrace();

            throw new RuntimeException(
                    "Failed to upload WhatsApp receipt",
                    exception
            );
        }
    }

    private String sendDocument(
            String mobile,
            String mediaId,
            String orderNumber
    ) {

        String formattedMobile =
                formatMobile(
                        mobile
                );

        System.out.println();
        System.out.println("========================================");
        System.out.println("WHATSAPP DOCUMENT MESSAGE REQUEST");
        System.out.println("Original Mobile: " + mobile);
        System.out.println("Formatted Mobile: " + formattedMobile);
        System.out.println("Order Number: " + orderNumber);
        System.out.println("Media ID: " + mediaId);
        System.out.println("========================================");

        Map<String, Object> document =
                Map.of(
                        "id",
                        mediaId,
                        "filename",
                        orderNumber
                                + "-receipt.pdf",
                        "caption",
                        "Receipt for order "
                                + orderNumber
                );

        Map<String, Object> requestBody =
                Map.of(
                        "messaging_product",
                        "whatsapp",
                        "recipient_type",
                        "individual",
                        "to",
                        formattedMobile,
                        "type",
                        "document",
                        "document",
                        document
                );

        try {

            String response =
                    whatsappRestClient
                            .post()
                            .uri("/messages")
                            .body(
                                    requestBody
                            )
                            .retrieve()
                            .body(
                                    String.class
                            );

            System.out.println();
            System.out.println("========================================");
            System.out.println("WHATSAPP DOCUMENT ACCEPTED BY META");
            System.out.println("Recipient: " + formattedMobile);
            System.out.println("Order Number: " + orderNumber);
            System.out.println("Media ID: " + mediaId);
            System.out.println("Meta Response: " + response);
            System.out.println("========================================");
            System.out.println();

            return response;

        } catch (Exception exception) {

            System.out.println();
            System.out.println("========================================");
            System.out.println("WHATSAPP DOCUMENT MESSAGE FAILED");
            System.out.println("Recipient: " + formattedMobile);
            System.out.println("Order Number: " + orderNumber);
            System.out.println("Media ID: " + mediaId);
            System.out.println("Error: " + exception.getMessage());
            System.out.println("========================================");
            System.out.println();

            exception.printStackTrace();

            throw exception;
        }
    }

    private String formatMobile(
            String mobile
    ) {

        if (
                mobile == null ||
                mobile.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Mobile number is required"
            );
        }

        String number =
                mobile.replaceAll(
                        "[^0-9]",
                        ""
                );

        if (
                number.length()
                        == 10
        ) {

            number =
                    "91"
                            + number;
        }

        return number;
    }
}