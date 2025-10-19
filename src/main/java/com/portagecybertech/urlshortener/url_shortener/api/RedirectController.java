package com.portagecybertech.urlshortener.url_shortener.api;

import com.portagecybertech.urlshortener.url_shortener.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur pour la redirection automatique des URLs raccourcies.
 * 
 * <p>Ce contrôleur gère la redirection HTTP des codes courts vers leurs URLs originales.
 * Il intercepte les requêtes sur les URLs courtes et effectue une redirection 302
 * vers l'URL originale correspondante.
 * 
 * <p><strong>Endpoint :</strong>
 * <ul>
 *   <li>{@code GET /{shortCode}} - Redirige vers l'URL originale</li>
 * </ul>
 * 
 * <p><strong>Comportement :</strong>
 * <ul>
 *   <li><strong>Succès (302)</strong> : Redirection vers l'URL originale</li>
 *   <li><strong>Erreur (404)</strong> : Code court non trouvé</li>
 * </ul>
 * 
 * <p><strong>Exemple d'utilisation :</strong>
 * <pre>
 * GET http://localhost:8080/abc123
 * → 302 Redirect vers https://example.com/very/long/url
 * </pre>
 * 
 * @author Aziz Rayene Delaa
 * @version 1.0
 * @since 1.0
 * @see UrlService
 */
@RestController
public class RedirectController {

    /**
     * Service pour la logique métier des URLs raccourcies.
     */
    private final UrlService urlService;

    /**
     * Constructeur principal du contrôleur.
     * 
     * @param urlService le service injecté pour la gestion des URLs
     */
    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
    }

    /**
     * Redirige un code court vers son URL originale.
     * 
     * <p><strong>Endpoint :</strong> {@code GET /{shortCode}}
     * 
     * <p><strong>Paramètre de chemin :</strong>
     * <ul>
     *   <li>{@code shortCode} - Le code court à rediriger</li>
     * </ul>
     * 
     * <p><strong>Réponse en cas de succès (302) :</strong>
     * <p>Redirection HTTP avec l'en-tête {@code Location} contenant l'URL originale.
     * 
     * <p><strong>Réponse en cas d'erreur (404) :</strong>
     * <p>Code court non trouvé dans la base de données.
     * 
     * @param shortCode le code court à rediriger
     * @return ResponseEntity avec redirection 302 ou 404 si non trouvé
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        try {
            String originalUrl = urlService.expand(shortCode);
            return ResponseEntity.status(302)
                    .header("Location", originalUrl)
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}


