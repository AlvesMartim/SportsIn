package org.SportsIn.services;

import org.SportsIn.model.territory.PointSportif;
import org.SportsIn.model.territory.Zone;
import org.SportsIn.utils.GeoUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ZoneGeneratorService {

    /**
     * Génère automatiquement des zones en regroupant les points proches.
     *
     * @param allPoints La liste complète des points sportifs disponibles.
     * @param radiusKm Le rayon de recherche en kilomètres pour grouper les points.
     * @param minPointsPerZone Le nombre minimum de points pour former une zone valide.
     * @return Une liste de zones générées.
     */
    public List<Zone> generateZonesFromPoints(List<PointSportif> allPoints, double radiusKm, int minPointsPerZone) {
        List<Zone> generatedZones = new ArrayList<>();
        Set<Long> assignedPointIds = new HashSet<>();
        long zoneIdCounter = 1;

        for (PointSportif centerPoint : allPoints) {
            // Si le point est déjà dans une zone, on passe
            if (assignedPointIds.contains(centerPoint.getId())) {
                continue;
            }

            // Créer un nouveau groupe potentiel avec ce point comme centre
            List<PointSportif> currentGroup = new ArrayList<>();
            currentGroup.add(centerPoint);

            // Chercher les voisins proches qui ne sont pas encore assignés
            for (PointSportif candidate : allPoints) {
                if (!candidate.getId().equals(centerPoint.getId()) && !assignedPointIds.contains(candidate.getId())) {
                    double distance = GeoUtils.calculateDistance(
                            centerPoint.getLatitude(), centerPoint.getLongitude(),
                            candidate.getLatitude(), candidate.getLongitude()
                    );

                    if (distance <= radiusKm) {
                        currentGroup.add(candidate);
                    }
                }
            }

            // Si le groupe est assez grand, on crée une zone
            if (currentGroup.size() >= minPointsPerZone) {
                Zone newZone = new Zone(
                        zoneIdCounter++,
                        "Zone Auto " + zoneIdCounter + " (Centre: " + centerPoint.getNom() + ")",
                        currentGroup
                );
                
                generatedZones.add(newZone);

                // Marquer ces points comme assignés
                for (PointSportif p : currentGroup) {
                    assignedPointIds.add(p.getId());
                }
            }
        }

        return generatedZones;
    }
}
