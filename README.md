# URL Shortener

Un service de raccourcissement d'URL moderne et élégant développé avec Spring Boot et Thymeleaf.

## Fonctionnalités

- **Raccourcissement d'URL** : Transformez vos URLs longues en liens courts
- **Récupération d'URL** : Retrouvez l'URL originale à partir du code court
- **Redirection automatique** : Accès direct via les URLs courtes
- **Interface moderne** : Design responsive et intuitif
- **Base de données persistante** : Conservation des données entre les redémarrages
- **Tests complets** : Couverture de tests unitaires et d'intégration

## Démarrage rapide

### Prérequis
- Java 17 ou supérieur
- Maven 3.6+

### Installation et lancement

1. **Cloner le projet**
   ```bash
   git clone https://github.com/Rayenoca/Test_Technique_Portage_CyberTech.git
   cd url-shortener
   ```

2. **Lancer l'application**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Accéder à l'application**
   - Interface web : http://localhost:8080

## Technologies utilisées

- **Backend** : Spring Boot 3.5.6, Spring Data JPA, Spring Web MVC
- **Base de données** : H2 Database (mode fichier persistant)
- **Frontend** : Thymeleaf, HTML5, CSS3, JavaScript
- **Tests** : JUnit 5, Mockito, Spring Boot Test
- **Build** : Maven


## Configuration

### Base de données
L'application utilise H2 en mode fichier persistant. Les données sont stockées dans `data/urlshortener.mv.db`.

## Tests

### Lancer tous les tests
```bash
./mvnw test
```

## Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## Auteur

Aziz Rayene Delaa