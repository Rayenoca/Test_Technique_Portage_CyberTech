package com.portagecybertech.urlshortener.url_shortener.api;

import com.portagecybertech.urlshortener.url_shortener.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    public record ShortenRequest(String originalUrl) {}

    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(@RequestBody ShortenRequest req) {
        try {
            UrlService.ShortenResponse result = urlService.shorten(req.originalUrl());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}


