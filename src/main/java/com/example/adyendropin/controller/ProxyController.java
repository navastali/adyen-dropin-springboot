package com.example.adyendropin.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

@RestController
public class ProxyController {

    @Value("${ADYEN_API_KEY:}")
    private String adyenApiKey;

    @Value("${ADYEN_MERCHANT_ACCOUNT:}")
    private String merchantAccount;

    @Value("${ADYEN_ENVIRONMENT:test}")
    private String adyenEnvironment;

    private String checkoutBase() {
        return adyenEnvironment.equalsIgnoreCase("live")
                ? "https://checkout-live.adyen.com"
                : "https://checkout-test.adyen.com";
    }

    private String proxyToAdyen(String path, String body, String method) throws IOException {
        String endpoint = checkoutBase() + path;
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        if (adyenApiKey != null && !adyenApiKey.isEmpty()) {
            conn.setRequestProperty("X-API-Key", adyenApiKey);
        }
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }
        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 400) ? conn.getInputStream() : conn.getErrorStream();
        String response;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            response = br.lines().collect(Collectors.joining("\n"));
        }
        conn.disconnect();
        return response;
    }

    @PostMapping("/api/paymentMethods")
    public String paymentMethods(HttpServletRequest request) throws IOException {
        String body = request.getReader().lines().collect(Collectors.joining("\n"));
        if (body == null || body.trim().isEmpty()) {
            // Build minimal request
            body = String.format("{\n  \"merchantAccount\": \"%s\",\n  \"channel\": \"Web\"\n}", merchantAccount);
        }
        return proxyToAdyen("/v71/paymentMethods", body, "POST");
    }

    @PostMapping("/api/payments")
    public String payments(HttpServletRequest request) throws IOException {
        String body = request.getReader().lines().collect(Collectors.joining("\n"));
        // If merchantAccount not present, inject it
        if (!body.contains("merchantAccount") && merchantAccount != null && !merchantAccount.isEmpty()) {
            // naive injection: insert before last }
            body = body.trim();
            if (body.endsWith("}")) {
                body = body.substring(0, body.length()-1) + ", \"merchantAccount\": \"" + merchantAccount + "\"}";
            }
        }
        return proxyToAdyen("/v71/payments", body, "POST");
    }

    @PostMapping("/api/details")
    public String details(HttpServletRequest request) throws IOException {
        String body = request.getReader().lines().collect(Collectors.joining("\n"));
        return proxyToAdyen("/v71/payments/details", body, "POST");
    }
}