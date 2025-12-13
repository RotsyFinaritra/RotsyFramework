# Gestion des codes d'erreur avec @Json

## Exemples d'utilisation

### 1. Succès avec données (200)
```java
@Controller
public class UserController {
    
    @GetMapping("/api/users")
    @Json
    public List<User> getAllUsers() {
        return userService.findAll(); // Retourne une liste d'utilisateurs
    }
    
    @GetMapping("/api/user/{id}")
    @Json
    public User getUserById(@RequestParam("id") int id) {
        return userService.findById(id); // Retourne un utilisateur
    }
}
```
**Réponse** :
```json
{
  "status": "success",
  "code": 200,
  "data": [{"id": 1, "name": "John"}, {"id": 2, "name": "Jane"}]
}
```

### 2. Aucune donnée trouvée (404)
```java
@GetMapping("/api/users")
@Json
public List<User> getAllUsers() {
    return new ArrayList<>(); // Liste vide
}

@GetMapping("/api/user/{id}")
@Json
public User getUserById(@RequestParam("id") int id) {
    return null; // Utilisateur non trouvé
}
```
**Réponse** :
```json
{
  "status": "error", 
  "code": 404,
  "data": "No data found"
}
```

### 3. Erreur avec ModelView
```java
@GetMapping("/api/user/{id}")
@Json
public ModelView getUserProfile(@RequestParam("id") int id) {
    ModelView mv = new ModelView();
    
    User user = userService.findById(id);
    if (user == null) {
        mv.addObject("error", "User not found with ID: " + id);
        return mv;
    }
    
    mv.addObject("user", user);
    mv.addObject("profile", profileService.getProfile(id));
    return mv;
}
```
**Réponse (succès)** :
```json
{
  "status": "success",
  "code": 200, 
  "data": {
    "user": {"id": 1, "name": "John"},
    "profile": {"bio": "Developer"}
  }
}
```

**Réponse (erreur)** :
```json
{
  "status": "error",
  "code": 404,
  "data": {
    "error": "User not found with ID: 999"
  }
}
```

### 4. Erreur de validation (400)
```java
@PostMapping("/api/user")
@Json
public ModelView createUser(@RequestParam("name") String name) {
    ModelView mv = new ModelView();
    
    if (name == null || name.trim().isEmpty()) {
        mv.addObject("error", "Invalid name provided");
        return mv;
    }
    
    User user = userService.create(name);
    mv.addObject("user", user);
    return mv;
}
```
**Réponse (erreur)** :
```json
{
  "status": "error",
  "code": 400,
  "data": {
    "error": "Invalid name provided"
  }
}
```

### 5. Erreur serveur (500)
```java
@GetMapping("/api/users")
@Json
public List<User> getAllUsers() {
    // Si une exception est lancée (DB error, etc.)
    throw new RuntimeException("Database connection failed");
}
```
**Réponse** :
```json
{
  "status": "error",
  "code": 500,
  "data": "Database connection failed"
}
```

## Récapitulatif des codes

| Code | Status | Utilisation |
|------|--------|-------------|
| 200  | success | Opération réussie avec données |
| 400  | error | Mauvaise requête, paramètres invalides |
| 404  | error | Ressource non trouvée, données vides |
| 500  | error | Erreur serveur, exception non gérée |

## Conseils pour les développeurs

### Pour forcer un code d'erreur spécifique :
```java
@GetMapping("/api/user/{id}")
@Json
public ModelView getUser(@RequestParam("id") int id) {
    ModelView mv = new ModelView();
    
    if (id <= 0) {
        mv.addObject("error", "Invalid ID parameter"); // → 400
        return mv;
    }
    
    User user = userService.findById(id);
    if (user == null) {
        mv.addObject("error", "User not found"); // → 404  
        return mv;
    }
    
    mv.addObject("user", user);
    return mv; // → 200
}
```