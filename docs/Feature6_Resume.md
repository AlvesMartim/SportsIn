# Feature 6 : Team Progression & Unlockable Perks

## 1. Vue d'ensemble

La Feature 6 ajoute un **systeme de progression par equipe** au projet SportsIn. Chaque equipe accumule de l'XP en jouant des matchs, monte en niveau, et debloque des **perks activables** (bouclier, boost d'influence, multiplicateur XP) qui modifient le gameplay.

### Objectifs
- Systeme d'XP et de niveaux (1 a 10)
- Catalogue de perks parametrables (non hardcodes)
- Perks consommables avec duree et cooldown
- Integration dans le calcul d'influence existant
- Respect des principes SOLID et des Design Patterns

---

## 2. Architecture & Design Patterns

### Strategy Pattern
L'interface `PerkEffectStrategy` definit le contrat pour chaque type d'effet. Chaque effet est un `@Component` Spring independant. Ajouter un nouveau perk = **1 classe @Component + 1 INSERT SQL**.

```
PerkEffectStrategy (interface)
├── ShieldEffect         (INFLUENCE_REDUCTION)
├── BoostEffect          (INFLUENCE_BOOST)
└── XpMultiplierEffect   (XP_MULTIPLIER)
```

### Registry Pattern
`PerkEffectRegistry` auto-decouvre tous les beans `PerkEffectStrategy` via injection Spring et les indexe par `effectType`. Resolution en O(1).

### Chain of Responsibility (Modifier Chain)
`InfluenceCalculator` chaine des `InfluenceModifier` tries par ordre de priorite :
1. `RouteInfluenceModifier` (ordre 10) — bonus de route existant
2. `PerkInfluenceModifier` (ordre 20) — bonus/malus des perks actifs

### Principes SOLID
- **Open/Closed** : nouveau perk = 1 composant + 1 ligne SQL, aucun code existant a modifier
- **Single Responsibility** : separation donnees (entites) / logique (strategies) / cycle de vie (services)
- **Dependency Inversion** : tout passe par des interfaces (`PerkEffectStrategy`, `InfluenceModifier`)

---

## 3. Systeme de niveaux

Table XP statique dans `LevelThreshold.java` :

| Niveau | XP requis |
|--------|-----------|
| 1      | 0         |
| 2      | 100       |
| 3      | 300       |
| 4      | 600       |
| 5      | 1 000     |
| 6      | 1 500     |
| 7      | 2 200     |
| 8      | 3 000     |
| 9      | 4 000     |
| 10     | 5 500     |

### Gain d'XP
- **Victoire** : +30 XP
- **Defaite** : +10 XP
- Les multiplicateurs XP (perk `XP_BOOST`) s'appliquent automatiquement

---

## 4. Perks disponibles

| Code | Niveau requis | Duree | Cooldown | Effet |
|------|---------------|-------|----------|-------|
| `SHIELD_QUARTIER` | 3 | 3 jours | 7 jours | -50% influence adverse sur un point |
| `BOOST_INFLUENCE` | 5 | 2 jours | 5 jours | +25% influence propre sur un point |
| `XP_BOOST` | 2 | 24h | 3 jours | x1.5 XP gagne |

Chaque perk est **parametrable via JSON** (champ `parametersJson`), ce qui permet de modifier les pourcentages sans toucher au code.

---

## 5. Endpoints API REST

### `GET /api/teams/{teamId}/progression`
Retourne le niveau, l'XP et les perks debloques d'une equipe.

**Reponse :**
```json
{
  "teamId": 1,
  "level": 3,
  "currentXp": 350,
  "xpForNextLevel": 250,
  "unlockedPerks": [
    { "code": "XP_BOOST", "name": "Boost XP", "requiredLevel": 2 },
    { "code": "SHIELD_QUARTIER", "name": "Bouclier de Quartier", "requiredLevel": 3 }
  ]
}
```

### `GET /api/perks`
Retourne le catalogue complet des perks.

### `POST /api/teams/{teamId}/perks/activate`
Active un perk pour une equipe sur une cible.

**Body :**
```json
{
  "perkCode": "SHIELD_QUARTIER",
  "targetId": "42"
}
```

**Validations :**
- Niveau suffisant
- Nombre max d'instances non atteint
- Cooldown ecoule

### `GET /api/teams/{teamId}/perks/active`
Retourne les perks actuellement actifs pour une equipe.

---

## 6. Fichiers crees (18 fichiers)

### Modeles
| Fichier | Description |
|---------|-------------|
| `model/progression/PerkDefinition.java` | Entite JPA — catalogue des perks (code, niveau, duree, cooldown, params JSON) |
| `model/progression/ActivePerk.java` | Entite JPA — instance de perk active (equipe, cible, expiration) |
| `model/progression/LevelThreshold.java` | Classe utilitaire — table XP/niveaux statique |
| `model/progression/PerkContext.java` | Record Java — contexte passe aux strategies |

### Strategies (effets)
| Fichier | Description |
|---------|-------------|
| `model/progression/effects/PerkEffectStrategy.java` | Interface Strategy |
| `model/progression/effects/ShieldEffect.java` | Effet INFLUENCE_REDUCTION (-50% influence adverse) |
| `model/progression/effects/BoostEffect.java` | Effet INFLUENCE_BOOST (+25% influence propre) |
| `model/progression/effects/XpMultiplierEffect.java` | Effet XP_MULTIPLIER (x1.5 XP) |
| `model/progression/effects/PerkEffectRegistry.java` | Registre auto-decouverte des strategies |

### Services
| Fichier | Description |
|---------|-------------|
| `services/TeamProgressionService.java` | Niveau, perks debloques, catalogue |
| `services/PerkActivationService.java` | Activation/desactivation avec validations |
| `services/XpGrantService.java` | Attribution XP avec multiplicateur |
| `services/InfluenceCalculator.java` | Chaine de calcul des modificateurs |
| `services/InfluenceModifier.java` | Interface pour les modificateurs d'influence |
| `services/RouteInfluenceModifier.java` | Modificateur route (extrait de TerritoryService) |
| `services/PerkInfluenceModifier.java` | Modificateur perks (query perks actifs) |

### Controleur
| Fichier | Description |
|---------|-------------|
| `controller/ProgressionController.java` | 4 endpoints REST (voir section 5) |

### Repositories
| Fichier | Description |
|---------|-------------|
| `repository/PerkDefinitionRepository.java` | CRUD + findByCode, findByRequiredLevel |
| `repository/ActivePerkRepository.java` | CRUD + queries actifs/expires/par equipe |

---

## 7. Fichiers modifies (6 fichiers)

| Fichier | Modification |
|---------|-------------|
| `services/TerritoryService.java` | Delegue le calcul de bonus a `InfluenceCalculator` au lieu du calcul inline |
| `services/SessionService.java` | Appelle `XpGrantService.grantMatchXp()` apres chaque match |
| `resources/schema.sql` | Ajout tables `perk_definition` et `active_perk` + 4 index |
| `resources/data.sql` | Ajout des 3 perks initiaux |
| `test/.../TerritoryServiceTest.java` | Constructeur mis a jour (ajout InfluenceCalculator) |
| `test/.../SessionServiceTest.java` | Constructeur mis a jour (ajout XpGrantService) |
| `test/.../MissionServiceTest.java` | Constructeur mis a jour (ajout InfluenceCalculator) |

---

## 8. Tests (34 tests Feature 6)

### LevelThresholdTest — 14 tests
Teste le calcul de niveau a partir de l'XP :
- Correspondance XP → niveau (0, 99, 100, 300, 1000, 5500, 999999 XP)
- XP restant pour le prochain niveau
- Seuil XP requis par niveau (valide et invalide)
- Cas limites : XP negatif, niveau max depasse

### ShieldEffectTest — 9 tests
Teste l'effet Shield et ses conditions d'activation :
- Type d'effet correct (`INFLUENCE_REDUCTION`)
- Calcul de reduction (50%, 100%, 0% base, defaut sans parametre)
- Rejet si niveau insuffisant
- Acceptation au niveau requis
- Rejet si max instances atteint
- Rejet pendant le cooldown

### InfluenceCalculatorTest — 4 tests
Teste le chainage des modificateurs d'influence :
- Deux modifiers chaines dans l'ordre
- Aucun modifier → retourne 0
- Un seul modifier
- Valeur accumulee transmise au modifier suivant

### TeamProgressionServiceTest — 7 tests
Teste l'integration equipe + systeme de niveaux :
- Niveau d'equipe a differents paliers XP (0, 300, 599, 600)
- XP necessaire pour le prochain niveau
- Progression monotone (le niveau ne descend jamais)

### Resultat global
```
BUILD SUCCESSFUL
67 tests completed, 0 failures (34 nouveaux + 33 existants adaptes)
```

---

## 9. Schema de base de donnees

### Table `perk_definition`
```sql
CREATE TABLE IF NOT EXISTS perk_definition (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    effect_type TEXT NOT NULL,
    required_level INTEGER NOT NULL DEFAULT 1,
    duration_seconds INTEGER NOT NULL DEFAULT 86400,
    cooldown_seconds INTEGER NOT NULL DEFAULT 0,
    max_active_instances INTEGER NOT NULL DEFAULT 1,
    stackable INTEGER NOT NULL DEFAULT 0,
    parameters_json TEXT
);
```

### Table `active_perk`
```sql
CREATE TABLE IF NOT EXISTS active_perk (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    team_id INTEGER NOT NULL,
    perk_definition_id INTEGER NOT NULL,
    target_id TEXT,
    activated_at TEXT NOT NULL,
    expires_at TEXT NOT NULL,
    last_used_at TEXT,
    usage_count INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (team_id) REFERENCES equipe(id),
    FOREIGN KEY (perk_definition_id) REFERENCES perk_definition(id)
);
```

---

## 10. Diagramme de flux

```
Match termine
     │
     ▼
SessionService.endSession()
     │
     ├─► XpGrantService.grantMatchXp(winner, true)   → +30 XP (× multiplicateur)
     ├─► XpGrantService.grantMatchXp(loser, false)    → +10 XP (× multiplicateur)
     │
     ▼
Equipe.xp mis a jour en BDD
     │
     ▼
LevelThreshold.levelForXp(xp) → nouveau niveau
     │
     ▼
Nouveaux perks debloques si niveau suffisant
     │
     ▼
POST /api/teams/{id}/perks/activate
     │
     ├─► Validation : niveau, cooldown, max instances
     ├─► Creation ActivePerk en BDD
     │
     ▼
Calcul d'influence (TerritoryService)
     │
     ├─► InfluenceCalculator.computeTotalModifier()
     │       ├─► RouteInfluenceModifier  (ordre 10)
     │       └─► PerkInfluenceModifier   (ordre 20)
     │
     ▼
Score final = score de base + modificateur total
```

---

## 11. Comment ajouter un nouveau perk

1. **Creer une classe Strategy** :
```java
@Component
public class NouvelEffet implements PerkEffectStrategy {
    @Override
    public String getEffectType() { return "NOUVEL_EFFET"; }

    @Override
    public double computeInfluenceModifier(PerkContext ctx) {
        // logique de l'effet
    }

    @Override
    public boolean canActivate(Equipe team, PerkDefinition def, List<ActivePerk> active) {
        // conditions d'activation
    }
}
```

2. **Ajouter une ligne SQL** dans `data.sql` :
```sql
INSERT INTO perk_definition (code, name, description, effect_type, required_level,
    duration_seconds, cooldown_seconds, max_active_instances, stackable, parameters_json)
VALUES ('NOUVEL_EFFET', 'Mon Nouveau Perk', 'Description...', 'NOUVEL_EFFET',
    4, 172800, 432000, 1, 0, '{"param": 30}');
```

Aucune autre modification necessaire grace au principe **Open/Closed**.

---

## 12. Branche Git

Tout le code se trouve sur la branche `feature/team-progression`.
