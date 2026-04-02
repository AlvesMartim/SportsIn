package org.SportsIn.services;

import org.SportsIn.model.Arene;
import org.SportsIn.model.territory.Zone;
import org.SportsIn.model.territory.ZoneRepository;
import org.SportsIn.model.territory.Route;
import org.SportsIn.model.territory.RouteBonus;
import org.SportsIn.model.territory.RouteRepository;
import org.SportsIn.repository.AreneRepository;
import org.SportsIn.repository.EquipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsable de la logique de conquête de territoire (Arènes et Zones).
 */
@Service
public class TerritoryService {

    private static final double DEFAULT_SESSION_INFLUENCE_GAIN = 25.0;
    private static final double CAPTURE_BASE_INFLUENCE = 35.0;

    private final AreneRepository areneRepository;
    private final EquipeRepository equipeRepository;
    private final ZoneRepository zoneRepository;
    private final RouteRepository routeRepository;
    private final RouteService routeService;
    private final RouteGeneratorService routeGeneratorService;
    private final InfluenceCalculator influenceCalculator;
    private final TerritoryInfluenceStateService territoryInfluenceStateService;

    @Autowired
    public TerritoryService(AreneRepository areneRepository,
                            EquipeRepository equipeRepository,
                            ZoneRepository zoneRepository,
                            RouteRepository routeRepository,
                            InfluenceCalculator influenceCalculator,
                            TerritoryInfluenceStateService territoryInfluenceStateService) {
        this.areneRepository = areneRepository;
        this.equipeRepository = equipeRepository;
        this.zoneRepository = zoneRepository;
        this.routeRepository = routeRepository;
        this.routeService = new RouteService();
        this.routeGeneratorService = new RouteGeneratorService();
        this.influenceCalculator = influenceCalculator;
        this.territoryInfluenceStateService = territoryInfluenceStateService;
    }

    public TerritoryService(AreneRepository areneRepository,
                            EquipeRepository equipeRepository,
                            ZoneRepository zoneRepository,
                            RouteRepository routeRepository,
                            InfluenceCalculator influenceCalculator) {
        this.areneRepository = areneRepository;
        this.equipeRepository = equipeRepository;
        this.zoneRepository = zoneRepository;
        this.routeRepository = routeRepository;
        this.routeService = new RouteService();
        this.routeGeneratorService = new RouteGeneratorService();
        this.influenceCalculator = influenceCalculator;
        this.territoryInfluenceStateService = new TerritoryInfluenceStateService();
    }

    /**
     * Génère et initialise les routes automatiquement à partir des arènes existantes.
     * @param maxJumpDistanceKm Distance max entre deux arènes.
     * @param minArenesPerRoute Nombre min d'arènes pour former une route.
     */
    public void initializeRoutesAutomatically(double maxJumpDistanceKm, int minArenesPerRoute) {
        List<Arene> allArenes = areneRepository.findAll();
        List<Route> generatedRoutes = routeGeneratorService.generateRoutes(allArenes, maxJumpDistanceKm, minArenesPerRoute);
        
        routeRepository.deleteAll();
        routeRepository.saveAll(generatedRoutes);
        
        System.out.println(">>> Routes générées automatiquement : " + generatedRoutes.size());
        for (Route r : generatedRoutes) {
            System.out.println("    - " + r.getNom() + " (" + r.getArenes().size() + " arènes)");
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
     * Récupère le bonus de score applicable pour une équipe sur une arène donnée.
     * @param teamId L'ID de l'équipe.
     * @param areneId L'ID de l'arène où se déroule l'action.
     * @return Le multiplicateur de bonus (ex: 0.10 pour +10%). Retourne 0.0 si aucun bonus.
     */
    public double getScoreBonusForTeamOnPoint(Long teamId, String areneId) {
        return influenceCalculator.computeTotalModifier(teamId, areneId);
    }

    /**
     * Appelé lorsqu'une équipe gagne une session sur une arène.
     * Met à jour l'arène et vérifie si cela déclenche la capture d'une zone.
     *
     * @param areneId L'ID de l'arène concernée.
     * @param winningTeamId L'ID de l'équipe gagnante.
     */
    public void updateTerritoryControl(String areneId, Long winningTeamId) {
        updateTerritoryControl(areneId, winningTeamId, DEFAULT_SESSION_INFLUENCE_GAIN);
    }

    /**
     * Variante avec gain d'influence explicite (Hard Mode meteo, perks, routes).
     */
    public void updateTerritoryControl(String areneId, Long winningTeamId, double influenceGain) {
        areneRepository.findById(areneId).ifPresent(arene -> {
            Long oldOwner = arene.getControllingTeamId();
            double sanitizedGain = Math.max(5.0, influenceGain);
            
            if (winningTeamId.equals(oldOwner)) {
                double level = territoryInfluenceStateService.reinforce(areneId, sanitizedGain * 0.30);
                System.out.println("Arène " + arene.getNom() + " défendue avec succès par l'équipe " + winningTeamId
                        + " | influence=" + String.format("%.1f", level));
                return;
            }

            System.out.println("Arène " + arene.getNom() + " (ID: " + areneId + ") passe de l'équipe " + oldOwner + " à l'équipe " + winningTeamId);
            
            equipeRepository.findById(winningTeamId).ifPresent(equipe -> {
                arene.setControllingTeam(equipe);
                areneRepository.save(arene);

                double seededLevel = CAPTURE_BASE_INFLUENCE + Math.min(50.0, sanitizedGain * 0.50);
                double level = territoryInfluenceStateService.setLevel(areneId, seededLevel);
                System.out.println(">>> Influence territoriale initialisee sur " + arene.getNom() + " a "
                        + String.format("%.1f", level));
            });

            // Vérifier les zones impactées
            checkZonesImpactedByArene(areneId);

            // Vérifier les bonus de route
            checkRouteBonuses(winningTeamId);
        });
    }

    public double getCurrentInfluenceLevel(String areneId) {
        return territoryInfluenceStateService.getInfluenceLevel(areneId);
    }

    private void checkZonesImpactedByArene(String areneId) {
        List<Zone> impactedZones = zoneRepository.findZonesByAreneId(areneId);
        
        for (Zone zone : impactedZones) {
            boolean changed = zone.updateZoneControl();
            
            if (changed) {
                Long newZoneOwner = zone.getControllingTeamId();
                zoneRepository.save(zone);
                
                if (newZoneOwner != null) {
                    System.out.println(">>> ZONE CONQUISE ! La zone '" + zone.getNom() + "' est maintenant contrôlée par l'équipe " + newZoneOwner);
                } else {
                    System.out.println(">>> ZONE PERDUE ! La zone '" + zone.getNom() + "' est redevenue neutre/contestée.");
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
                                   " | Arènes consécutives: " + bonus.getConsecutivePoints() + 
                                   " | Bonus: " + bonus.getBonusType() + " (" + (bonus.getBonusValue() * 100) + "%)");
            }
        }
    }
}
