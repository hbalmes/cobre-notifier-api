package com.cobre.notifier.api.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de RestTemplate para llamadas HTTP a webhooks.
 */
@Configuration
public class RestTemplateConfig {

    @Value("${app.webhook.timeout-ms:5000}")
    private int timeoutMs;

    @Value("${app.webhook.connect-timeout-ms:3000}")
    private int connectTimeoutMs;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(timeoutMs);
        
        return new RestTemplate(factory);
    }
}

