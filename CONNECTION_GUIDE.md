# 🔗 Configuration Front-End ↔ Back-End ↔ Base de Données

Ce document explique comment le projet **SportsIn** relie le front-end React/Vite, le back-end Spring Boot et la base de données SQLite.

## 📋 Vue d'ensemble de l'architecture

```
┌─────────────────────────────────────────────────────┐
│  FRONTEND (React + Vite)                           │
│  Port: 5173                                         │
│  - App.jsx                                          │
│  - Pages (HomePage, MapPage, etc.)                  │
│  - api/api.js (service API)                         │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP requests (fetch)
                       │ /api/equipes, /joueurs, etc.
                       ↓
┌─────────────────────────────────────────────────────┐
│  BACKEND (Spring Boot)                              │
│  Port: 8080                                         │
│  - Controllers (@RestController)                    │
│  - Services (@Service)                              │
│  - Repositories (JpaRepository)                     │
│  - Models (Entity @Entity)                          │
└──────────────────────┬──────────────────────────────┘
                       │ JPA/Hibernate
                       │ SQL queries
                       ↓
┌─────────────────────────────────────────────────────┐
│  DATABASE (SQLite)                                  │
│  File: sportsin.db                                  │
│  - Tables: equipe, joueur, arene, sport, session   │
│  - Relationships: FK, Indexes                       │
└─────────────────────────────────────────────────────┘
```

## 1️⃣ Configuration de la Base de Données

### Fichier: `app/src/main/resources/application.properties`

```properties
# SQLite Configuration
spring.datasource.url=jdbc:sqlite:sportsin.db
spring.datasource.driver-class-name=org.sqlite.JDBC

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=validate
```

### Création de la BD

```bash
./create_database.sh
```

Cela exécute le script SQL dans `app/src/main/resources/schema.sql` qui crée :
- ✅ Table `equipe`
- ✅ Table `joueur`
- ✅ Table `arene`
- ✅ Table `sport`
- ✅ Table `session`
- ✅ Table `metric_value`
- ✅ Tables de jointure (many-to-many)

## 2️⃣ Entités JPA (Modèle ↔ BD)

Les classes `Entity` font le pont entre Java et la base de données :

### Exemple: Équipe

```java
@Entity
@Table(name = "equipe")
public class Equipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nom;
    
    @OneToMany(mappedBy = "equipe", cascade = CascadeType.ALL)
    private Set<Joueur> joueurs;
}
```

**Fichiers concernés:**
- `app/src/main/java/org/SportsIn/model/Equipe.java`
- `app/src/main/java/org/SportsIn/model/Joueur.java`
- `app/src/main/java/org/SportsIn/model/Arene.java`

## 3️⃣ Repositories Spring Data JPA

Les repositories permettent d'accéder à la BD sans écrire du SQL :

```java
@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Long> {
    Optional<Equipe> findByNom(String nom);
}
```

**Méthodes disponibles automatiquement:**
- `findAll()` - Récupère toutes les équipes
- `findById(Long id)` - Récupère une équipe par ID
- `save(Equipe equipe)` - Crée ou modifie une équipe
- `delete(Equipe equipe)` - Supprime une équipe

**Fichiers:**
- `app/src/main/java/org/SportsIn/repository/EquipeRepository.java`
- `app/src/main/java/org/SportsIn/repository/JoueurRepository.java`
- `app/src/main/java/org/SportsIn/repository/AreneRepository.java`

## 4️⃣ Contrôleurs REST (API)

Les contrôleurs exposent des endpoints HTTP pour le front-end :

```java
@RestController
@RequestMapping("/api/equipes")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class EquipeController {
    
    @GetMapping
    public ResponseEntity<List<Equipe>> getAll() {
        return ResponseEntity.ok(equipeRepository.findAll());
    }
    
    @PostMapping
    public ResponseEntity<Equipe> create(@RequestBody Equipe equipe) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(equipeRepository.save(equipe));
    }
}
```

### Endpoints disponibles:

| Méthode | URL | Description |
|---------|-----|-------------|
| GET | `/api/equipes` | Récupère toutes les équipes |
| GET | `/api/equipes/{id}` | Récupère une équipe |
| POST | `/api/equipes` | Crée une équipe |
| PUT | `/api/equipes/{id}` | Modifie une équipe |
| DELETE | `/api/equipes/{id}` | Supprime une équipe |

**Même pattern pour:** `/api/joueurs`, `/api/arenes`

**Fichiers:**
- `app/src/main/java/org/SportsIn/controller/EquipeController.java`
- `app/src/main/java/org/SportsIn/controller/JoueurController.java`
- `app/src/main/java/org/SportsIn/controller/AreneController.java`

## 5️⃣ Configuration CORS (Front-End → Back-End)

Le CORS (Cross-Origin Resource Sharing) permet au front-end d'accéder à l'API du back-end:

**Fichier:** `app/src/main/java/org/SportsIn/config/CorsConfig.java`

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
    }
}
```

## 6️⃣ Configuration Vite (Proxy)

**Fichier:** `frontend/vite.config.js`

```javascript
export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  }
})
```

**Bénéfice:** Pendant le développement, les requêtes `/api/*` sont automatiquement redirigées vers `http://localhost:8080/api/*`

## 7️⃣ Service API React

**Fichier:** `frontend/src/api/api.js`

```javascript
const API_BASE_URL = '/api'; // Utilise le proxy Vite

export const equipeAPI = {
  getAll: async () => fetchAPI('/equipes'),
  create: async (data) => fetchAPI('/equipes', { 
    method: 'POST', 
    body: JSON.stringify(data) 
  }),
};
```

### Utilisation dans les composants:

```javascript
import { equipeAPI } from '../api/api';

export default function MyComponent() {
  const [equipes, setEquipes] = useState([]);

  useEffect(() => {
    equipeAPI.getAll()
      .then(data => setEquipes(data))
      .catch(error => console.error(error));
  }, []);

  return (
    <div>
      {equipes.map(equipe => (
        <div key={equipe.id}>{equipe.nom}</div>
      ))}
    </div>
  );
}
```

## ✅ Checklist de démarrage

```bash
# 1. Créer la base de données
./create_database.sh

# 2. Démarrer le back-end Spring Boot
./gradlew bootRun
# 👉 Backend accessible à http://localhost:8080

# 3. Installer les dépendances front-end
cd frontend
npm install

# 4. Démarrer le front-end Vite
npm run dev
# 👉 Frontend accessible à http://localhost:5173

# 5. Tester la connexion
# Accédez à http://localhost:5173/api-test
```

## 🧪 Page de Test

Une page de test est disponible pour vérifier que tout fonctionne :

**Fichier:** `frontend/src/pages/ApiTestPage.jsx`

Elle permet de :
- ✅ Récupérer toutes les équipes, joueurs et arènes
- ✅ Créer une nouvelle équipe
- ✅ Créer un nouveau joueur
- ✅ Supprimer des données

### Accès: `http://localhost:5173/` (après intégration dans App.jsx)

## 🔧 Dépannage

### Erreur 1: "CORS error"
**Solution:** Vérifier que `CorsConfig.java` est activé et que les ports sont corrects.

### Erreur 2: "Base de données introuvable"
**Solution:** Exécuter `./create_database.sh`

### Erreur 3: "Cannot resolve symbol"
**Solution:** 
```bash
# Rebuilder le projet
./gradlew clean build
```

### Erreur 4: "Cannot GET /api/..."
**Solution:** S'assurer que le backend est lancé sur le port 8080

## 📚 Ressources

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Hibernate Documentation](https://hibernate.org/)
- [React Hooks Documentation](https://react.dev/reference/react)
- [SQLite Documentation](https://www.sqlite.org/docs.html)
