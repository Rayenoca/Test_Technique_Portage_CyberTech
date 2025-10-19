package com.portagecybertech.urlshortener.url_shortener.service;

import com.portagecybertech.urlshortener.url_shortener.model.UrlMapping;
import com.portagecybertech.urlshortener.url_shortener.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Service principal pour la gestion des URLs raccourcies.
 * 
 * <p>Ce service fournit les fonctionnalités principales du raccourcissement d'URL :
 * <ul>
 *   <li><strong>Raccourcissement</strong> : Convertit une URL longue en URL courte</li>
 *   <li><strong>Expansion</strong> : Récupère l'URL originale à partir du code court</li>
 *   <li><strong>Gestion des collisions</strong> : Assure l'unicité des codes courts</li>
 *   <li><strong>Validation</strong> : Vérifie la validité des URLs</li>
 * </ul>
 * 
 * <p><strong>Algorithme de raccourcissement :</strong>
 * <ol>
 *   <li>Validation de l'URL originale (HTTP/HTTPS)</li>
 *   <li>Vérification si l'URL existe déjà</li>
 *   <li>Génération d'un hash MD5 de l'URL</li>
 *   <li>Conversion en Base62 pour obtenir un code court</li>
 *   <li>Gestion des collisions potentielles</li>
 * </ol>
 * 
 * <p><strong>Configuration :</strong>
 * Le service utilise la propriété {@code app.base-url} pour construire les URLs raccourcies.
 * 
 * @author Aziz Rayene Delaa
 * @version 1.0
 * @since 1.0
 * @see UrlMapping
 * @see UrlMappingRepository
 */
@Service
public class UrlService {

    /**
     * Alphabet Base62 utilisé pour la génération des codes courts.
     * 
     * <p>Contient les caractères : 0-9, A-Z, a-z (62 caractères au total).
     * Cet alphabet permet de générer des codes courts lisibles et URL-safe.
     */
    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    
    /**
     * Longueur maximale autorisée pour un code court.
     * 
     * <p>Cette limite garantit que les URLs raccourcies restent courtes
     * tout en offrant suffisamment de combinaisons possibles.
     */
    private static final int MAX_SHORT_CODE_LENGTH = 10;

    /**
     * Repository pour l'accès aux données des mappings d'URLs.
     */
    private final UrlMappingRepository urlMappingRepository;
    
    /**
     * URL de base configurée pour construire les URLs raccourcies.
     * 
     * <p>Cette valeur est injectée depuis la propriété {@code app.base-url}
     * du fichier de configuration.
     */
    private final String baseUrl;

    /**
     * Constructeur principal du service.
     * 
     * @param urlMappingRepository le repository pour l'accès aux données
     * @param baseUrl l'URL de base configurée (injectée depuis app.base-url)
     */
    public UrlService(UrlMappingRepository urlMappingRepository, 
                     @Value("${app.base-url}") String baseUrl) {
        this.urlMappingRepository = urlMappingRepository;
        this.baseUrl = baseUrl;
    }

    /**
     * Raccourcit une URL originale en générant un code court unique.
     * 
     * <p><strong>Processus :</strong>
     * <ol>
     *   <li>Validation de l'URL originale</li>
     *   <li>Vérification si l'URL existe déjà</li>
     *   <li>Génération d'un nouveau code court si nécessaire</li>
     *   <li>Gestion des collisions potentielles</li>
     *   <li>Retour de l'URL raccourcie complète</li>
     * </ol>
     * 
     * @param originalUrl l'URL originale à raccourcir (doit être HTTP/HTTPS valide)
     * @return une réponse contenant l'URL raccourcie complète
     * @throws IllegalArgumentException si l'URL originale est invalide, null ou vide
     * @throws RuntimeException si une erreur survient lors de la génération du hash
     */
    public ShortenResponse shorten(String originalUrl) {
        if (!isValidHttpUrl(originalUrl)) {
            throw new IllegalArgumentException("Invalid URL");
        }

        Optional<UrlMapping> existing = urlMappingRepository.findByOriginalUrl(originalUrl);
        if (existing.isPresent()) {
            return new ShortenResponse(baseUrl + "/" + existing.get().getShortCode());
        }

        int attempt = 0;
        while (true) {
            String candidate = generateShortCode(originalUrl, attempt);
            Optional<UrlMapping> existingCode = urlMappingRepository.findByShortCode(candidate);
            if (existingCode.isEmpty()) {
                UrlMapping saved = urlMappingRepository.save(new UrlMapping(candidate, originalUrl));
                return new ShortenResponse(baseUrl + "/" + saved.getShortCode());
            }
            if (existingCode.get().getOriginalUrl().equals(originalUrl)) {
                // Same mapping already present (race condition scenario)
                return new ShortenResponse(baseUrl + "/" + existingCode.get().getShortCode());
            }
            attempt++;
        }
    }

    /**
     * Récupère l'URL originale à partir d'un code court.
     * 
     * <p>Cette méthode effectue une recherche dans la base de données pour trouver
     * l'URL originale correspondant au code court fourni.
     * 
     * @param shortCode le code court à rechercher
     * @return l'URL originale correspondante
     * @throws IllegalArgumentException si le code court n'est pas trouvé dans la base de données
     */
    public String expand(String shortCode) {
        return urlMappingRepository.findByShortCode(shortCode)
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> new IllegalArgumentException("Short code not found"));
    }

    /**
     * Record représentant la réponse du service de raccourcissement.
     * 
     * <p>Cette classe immuable contient l'URL raccourcie générée.
     * 
     * @param shortUrl l'URL raccourcie complète (ex: http://localhost:8080/abc123)
     */
    public record ShortenResponse(String shortUrl) {}

    /**
     * Valide qu'une chaîne représente une URL HTTP/HTTPS valide.
     * 
     * <p>Cette méthode vérifie :
     * <ul>
     *   <li>Que la chaîne n'est pas null ou vide</li>
     *   <li>Que l'URL a un schéma HTTP ou HTTPS</li>
     *   <li>Que l'URL a un host valide</li>
     *   <li>Que l'URL est syntaxiquement correcte</li>
     * </ul>
     * 
     * @param value la chaîne à valider
     * @return true si l'URL est valide, false sinon
     */
    private boolean isValidHttpUrl(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Génère un code court à partir d'une chaîne d'entrée et d'un salt.
     * 
     * <p>Cette méthode utilise l'algorithme suivant :
     * <ol>
     *   <li>Concatène l'entrée avec le salt</li>
     *   <li>Génère un hash MD5 de la chaîne résultante</li>
     *   <li>Convertit le hash en Base62</li>
     *   <li>Tronque à la longueur maximale si nécessaire</li>
     * </ol>
     * 
     * @param input la chaîne d'entrée (généralement l'URL originale)
     * @param salt le salt pour éviter les collisions
     * @return le code court généré
     */
    private String generateShortCode(String input, int salt) {
        byte[] digest = md5(input + ":" + salt);
        String base62 = toBase62(digest);
        if (base62.length() <= MAX_SHORT_CODE_LENGTH) {
            return base62;
        }
        return base62.substring(0, MAX_SHORT_CODE_LENGTH);
    }

    /**
     * Génère un hash MD5 d'une chaîne d'entrée.
     * 
     * <p>Cette méthode utilise l'algorithme MD5 pour générer un hash
     * de la chaîne fournie. MD5 est utilisé ici pour sa rapidité et
     * sa distribution uniforme des valeurs.
     * 
     * @param input la chaîne à hasher
     * @return le hash MD5 sous forme de tableau de bytes
     * @throws IllegalStateException si l'algorithme MD5 n'est pas disponible
     */
    private byte[] md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }

    /**
     * Convertit un tableau de bytes en représentation Base62.
     * 
     * <p>Cette méthode convertit un hash binaire en une chaîne Base62
     * utilisant l'alphabet défini dans {@code BASE62_ALPHABET}.
     * 
     * @param bytes le tableau de bytes à convertir
     * @return la représentation Base62 du tableau de bytes
     */
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


