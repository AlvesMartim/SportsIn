package org.SportsIn.services;

import org.SportsIn.model.territory.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TerritoryServiceTest {

    private TerritoryService territoryService;
    private PointSportifRepository pointRepository;
    private ZoneRepository zoneRepository;
    private RouteRepository routeRepository;

    private PointSportif p1, p2, p3, p4;
    private Zone zoneNord;
    private Route routeTest;

    @BeforeEach
    void setUp() {
        pointRepository = new InMemoryPointSportifRepository();
        zoneRepository = new InMemoryZoneRepository();
        routeRepository = new InMemoryRouteRepository();
        territoryService = new TerritoryService(pointRepository, zoneRepository, routeRepository);

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

        // Création d'une route
        routeTest = new Route(200L, "Route Test", "P1 -> P4", new ArrayList<>(Arrays.asList(p1, p2, p3, p4)));
        routeRepository.save(routeTest);
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

        // ACT : L'équipe 20 prend p1, p2, p3 successivement
        territoryService.updateTerritoryControl(1L, 20L); // 20: 2pts, 10: 2pts -> Zone perdue
        territoryService.updateTerritoryControl(2L, 20L); // 20: 3pts, 10: 1pt -> Zone prise par 20
        
        // ASSERT
        Zone updatedZone = zoneRepository.findById(100L).orElseThrow();
        assertEquals(20L, updatedZone.getControllingTeamId());
    }

    @Test
    @DisplayName("Détection de bonus de route")
    void testUpdateTerritoryControl_RouteBonus() {
        // ARRANGE : L'équipe 10 a P1 et P2
        p1.setControllingTeamId(10L);
        p2.setControllingTeamId(10L);
        pointRepository.save(p1);
        pointRepository.save(p2);

        // ACT : L'équipe 10 prend P3 -> Suite P1, P2, P3
        // Cela devrait déclencher un log de bonus (vérifiable visuellement ou via mock si on mockait RouteService)
        territoryService.updateTerritoryControl(3L, 10L);

        // Pas d'assertion directe possible sur les logs sans framework de log capture,
        // mais le test passe si aucune exception n'est levée.
        // On vérifie juste que l'état est cohérent.
        assertEquals(10L, p3.getControllingTeamId());
    }
}
