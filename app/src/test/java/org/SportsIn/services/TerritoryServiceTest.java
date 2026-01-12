package org.SportsIn.services;

import org.SportsIn.model.territory.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TerritoryServiceTest {

    private TerritoryService territoryService;
    private PointSportifRepository pointRepository;
    private ZoneRepository zoneRepository;

    private PointSportif p1, p2, p3, p4;
    private Zone zoneNord;

    @BeforeEach
    void setUp() {
        pointRepository = new InMemoryPointSportifRepository();
        zoneRepository = new InMemoryZoneRepository();
        territoryService = new TerritoryService(pointRepository, zoneRepository);

        // Création de 4 points
        p1 = new PointSportif(1L, "Point 1", 0, 0, null);
        p2 = new PointSportif(2L, "Point 2", 0, 0, null);
        p3 = new PointSportif(3L, "Point 3", 0, 0, null);
        p4 = new PointSportif(4L, "Point 4", 0, 0, null);

        pointRepository.save(p1);
        pointRepository.save(p2);
        pointRepository.save(p3);
        pointRepository.save(p4);

        // Création d'une zone contenant ces 4 points
        zoneNord = new Zone(100L, "Zone Nord", List.of(p1, p2, p3, p4));
        zoneRepository.save(zoneNord);
    }

    @Test
    @DisplayName("Conquête d'un point simple")
    void testUpdateTerritoryControl_SimplePointConquest() {
        // ACT
        territoryService.updateTerritoryControl(1L, 10L); // L'équipe 10 prend le point 1

        // ASSERT
        PointSportif updatedP1 = pointRepository.findById(1L).orElseThrow();
        assertEquals(10L, updatedP1.getControllingTeamId());
        
        // La zone ne doit pas être contrôlée (seulement 1 point)
        Zone updatedZone = zoneRepository.findById(100L).orElseThrow();
        assertNull(updatedZone.getControllingTeamId());
    }

    @Test
    @DisplayName("Conquête d'une zone (3 points)")
    void testUpdateTerritoryControl_ZoneConquest() {
        // ARRANGE : L'équipe 10 a déjà 2 points
        p1.setControllingTeamId(10L);
        p2.setControllingTeamId(10L);
        pointRepository.save(p1);
        pointRepository.save(p2);

        // ACT : L'équipe 10 prend le 3ème point
        territoryService.updateTerritoryControl(3L, 10L);

        // ASSERT
        Zone updatedZone = zoneRepository.findById(100L).orElseThrow();
        assertEquals(10L, updatedZone.getControllingTeamId(), "La zone devrait être contrôlée par l'équipe 10 (3 points).");
    }

    @Test
    @DisplayName("Perte d'une zone (passage sous 3 points)")
    void testUpdateTerritoryControl_ZoneLoss() {
        // ARRANGE : L'équipe 10 contrôle la zone avec 3 points
        p1.setControllingTeamId(10L);
        p2.setControllingTeamId(10L);
        p3.setControllingTeamId(10L);
        zoneNord.setControllingTeamId(10L);
        
        pointRepository.save(p1);
        pointRepository.save(p2);
        pointRepository.save(p3);
        zoneRepository.save(zoneNord);

        // ACT : L'équipe 20 prend un des points de l'équipe 10
        territoryService.updateTerritoryControl(3L, 20L);

        // ASSERT
        Zone updatedZone = zoneRepository.findById(100L).orElseThrow();
        // L'équipe 10 n'a plus que 2 points -> elle perd la zone
        // Note : Cela dépend de la règle implémentée dans Zone.java (si on a décommenté la perte de zone)
        // Dans le code actuel de Zone.java, j'ai laissé la ligne commentée ou non ?
        // Vérifions le code de Zone.java...
        // Dans ma réponse précédente, j'ai écrit : "Si on veut qu'elle perde la zone, on décommente la ligne suivante : this.controllingTeamId = null;"
        // Je vais supposer que je l'ai décommenté ou que je vais le faire pour que ce test passe si c'est le comportement voulu.
        // Pour l'instant, vérifions si c'est null.
        
        // Si la règle est "strictement >= 3 pour contrôler", alors < 3 signifie perte.
        // Si la règle est "maintien tant que personne d'autre ne prend", c'est différent.
        // Le test suppose la règle stricte.
        
        // Vérifions le code actuel de Zone.java via read_file si besoin, mais je viens de l'écrire.
        // J'ai écrit : } else if (newMaster == null && this.controllingTeamId != null) { this.controllingTeamId = null; return true; }
        // Donc oui, la perte est active.
        
        assertNull(updatedZone.getControllingTeamId(), "La zone devrait être perdue (plus que 2 points).");
    }

    @Test
    @DisplayName("Changement de propriétaire de zone")
    void testUpdateTerritoryControl_ZoneOwnerChange() {
        // ARRANGE : L'équipe 10 a 3 points, l'équipe 20 a 0 point.
        p1.setControllingTeamId(10L);
        p2.setControllingTeamId(10L);
        p3.setControllingTeamId(10L);
        zoneNord.setControllingTeamId(10L);
        
        // L'équipe 20 a déjà le point 4
        p4.setControllingTeamId(20L);
        
        pointRepository.save(p1);
        pointRepository.save(p2);
        pointRepository.save(p3);
        pointRepository.save(p4);
        zoneRepository.save(zoneNord);

        // ACT : L'équipe 20 prend p1, p2, p3 successivement ? Non, juste assez pour avoir la majorité ?
        // Règle actuelle : "celui qui a >= 3 points".
        // Si l'équipe 20 prend p1, elle a p1 et p4 (2 points). L'équipe 10 a p2 et p3 (2 points). Personne n'a 3.
        
        // Scénario : L'équipe 20 prend p1, p2, p3.
        territoryService.updateTerritoryControl(1L, 20L); // 20: 2pts, 10: 2pts -> Zone perdue
        territoryService.updateTerritoryControl(2L, 20L); // 20: 3pts, 10: 1pt -> Zone prise par 20
        
        // ASSERT
        Zone updatedZone = zoneRepository.findById(100L).orElseThrow();
        assertEquals(20L, updatedZone.getControllingTeamId());
    }
}
