package com.portagecybertech.urlshortener.url_shortener.service;

import com.portagecybertech.urlshortener.url_shortener.model.UrlMapping;
import com.portagecybertech.urlshortener.url_shortener.repository.UrlMappingRepository;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class UrlService {

    private static final String BASE_URL = "http://localhost:8080/";

    private final UrlMappingRepository urlMappingRepository;

    public UrlService(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    public String shorten(String originalUrl) {
        if (!isValidHttpUrl(originalUrl)) {
            throw new IllegalArgumentException("Invalid URL");
        }

        Optional<UrlMapping> existing = urlMappingRepository.findByOriginalUrl(originalUrl);
        if (existing.isPresent()) {
            return BASE_URL + existing.get().getShortCode();
        }

        throw new UnsupportedOperationException("Short code generation not implemented");
    }

    public String expand(String shortCode) {
        return urlMappingRepository.findByShortCode(shortCode)
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> new IllegalArgumentException("Short code not found"));
    }

    private boolean isValidHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}


