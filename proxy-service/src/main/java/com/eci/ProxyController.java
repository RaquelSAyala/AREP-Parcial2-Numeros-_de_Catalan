package com.eci;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class ProxyController {

    private final List<String> mathServiceUrls = new ArrayList<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    public ProxyController(
            @Value("${MATH_SERVICE_URL_1:}") String url1,
            @Value("${MATH_SERVICE_URL_2:}") String url2
    ) {
        if (url1 != null && !url1.isBlank()) {
            mathServiceUrls.add(url1.trim());
        }
        if (url2 != null && !url2.isBlank()) {
            mathServiceUrls.add(url2.trim());
        }
    }

    @GetMapping("/catalan")
    public ResponseEntity<String> catalan(@RequestParam(name = "value") int value) {
        if (mathServiceUrls.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No math-service URLs configured in environment variables");
        }

        int attempts = 0;
        int size = mathServiceUrls.size();

        while (attempts < size) {
            int index = Math.floorMod(counter.getAndIncrement(), size);
            String baseUrl = mathServiceUrls.get(index);
            String target = baseUrl + "/catalan?value=" + value;

            try {
                String response = doGet(target);
                return ResponseEntity.ok(response);
            } catch (IOException ex) {
                attempts++;
                if (attempts >= size) {
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body("All math-service instances are unavailable");
                }
            }
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("All math-service instances are unavailable");
    }

    private String doGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            throw new IOException("GET request to math-service returned code " + responseCode);
        }
    }
}
