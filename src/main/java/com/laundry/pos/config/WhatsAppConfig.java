package com.laundry.pos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WhatsAppConfig {

    @Value("${whatsapp.api-url}")
    private String apiUrl;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Bean
    public RestClient whatsappRestClient() {
        return RestClient.builder()
                .baseUrl(apiUrl + "/" + phoneNumberId)
                .defaultHeader(
                        "Authorization",
                        "Bearer " + accessToken
                )
                .defaultHeader(
                        "Content-Type",
                        "application/json"
                )
                .build();
    }
}