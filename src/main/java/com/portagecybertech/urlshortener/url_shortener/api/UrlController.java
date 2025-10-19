package com.portagecybertech.urlshortener.url_shortener.api;

import com.portagecybertech.urlshortener.url_shortener.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour les opérations d'API sur les URLs raccourcies.
 * 
 * <p>Ce contrôleur expose les endpoints REST pour :
 * <ul>
 *   <li><strong>Raccourcissement d'URL</strong> : POST /api/shorten</li>
 *   <li><strong>Expansion d'URL</strong> : GET /api/expand/{shortCode}</li>
 * </ul>
 * 
 * <p><strong>Endpoints disponibles :</strong>
 * <ul>
 *   <li>{@code POST /api/shorten} - Raccourcit une URL originale</li>
 *   <li>{@code GET /api/expand/{shortCode} - Récupère l'URL originale à partir du code court</li>
 * </ul>
 * 
 * <p><strong>Gestion d'erreurs :</strong>
 * <ul>
 *   <li>400 Bad Request : URL invalide fournie</li>
 *   <li>404 Not Found : Code court non trouvé</li>
 * </ul>
 * 
 * @author Aziz Rayene Delaa
 * @version 1.0
 * @since 1.0
 * @see UrlService
 */
@RestController
@RequestMapping("/api")
public class UrlController {

    /**
     * Service pour la logique métier des URLs raccourcies.
     */
    private final UrlService urlService;

    /**
     * Constructeur principal du contrôleur.
     * 
     * @param urlService le service injecté pour la gestion des URLs
     */
    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    /**
     * Record représentant une requête de raccourcissement d'URL.
     * 
     * @param originalUrl l'URL originale à raccourcir
     */
    public record ShortenRequest(String originalUrl) {}

    /**
     * Raccourcit une URL originale en générant un code court unique.
     * 
     * <p><strong>Endpoint :</strong> {@code POST /api/shorten}
     * 
     * <p><strong>Corps de la requête :</strong>
     * <pre>{@code
     * {
     *   "originalUrl": "https://example.com/very/long/url"
     * }
     * }</pre>
     * 
     * <p><strong>Réponse en cas de succès (200) :</strong>
     * <pre>{@code
     * {
     *   "shortUrl": "http://localhost:8080/abc123"
     * }
     * }</pre>
     * 
     * <p><strong>Réponse en cas d'erreur (400) :</strong>
     * <pre>{@code "Invalid URL"}</pre>
     * 
     * @param req la requête contenant l'URL originale à raccourcir
     * @return ResponseEntity avec l'URL raccourcie ou un message d'erreur
     */
    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(@RequestBody ShortenRequest req) {
        try {
            UrlService.ShortenResponse result = urlService.shorten(req.originalUrl());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Récupère l'URL originale à partir d'un code court.
     * 
     * <p><strong>Endpoint :</strong> {@code GET /api/expand/{shortCode}}
     * 
     * <p><strong>Paramètre de chemin :</strong>
     * <ul>
     *   <li>{@code shortCode} - Le code court à rechercher</li>
     * </ul>
     * 
     * <p><strong>Réponse en cas de succès (200) :</strong>
     * <pre>{@code
     * {
     *   "originalUrl": "https://example.com/very/long/url"
     * }
     * }</pre>
     * 
     * <p><strong>Réponse en cas d'erreur (404) :</strong>
     * <p>Code court non trouvé dans la base de données.
     * 
     * @param shortCode le code court à rechercher
     * @return ResponseEntity avec l'URL originale ou 404 si non trouvé
     */
    @GetMapping("/expand/{shortCode}")
    public ResponseEntity<?> expand(@PathVariable String shortCode) {
        try {
            String originalUrl = urlService.expand(shortCode);
            return ResponseEntity.ok().body(new ExpandResponse(originalUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Record représentant la réponse d'expansion d'URL.
     * 
     * @param originalUrl l'URL originale récupérée
     */
    public record ExpandResponse(String originalUrl) {}
}


