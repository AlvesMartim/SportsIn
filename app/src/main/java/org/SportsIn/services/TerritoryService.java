package org.SportsIn.services;

import org.SportsIn.model.territory.PointSportif;
import org.SportsIn.model.territory.PointSportifRepository;
import org.SportsIn.model.territory.Zone;
import org.SportsIn.model.territory.ZoneRepository;
import org.SportsIn.model.territory.Route;
import org.SportsIn.model.territory.RouteBonus;
import org.SportsIn.model.territory.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

/**
 * Service responsable de la logique de conquête de territoire (Points et Zones).
 */
@Service
public class TerritoryService {

    private final PointSportifRepository pointRepository;
    private final ZoneRepository zoneRepository;
    private final RouteRepository routeRepository;
    private final RouteService routeService;
    private final RouteGeneratorService routeGeneratorService;

    public TerritoryService(PointSportifRepository pointRepository, ZoneRepository zoneRepository, RouteRepository routeRepository) {
        this.pointRepository = pointRepository;
        this.zoneRepository = zoneRepository;
        this.routeRepository = routeRepository;
        this.routeService = new RouteService();
        this.routeGeneratorService = new RouteGeneratorService();
    }

    /**
     * Génère et initialise les routes automatiquement à partir des points existants.
     * @param maxJumpDistanceKm Distance max entre deux points.
     * @param minPointsPerRoute Nombre min de points pour former une route.
     */
    public void initializeRoutesAutomatically(double maxJumpDistanceKm, int minPointsPerRoute) {
        List<PointSportif> allPoints = pointRepository.findAll();
        List<Route> generatedRoutes = routeGeneratorService.generateRoutes(allPoints, maxJumpDistanceKm, minPointsPerRoute);
        
        routeRepository.deleteAll();
        routeRepository.saveAll(generatedRoutes);
        
        System.out.println(">>> Routes générées automatiquement : " + generatedRoutes.size());
        for (Route r : generatedRoutes) {
            System.out.println("    - " + r.getNom() + " (" + r.getPoints().size() + " points)");
        }
    }

    /**
     * Récupère toutes les routes existantes.
     * @return Liste des routes.
     */
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    /**
     * Récupère le bonus de score applicable pour une équipe sur un point donné.
     * @param teamId L'ID de l'équipe.
     * @param pointId L'ID du point où se déroule l'action.
     * @return Le multiplicateur de bonus (ex: 0.10 pour +10%). Retourne 0.0 si aucun bonus.
     */
    public double getScoreBonusForTeamOnPoint(Long teamId, Long pointId) {
        List<Route> allRoutes = routeRepository.findAll();
        if (allRoutes.isEmpty()) return 0.0;

        List<RouteBonus> bonuses = routeService.calculateBonuses(allRoutes, teamId);
        double totalBonus = 0.0;

        for (RouteBonus bonus : bonuses) {
            // Vérifie si le point actuel fait partie de la route qui donne le bonus
            boolean pointIsOnRoute = bonus.getRoute().getPoints().stream()
                    .anyMatch(p -> p.getId().equals(pointId));
            
            if (pointIsOnRoute && "SCORE_MULTIPLIER".equals(bonus.getBonusType())) {
                totalBonus += bonus.getBonusValue();
            }
        }
        return totalBonus;
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

            // 3. Vérifier les bonus de route
            checkRouteBonuses(winningTeamId);
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

    private void checkRouteBonuses(Long teamId) {
        List<Route> allRoutes = routeRepository.findAll();
        if (allRoutes.isEmpty()) return;

        List<RouteBonus> bonuses = routeService.calculateBonuses(allRoutes, teamId);
        if (!bonuses.isEmpty()) {
            System.out.println(">>> BONUS DE ROUTE ACTIFS pour l'équipe " + teamId + " :");
            for (RouteBonus bonus : bonuses) {
                System.out.println("   - Route: " + bonus.getRoute().getNom() + 
                                   " | Points consécutifs: " + bonus.getConsecutivePoints() + 
                                   " | Bonus: " + bonus.getBonusType() + " (" + (bonus.getBonusValue() * 100) + "%)");
            }
        }
    }
}
