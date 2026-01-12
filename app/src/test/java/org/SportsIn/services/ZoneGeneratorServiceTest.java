package org.SportsIn.services;

import org.SportsIn.model.territory.PointSportif;
import org.SportsIn.model.territory.Zone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZoneGeneratorServiceTest {

    @Test
    @DisplayName("Doit regrouper les points proches dans une même zone")
    void testGenerateZones_GroupsNearbyPoints() {
        ZoneGeneratorService generator = new ZoneGeneratorService();
        List<PointSportif> points = new ArrayList<>();

        // Groupe 1 : Paris Centre (3 points proches)
        // Distance approx entre Châtelet et Louvre : ~1km
        points.add(new PointSportif(1L, "Châtelet", 48.8584, 2.3470, null));
        points.add(new PointSportif(2L, "Louvre", 48.8606, 2.3376, null));
        points.add(new PointSportif(3L, "Notre-Dame", 48.8530, 2.3499, null));

        // Groupe 2 : Marseille (2 points, trop loin de Paris)
        points.add(new PointSportif(4L, "Vieux Port", 43.2951, 5.3744, null));
        points.add(new PointSportif(5L, "Vélodrome", 43.2698, 5.3959, null));

        // Point isolé : Lyon
        points.add(new PointSportif(6L, "Lyon Part-Dieu", 45.7601, 4.8590, null));

        // ACT : Générer des zones avec un rayon de 2km et min 3 points
        List<Zone> zones = generator.generateZonesFromPoints(points, 2.0, 3);

        // ASSERT
        assertEquals(1, zones.size(), "Il ne devrait y avoir qu'une seule zone valide (Paris).");
        
        Zone parisZone = zones.get(0);
        assertEquals(3, parisZone.getPoints().size(), "La zone Paris devrait contenir 3 points.");
        
        // Vérifier que Marseille n'a pas créé de zone (seulement 2 points)
        // Vérifier que Lyon n'est pas inclus
    }

    @Test
    @DisplayName("Ne doit pas créer de zone si les points sont trop dispersés")
    void testGenerateZones_NoZoneIfDispersed() {
        ZoneGeneratorService generator = new ZoneGeneratorService();
        List<PointSportif> points = new ArrayList<>();

        points.add(new PointSportif(1L, "Paris", 48.8566, 2.3522, null));
        points.add(new PointSportif(2L, "Lyon", 45.7640, 4.8357, null));
        points.add(new PointSportif(3L, "Marseille", 43.2965, 5.3698, null));

        // ACT
        List<Zone> zones = generator.generateZonesFromPoints(points, 50.0, 2); // Rayon 50km

        // ASSERT
        assertTrue(zones.isEmpty(), "Aucune zone ne devrait être créée car les points sont trop loin.");
    }
}
