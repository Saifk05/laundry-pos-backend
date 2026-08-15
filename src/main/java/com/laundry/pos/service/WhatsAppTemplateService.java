package com.laundry.pos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class WhatsAppTemplateService {

    private final RestClient restClient;

    @Value("${whatsapp.api-url}")
    private String apiUrl;

    @Value("${whatsapp.business-account-id}")
    private String businessAccountId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    public WhatsAppTemplateService() {
        this.restClient =
                RestClient.create();
    }

    public String getTemplates() {

        return restClient
                .get()
                .uri(
                        apiUrl
                                + "/"
                                + businessAccountId
                                + "/message_templates"
                )
                .header(
                        "Authorization",
                        "Bearer " + accessToken
                )
                .retrieve()
                .body(
                        String.class
                );
    }

    public String createProcessingTemplate() {

        Map<String, Object> bodyComponent =
                Map.of(
                        "type",
                        "BODY",

                        "text",
                        "Hi {{1}}, your laundry order {{2}} is now being processed at our store.\n\nWe will notify you once it is ready.",

                        "example",
                        Map.of(
                                "body_text",
                                List.of(
                                        List.of(
                                                "Saifali",
                                                "LAUNDRY-0017"
                                        )
                                )
                        )
                );

        Map<String, Object> requestBody =
                Map.of(
                        "name",
                        "laundry_processing",

                        "language",
                        "en_US",

                        "category",
                        "UTILITY",

                        "components",
                        List.of(
                                bodyComponent
                        )
                );

        return createTemplate(
                requestBody
        );
    }

    public String createReadyTemplate() {

        Map<String, Object> bodyComponent =
                Map.of(
                        "type",
                        "BODY",

                        "text",
                        "Hi {{1}}, your laundry order {{2}} is ready for pickup.\n\nThank you.",

                        "example",
                        Map.of(
                                "body_text",
                                List.of(
                                        List.of(
                                                "Saifali",
                                                "LAUNDRY-0017"
                                        )
                                )
                        )
                );

        Map<String, Object> requestBody =
                Map.of(
                        "name",
                        "laundry_ready",

                        "language",
                        "en_US",

                        "category",
                        "UTILITY",

                        "components",
                        List.of(
                                bodyComponent
                        )
                );

        return createTemplate(
                requestBody
        );
    }

    public String createDeliveredTemplate() {

        Map<String, Object> bodyComponent =
                Map.of(
                        "type",
                        "BODY",

                        "text",
                        "Hi {{1}}, your laundry order {{2}} has been delivered successfully.\n\nThank you for choosing us.",

                        "example",
                        Map.of(
                                "body_text",
                                List.of(
                                        List.of(
                                                "Saifali",
                                                "LAUNDRY-0017"
                                        )
                                )
                        )
                );

        Map<String, Object> requestBody =
                Map.of(
                        "name",
                        "laundry_delivered",

                        "language",
                        "en_US",

                        "category",
                        "UTILITY",

                        "components",
                        List.of(
                                bodyComponent
                        )
                );

        return createTemplate(
                requestBody
        );
    }

    public String createCancelledTemplate() {

        Map<String, Object> bodyComponent =
                Map.of(
                        "type",
                        "BODY",

                        "text",
                        "Hi {{1}}, your laundry order {{2}} has been cancelled.\n\nPlease contact us if you need assistance.",

                        "example",
                        Map.of(
                                "body_text",
                                List.of(
                                        List.of(
                                                "Saifali",
                                                "LAUNDRY-0017"
                                        )
                                )
                        )
                );

        Map<String, Object> requestBody =
                Map.of(
                        "name",
                        "laundry_cancelled",

                        "language",
                        "en_US",

                        "category",
                        "UTILITY",

                        "components",
                        List.of(
                                bodyComponent
                        )
                );

        return createTemplate(
                requestBody
        );
    }

    private String createTemplate(
            Map<String, Object> requestBody
    ) {

        return restClient
                .post()
                .uri(
                        apiUrl
                                + "/"
                                + businessAccountId
                                + "/message_templates"
                )
                .header(
                        "Authorization",
                        "Bearer " + accessToken
                )
                .header(
                        "Content-Type",
                        "application/json"
                )
                .body(
                        requestBody
                )
                .retrieve()
                .body(
                        String.class
                );
    }
}