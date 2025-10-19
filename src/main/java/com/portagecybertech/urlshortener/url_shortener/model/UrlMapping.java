package com.portagecybertech.urlshortener.url_shortener.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "url_mapping",
    indexes = {
        @Index(name = "idx_url_mapping_shortcode", columnList = "short_code", unique = true),
        @Index(name = "idx_url_mapping_originalurl", columnList = "original_url", unique = true)
    }
)
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", length = 10, nullable = false, unique = true)
    private String shortCode;

    @Column(name = "original_url", length = 2048, nullable = false, unique = true)
    private String originalUrl;

    // Required by JPA
    protected UrlMapping() {
    }

    public UrlMapping(String shortCode, String originalUrl) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
    }

    public Long getId() {
        return id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }
}


