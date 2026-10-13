package com.ignou.smartmedical.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Periodically calls the application's own public health endpoint.
 *
 * On free hosting plans the web service sleeps after a period without inbound
 * requests and the free database powers off after a period without queries.
 * Requesting the public URL counts as inbound traffic and the health endpoint
 * runs a query, so both stay active. Disabled when no URL is configured
 * (for example during local runs and tests).
 */
@Component
public class KeepAliveScheduler {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveScheduler.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${keepalive.url:}")
    private String baseUrl;

    @Scheduled(initialDelayString = "${keepalive.interval-ms:600000}",
               fixedDelayString = "${keepalive.interval-ms:600000}")
    public void ping() {
        if (baseUrl == null || baseUrl.isBlank()) {
            return;
        }
        String url = baseUrl.replaceAll("/+$", "") + "/api/health";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            log.info("Keep-alive ping {} -> HTTP {}", url, response.statusCode());
        } catch (IOException ex) {
            log.warn("Keep-alive ping {} failed: {}", url, ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
