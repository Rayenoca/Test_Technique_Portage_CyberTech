package com.portagecybertech.urlshortener.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.portagecybertech.urlshortener.url_shortener.UrlShortenerApplication;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = UrlShortenerApplication.class)
@AutoConfigureMockMvc
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shorten_returnsShortUrl_whenValidOriginalUrl() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("originalUrl", "https://www.google.com");

        mockMvc.perform(
                        post("/api/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").isNotEmpty())
                .andExpect(jsonPath("$.shortUrl", Matchers.matchesPattern("http://localhost:8080/[A-Za-z0-9]{1,10}")));
    }

    @Test
    void shorten_returns400_whenInvalidUrl() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("originalUrl", "bad-url");

        mockMvc.perform(
                        post("/api/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shorten_returns400_whenEmptyUrl() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("originalUrl", "");

        mockMvc.perform(
                        post("/api/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shorten_returns400_whenNullUrl() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("originalUrl", null);

        mockMvc.perform(
                        post("/api/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void expand_returns404_whenShortCodeNotFound() throws Exception {
        mockMvc.perform(get("/api/expand/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void expand_returnsOriginalUrl_whenValidShortCode() throws Exception {
        // D'abord créer une URL courte
        Map<String, String> body = new HashMap<>();
        body.put("originalUrl", "https://www.example.com");
        
        String response = mockMvc.perform(
                        post("/api/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraire le code court de la réponse
        String shortUrl = objectMapper.readTree(response).get("shortUrl").asText();
        String shortCode = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        // Tester l'expansion
        mockMvc.perform(get("/api/expand/" + shortCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value("https://www.example.com"));
    }

    @Test
    void expand_handlesSpecialCharactersInShortCode() throws Exception {
        mockMvc.perform(get("/api/expand/test%20code"))
                .andExpect(status().isNotFound());
    }
}


