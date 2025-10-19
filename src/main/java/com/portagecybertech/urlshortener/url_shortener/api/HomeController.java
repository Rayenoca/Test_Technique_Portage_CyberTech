package com.portagecybertech.urlshortener.url_shortener.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur MVC pour la page d'accueil de l'application.
 * 
 * <p>Ce contrôleur gère l'affichage de l'interface utilisateur principale
 * de l'application de raccourcissement d'URL. Il utilise Thymeleaf pour
 * le rendu des templates HTML.
 * 
 * <p><strong>Endpoint :</strong>
 * <ul>
 *   <li>{@code GET /} - Affiche la page d'accueil avec l'interface utilisateur</li>
 * </ul>
 * 
 * <p><strong>Template utilisé :</strong>
 * <p>Le template {@code index.html} situé dans {@code src/main/resources/templates/}
 * contient l'interface utilisateur complète avec :
 * <ul>
 *   <li>Formulaire de raccourcissement d'URL</li>
 *   <li>Formulaire de récupération d'URL originale</li>
 *   <li>Interface JavaScript pour les appels API</li>
 * </ul>
 * 
 * @author Aziz Rayene Delaa
 * @version 1.0
 * @since 1.0
 */
@Controller
public class HomeController {
    
    /**
     * Affiche la page d'accueil de l'application.
     * 
     * <p><strong>Endpoint :</strong> {@code GET /}
     * 
     * <p>Cette méthode retourne le nom du template Thymeleaf à rendre.
     * Spring Boot recherche automatiquement le fichier {@code index.html}
     * dans le répertoire {@code src/main/resources/templates/}.
     * 
     * @return le nom du template à rendre ("index")
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
