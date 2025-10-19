package com.portagecybertech.urlshortener.url_shortener.repository;

import com.portagecybertech.urlshortener.url_shortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository Spring Data JPA pour la gestion des mappings d'URLs.
 * 
 * <p>Cette interface étend {@link JpaRepository} et fournit des méthodes
 * personnalisées pour rechercher des mappings d'URLs par code court ou URL originale.
 * 
 * <p><strong>Fonctionnalités :</strong>
 * <ul>
 *   <li>Recherche par code court unique</li>
 *   <li>Recherche par URL originale unique</li>
 *   <li>Opérations CRUD standard héritées de JpaRepository</li>
 * </ul>
 * 
 * <p><strong>Optimisations :</strong>
 * Les méthodes de recherche utilisent les index uniques définis sur la table
 * {@code url_mapping} pour des performances optimales.
 * 
 * @author Aziz Rayene Delaa
 * @version 1.0
 * @since 1.0
 * @see UrlMapping
 * @see JpaRepository
 */
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    
    /**
     * Recherche un mapping d'URL par son code court.
     * 
     * <p>Cette méthode effectue une recherche optimisée utilisant l'index unique
     * sur la colonne {@code short_code}.
     * 
     * @param shortCode le code court à rechercher
     * @return un Optional contenant le mapping trouvé, ou empty si non trouvé
     */
    Optional<UrlMapping> findByShortCode(String shortCode);
    
    /**
     * Recherche un mapping d'URL par son URL originale.
     * 
     * <p>Cette méthode effectue une recherche optimisée utilisant l'index unique
     * sur la colonne {@code original_url}. Elle permet de vérifier si une URL
     * a déjà été raccourcie et d'éviter les doublons.
     * 
     * @param originalUrl l'URL originale à rechercher
     * @return un Optional contenant le mapping trouvé, ou empty si non trouvé
     */
    Optional<UrlMapping> findByOriginalUrl(String originalUrl);
}


