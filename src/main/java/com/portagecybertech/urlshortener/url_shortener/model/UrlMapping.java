package com.portagecybertech.urlshortener.url_shortener.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Entité JPA représentant le mapping entre une URL courte et une URL originale.
 * 
 * <p>Cette classe stocke la correspondance entre :
 * <ul>
 *   <li>Un code court généré (shortCode) - identifiant unique de l'URL raccourcie</li>
 *   <li>L'URL originale complète (originalUrl) - l'URL de destination</li>
 * </ul>
 * 
 * <p>La table est optimisée avec des index uniques sur les deux colonnes principales
 * pour garantir l'unicité et améliorer les performances de recherche.
 * 
 * @author Aziz Rayene Delaa
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(
    name = "url_mapping",
    indexes = {
        @Index(name = "idx_url_mapping_shortcode", columnList = "short_code", unique = true),
        @Index(name = "idx_url_mapping_originalurl", columnList = "original_url", unique = true)
    }
)
public class UrlMapping {

    /**
     * Identifiant unique de l'entrée dans la base de données.
     * Généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Code court généré pour l'URL raccourcie.
     * 
     * <p>Caractéristiques :
     * <ul>
     *   <li>Longueur maximale : 10 caractères</li>
     *   <li>Unique dans la base de données</li>
     *   <li>Non nullable</li>
     *   <li>Utilisé dans l'URL raccourcie : http://localhost:8080/{shortCode}</li>
     * </ul>
     */
    @Column(name = "short_code", length = 10, nullable = false, unique = true)
    private String shortCode;

    /**
     * URL originale complète à raccourcir.
     * 
     * <p>Caractéristiques :
     * <ul>
     *   <li>Longueur maximale : 2048 caractères</li>
     *   <li>Unique dans la base de données (évite les doublons)</li>
     *   <li>Non nullable</li>
     *   <li>Doit être une URL HTTP/HTTPS valide</li>
     * </ul>
     */
    @Column(name = "original_url", length = 2048, nullable = false, unique = true)
    private String originalUrl;

    /**
     * Constructeur par défaut requis par JPA.
     * 
     * <p>Ce constructeur est utilisé par Hibernate lors de la désérialisation
     * des entités depuis la base de données. Il ne doit pas être utilisé
     * directement dans le code applicatif.
     */
    protected UrlMapping() {
    }

    /**
     * Constructeur principal pour créer une nouvelle correspondance URL.
     * 
     * @param shortCode le code court généré pour l'URL raccourcie
     * @param originalUrl l'URL originale complète à raccourcir
     * @throws IllegalArgumentException si l'un des paramètres est null ou vide
     */
    public UrlMapping(String shortCode, String originalUrl) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
    }

    /**
     * Retourne l'identifiant unique de cette entrée.
     * 
     * @return l'ID généré par la base de données, ou null si pas encore persisté
     */
    public Long getId() {
        return id;
    }

    /**
     * Retourne le code court de l'URL raccourcie.
     * 
     * @return le code court unique, non null
     */
    public String getShortCode() {
        return shortCode;
    }

    /**
     * Retourne l'URL originale complète.
     * 
     * @return l'URL originale, non null
     */
    public String getOriginalUrl() {
        return originalUrl;
    }
}


