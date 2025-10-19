package com.portagecybertech.urlshortener.url_shortener.service;

import com.portagecybertech.urlshortener.url_shortener.model.UrlMapping;
import com.portagecybertech.urlshortener.url_shortener.repository.UrlMappingRepository;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class UrlService {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int MAX_SHORT_CODE_LENGTH = 10;

    private final UrlMappingRepository urlMappingRepository;

    public UrlService(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    public ShortenResponse shorten(String originalUrl) {
        if (!isValidHttpUrl(originalUrl)) {
            throw new IllegalArgumentException("Invalid URL");
        }

        Optional<UrlMapping> existing = urlMappingRepository.findByOriginalUrl(originalUrl);
        if (existing.isPresent()) {
            return new ShortenResponse(BASE_URL + "/" + existing.get().getShortCode());
        }

        int attempt = 0;
        while (true) {
            String candidate = generateShortCode(originalUrl, attempt);
            Optional<UrlMapping> existingCode = urlMappingRepository.findByShortCode(candidate);
            if (existingCode.isEmpty()) {
                UrlMapping saved = urlMappingRepository.save(new UrlMapping(candidate, originalUrl));
                return new ShortenResponse(BASE_URL + "/" + saved.getShortCode());
            }
            if (existingCode.get().getOriginalUrl().equals(originalUrl)) {
                // Same mapping already present (race condition scenario)
                return new ShortenResponse(BASE_URL + "/" + existingCode.get().getShortCode());
            }
            attempt++;
        }
    }

    public String expand(String shortCode) {
        return urlMappingRepository.findByShortCode(shortCode)
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> new IllegalArgumentException("Short code not found"));
    }

    public record ShortenResponse(String shortUrl) {}

    private boolean isValidHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private String generateShortCode(String input, int salt) {
        byte[] digest = md5(input + ":" + salt);
        String base62 = toBase62(digest);
        if (base62.length() <= MAX_SHORT_CODE_LENGTH) {
            return base62;
        }
        return base62.substring(0, MAX_SHORT_CODE_LENGTH);
    }

    private byte[] md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }

    private String toBase62(byte[] bytes) {
        // convert to positive BigInteger to avoid negative values
        BigInteger number = new BigInteger(1, bytes);
        if (number.equals(BigInteger.ZERO)) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        BigInteger base = BigInteger.valueOf(62);
        while (number.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divRem = number.divideAndRemainder(base);
            int index = divRem[1].intValue();
            sb.append(BASE62_ALPHABET.charAt(index));
            number = divRem[0];
        }
        return sb.reverse().toString();
    }
}


