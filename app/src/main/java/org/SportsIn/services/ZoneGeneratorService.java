package org.SportsIn.services;

import org.SportsIn.model.Arene;
import org.SportsIn.model.territory.Zone;
import org.SportsIn.utils.GeoUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ZoneGeneratorService {

    /**
     * Génère automatiquement des zones en regroupant les arènes proches.
     *
     * @param allArenes La liste complète des arènes disponibles.
     * @param radiusKm Le rayon de recherche en kilomètres pour grouper les arènes.
     * @param minArenesPerZone Le nombre minimum d'arènes pour former une zone valide.
     * @return Une liste de zones générées.
     */
    public List<Zone> generateZonesFromArenes(List<Arene> allArenes, double radiusKm, int minArenesPerZone) {
        List<Zone> generatedZones = new ArrayList<>();
        Set<String> assignedAreneIds = new HashSet<>();
        long zoneIdCounter = 1;

        for (Arene centerArene : allArenes) {
            if (assignedAreneIds.contains(centerArene.getId())) {
                continue;
            }

            List<Arene> currentGroup = new ArrayList<>();
            currentGroup.add(centerArene);

            for (Arene candidate : allArenes) {
                if (!candidate.getId().equals(centerArene.getId()) && !assignedAreneIds.contains(candidate.getId())) {
                    double distance = GeoUtils.calculateDistance(
                            centerArene.getLatitude(), centerArene.getLongitude(),
                            candidate.getLatitude(), candidate.getLongitude()
                    );

                    if (distance <= radiusKm) {
                        currentGroup.add(candidate);
                    }
                }
            }

            if (currentGroup.size() >= minArenesPerZone) {
                Zone newZone = new Zone(
                        zoneIdCounter++,
                        "Zone Auto " + zoneIdCounter + " (Centre: " + centerArene.getNom() + ")",
                        currentGroup
                );
                
                generatedZones.add(newZone);

                for (Arene a : currentGroup) {
                    assignedAreneIds.add(a.getId());
                }
            }
        }

        return generatedZones;
    }
}
