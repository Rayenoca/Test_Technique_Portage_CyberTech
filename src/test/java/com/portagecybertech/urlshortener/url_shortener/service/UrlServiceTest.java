package com.portagecybertech.urlshortener.url_shortener.service;

import com.portagecybertech.urlshortener.url_shortener.model.UrlMapping;
import com.portagecybertech.urlshortener.url_shortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    private UrlService urlService;

    @BeforeEach
    void setup() {
        // Créer le service avec une valeur de base URL pour les tests
        urlService = new UrlService(urlMappingRepository, "http://localhost:8080");
    }

    @Test
    void shorten_sameInput_returnsSameShortCode() {
        String original = "https://ex.com";
        String shortCode = "abc123";
        UrlMapping mapping = new UrlMapping(shortCode, original);

        when(urlMappingRepository.findByOriginalUrl(eq(original)))
                .thenReturn(Optional.of(mapping));

        String first = urlService.shorten(original).shortUrl();
        String second = urlService.shorten(original).shortUrl();

        assertEquals("http://localhost:8080/" + shortCode, first);
        assertEquals("http://localhost:8080/" + shortCode, second);
    }

    @Test
    void shorten_rejectsInvalidUrl() {
        assertThrows(IllegalArgumentException.class, () -> urlService.shorten("bad-url"));
    }

    @Test
    void expand_returnsOriginal() {
        String original = "https://ex.com";
        String shortCode = "abc123";
        UrlMapping mapping = new UrlMapping(shortCode, original);

        when(urlMappingRepository.findByShortCode(eq(shortCode)))
                .thenReturn(Optional.of(mapping));

        String result = urlService.expand(shortCode);
        assertEquals(original, result);
    }

    @Test
    void expand_throwsException_whenShortCodeNotFound() {
        String shortCode = "nonexistent";

        when(urlMappingRepository.findByShortCode(eq(shortCode)))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> urlService.expand(shortCode));
    }

    @Test
    void shorten_createsNewMapping_whenUrlNotExists() {
        String original = "https://example.com";
        UrlMapping mapping = new UrlMapping("xyz789", original);

        when(urlMappingRepository.findByOriginalUrl(eq(original)))
                .thenReturn(Optional.empty());
        when(urlMappingRepository.findByShortCode(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());
        when(urlMappingRepository.save(org.mockito.ArgumentMatchers.any(UrlMapping.class)))
                .thenReturn(mapping);

        UrlService.ShortenResponse result = urlService.shorten(original);
        assertEquals("http://localhost:8080/xyz789", result.shortUrl());
    }

    @Test
    void shorten_handlesCollision() {
        String original = "https://example.com";
        String shortCode1 = "abc123";
        String shortCode2 = "def456";
        UrlMapping mapping1 = new UrlMapping(shortCode1, "https://other.com");
        UrlMapping mapping2 = new UrlMapping(shortCode2, original);

        when(urlMappingRepository.findByOriginalUrl(eq(original)))
                .thenReturn(Optional.empty());
        when(urlMappingRepository.findByShortCode(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(mapping1))
                .thenReturn(Optional.empty());
        when(urlMappingRepository.save(org.mockito.ArgumentMatchers.any(UrlMapping.class)))
                .thenReturn(mapping2);

        UrlService.ShortenResponse result = urlService.shorten(original);
        assertEquals("http://localhost:8080/" + shortCode2, result.shortUrl());
    }

    @Test
    void shorten_rejectsNullUrl() {
        assertThrows(IllegalArgumentException.class, () -> urlService.shorten(null));
    }

    @Test
    void shorten_rejectsEmptyUrl() {
        assertThrows(IllegalArgumentException.class, () -> urlService.shorten(""));
    }

    @Test
    void shorten_rejectsNonHttpUrl() {
        assertThrows(IllegalArgumentException.class, () -> urlService.shorten("ftp://example.com"));
    }

    @Test
    void shorten_acceptsHttpsUrl() {
        String original = "https://secure.example.com";
        String shortCode = "secure123";
        UrlMapping mapping = new UrlMapping(shortCode, original);

        when(urlMappingRepository.findByOriginalUrl(eq(original)))
                .thenReturn(Optional.of(mapping));

        UrlService.ShortenResponse result = urlService.shorten(original);
        assertEquals("http://localhost:8080/" + shortCode, result.shortUrl());
    }
}


