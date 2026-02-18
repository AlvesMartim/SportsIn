package org.SportsIn.services;

import org.SportsIn.model.Arene;
import org.SportsIn.model.territory.Zone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZoneGeneratorServiceTest {

    @Test
    @DisplayName("Doit regrouper les arènes proches dans une même zone")
    void testGenerateZones_GroupsNearbyArenes() {
        ZoneGeneratorService generator = new ZoneGeneratorService();
        List<Arene> arenes = new ArrayList<>();

        // Groupe 1 : Paris Centre (3 arènes proches)
        arenes.add(new Arene("1", "Châtelet", 48.8584, 2.3470));
        arenes.add(new Arene("2", "Louvre", 48.8606, 2.3376));
        arenes.add(new Arene("3", "Notre-Dame", 48.8530, 2.3499));

        // Groupe 2 : Marseille (2 arènes, trop loin de Paris)
        arenes.add(new Arene("4", "Vieux Port", 43.2951, 5.3744));
        arenes.add(new Arene("5", "Vélodrome", 43.2698, 5.3959));

        // Arène isolée : Lyon
        arenes.add(new Arene("6", "Lyon Part-Dieu", 45.7601, 4.8590));

        // ACT : Générer des zones avec un rayon de 2km et min 3 arènes
        List<Zone> zones = generator.generateZonesFromArenes(arenes, 2.0, 3);

        // ASSERT
        assertEquals(1, zones.size(), "Il ne devrait y avoir qu'une seule zone valide (Paris).");
        
        Zone parisZone = zones.get(0);
        assertEquals(3, parisZone.getArenes().size(), "La zone Paris devrait contenir 3 arènes.");
    }

    @Test
    @DisplayName("Ne doit pas créer de zone si les arènes sont trop dispersées")
    void testGenerateZones_NoZoneIfDispersed() {
        ZoneGeneratorService generator = new ZoneGeneratorService();
        List<Arene> arenes = new ArrayList<>();

        arenes.add(new Arene("1", "Paris", 48.8566, 2.3522));
        arenes.add(new Arene("2", "Lyon", 45.7640, 4.8357));
        arenes.add(new Arene("3", "Marseille", 43.2965, 5.3698));

        // ACT
        List<Zone> zones = generator.generateZonesFromArenes(arenes, 50.0, 2);

        // ASSERT
        assertTrue(zones.isEmpty(), "Aucune zone ne devrait être créée car les arènes sont trop loin.");
    }
}
