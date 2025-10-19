package com.portagecybertech.urlshortener.url_shortener.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void redirect_returns302_whenValidShortCode() throws Exception {
        // D'abord créer une URL courte
        Map<String, String> body = new HashMap<>();
        body.put("originalUrl", "https://www.google.com");
        
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

        // Tester la redirection
        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.google.com"));
    }

    @Test
    void redirect_returns404_whenShortCodeNotFound() throws Exception {
        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirect_returns404_whenEmptyShortCode() throws Exception {
        // Le test de la racine "/" retourne 200 car c'est géré par HomeController
        // Testons plutôt avec un code court vide
        mockMvc.perform(get("/ "))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirect_handlesSpecialCharacters() throws Exception {
        mockMvc.perform(get("/test%20code"))
                .andExpect(status().isNotFound());
    }
}
