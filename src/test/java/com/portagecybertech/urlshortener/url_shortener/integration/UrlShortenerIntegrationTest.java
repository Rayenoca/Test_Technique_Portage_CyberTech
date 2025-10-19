package com.portagecybertech.urlshortener.url_shortener.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlShortenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completeWorkflow_shortenAndExpandAndRedirect() throws Exception {
        String originalUrl = "https://www.example.com/very/long/path?param=value";

        // 1. Raccourcir l'URL
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("originalUrl", originalUrl);

        String shortenResponse = mockMvc.perform(
                        post("/api/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").isNotEmpty())
                .andExpect(jsonPath("$.shortUrl", org.hamcrest.Matchers.startsWith("http://localhost:8080/")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String shortUrl = objectMapper.readTree(shortenResponse).get("shortUrl").asText();
        String shortCode = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        // 2. Récupérer l'URL originale via l'API
        mockMvc.perform(get("/api/expand/" + shortCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value(originalUrl));

        // 3. Tester la redirection directe
        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));
    }

    @Test
    void duplicateUrl_returnsSameShortCode() throws Exception {
        String originalUrl = "https://www.google.com";

        // Premier raccourcissement
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("originalUrl", originalUrl);

        String firstResponse = mockMvc.perform(
                        post("/api/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String firstShortUrl = objectMapper.readTree(firstResponse).get("shortUrl").asText();

        // Deuxième raccourcissement de la même URL
        String secondResponse = mockMvc.perform(
                        post("/api/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondShortUrl = objectMapper.readTree(secondResponse).get("shortUrl").asText();

        // Les deux URLs courtes doivent être identiques
        org.junit.jupiter.api.Assertions.assertEquals(firstShortUrl, secondShortUrl);
    }

    @Test
    void invalidUrl_returnsBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("originalUrl", "not-a-valid-url");

        mockMvc.perform(
                        post("/api/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonExistentShortCode_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/expand/nonexistent"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void homePage_loadsSuccessfully() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("URL Shortener")));
    }
}
