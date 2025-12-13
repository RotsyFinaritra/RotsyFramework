# Utilisation de l'annotation @Json

## Description
L'annotation `@Json` permet de retourner automatiquement une réponse JSON formatée depuis vos méthodes de contrôleur.

## Format de réponse
Toutes les réponses JSON suivent ce format standardisé :
```json
{
  "status": "success",
  "code": 200,
  "data": { ... }
}
```

## Utilisation

### 1. Ajouter l'annotation sur votre méthode de contrôleur
```java
@Controller
public class MyController {
    
    @GetMapping("/api/users")
    @Json
    public List<User> getUsers() {
        return userService.getAllUsers();
    }
    
    @GetMapping("/api/user/{id}")
    @Json
    public User getUser(@RequestParam("id") int id) {
        return userService.getUserById(id);
    }
    
    @GetMapping("/api/dashboard")
    @Json
    public ModelView getDashboard() {
        ModelView mv = new ModelView();
        mv.setView("dashboard");
        mv.addObject("users", userService.getAllUsers());
        mv.addObject("stats", statsService.getStats());
        return mv;
    }
}
```

### 2. Ajouter la dépendance Jackson (Recommandé)
Pour de meilleures performances et plus de fonctionnalités, ajoutez Jackson à votre projet :

#### Maven
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.16.1</version>
</dependency>
```

#### Gradle
```groovy
implementation 'com.fasterxml.jackson.core:jackson-databind:2.16.1'
```

## Comportement

### Avec Jackson (recommandé)
- Sérialisation JSON complète et optimisée
- Support des annotations Jackson (@JsonProperty, @JsonIgnore, etc.)
- Gestion automatique des dates, collections, objets complexes

### Sans Jackson (fallback)
- Implémentation JSON basique intégrée au framework
- Fonctionnalités limitées mais suffisantes pour des cas simples
- Un message d'information sera affiché dans les logs

## Exemples de réponses

### Objet simple
```json
{
  "status": "success",
  "code": 200,
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

### Tableau d'objets
```json
{
  "status": "success",
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "John Doe"
    },
    {
      "id": 2,
      "name": "Jane Smith"
    }
  ]
}
```

### ModelView
Si votre méthode retourne un `ModelView`, seule la `data` sera sérialisée :
```json
{
  "status": "success",
  "code": 200,
  "data": {
    "users": [...],
    "stats": {...}
  }
}
```