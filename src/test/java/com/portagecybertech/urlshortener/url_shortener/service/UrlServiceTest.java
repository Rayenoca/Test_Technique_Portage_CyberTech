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

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setup() {
        // no-op setup; using @InjectMocks to construct service
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
}


