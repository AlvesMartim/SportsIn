package org.SportsIn.services;

import org.SportsIn.model.territory.PointSportif;
import org.SportsIn.model.territory.PointSportifRepository;
import org.SportsIn.model.territory.Zone;
import org.SportsIn.model.territory.ZoneRepository;

import java.util.List;

/**
 * Service responsable de la logique de conquête de territoire (Points et Zones).
 */
public class TerritoryService {

    private final PointSportifRepository pointRepository;
    private final ZoneRepository zoneRepository;

    public TerritoryService(PointSportifRepository pointRepository, ZoneRepository zoneRepository) {
        this.pointRepository = pointRepository;
        this.zoneRepository = zoneRepository;
    }

    /**
     * Appelé lorsqu'une équipe gagne une session sur un point.
     * Met à jour le point et vérifie si cela déclenche la capture d'une zone.
     *
     * @param pointId L'ID du point concerné.
     * @param winningTeamId L'ID de l'équipe gagnante.
     */
    public void updateTerritoryControl(Long pointId, Long winningTeamId) {
        // 1. Mettre à jour le point
        pointRepository.findById(pointId).ifPresent(point -> {
            Long oldOwner = point.getControllingTeamId();
            
            // Si l'équipe contrôle déjà le point, pas besoin de recalculer les zones
            // sauf si on veut gérer des compteurs de défense, mais ici c'est simple.
            if (winningTeamId.equals(oldOwner)) {
                System.out.println("Point " + point.getNom() + " défendu avec succès par l'équipe " + winningTeamId);
                return;
            }

            System.out.println("Point " + point.getNom() + " (ID: " + pointId + ") passe de l'équipe " + oldOwner + " à l'équipe " + winningTeamId);
            point.setControllingTeamId(winningTeamId);
            pointRepository.save(point);

            // 2. Vérifier les zones impactées
            checkZonesImpactedByPoint(pointId);
        });
    }

    private void checkZonesImpactedByPoint(Long pointId) {
        List<Zone> impactedZones = zoneRepository.findZonesByPointId(pointId);
        
        for (Zone zone : impactedZones) {
            Long oldZoneOwner = zone.getControllingTeamId();
            boolean changed = zone.updateZoneControl();
            
            if (changed) {
                Long newZoneOwner = zone.getControllingTeamId();
                zoneRepository.save(zone);
                
                if (newZoneOwner != null) {
                    System.out.println(">>> ZONE CONQUISE ! La zone '" + zone.getNom() + "' est maintenant contrôlée par l'équipe " + newZoneOwner);
                } else {
                    System.out.println(">>> ZONE PERDUE ! La zone '" + zone.getNom() + "' est redevenue neutre/contestée (plus d'équipe avec 3 points).");
                }
            }
        }
    }
}
