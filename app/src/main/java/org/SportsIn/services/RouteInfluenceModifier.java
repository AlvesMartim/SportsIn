package org.SportsIn.services;

import org.SportsIn.model.territory.Route;
import org.SportsIn.model.territory.RouteBonus;
import org.SportsIn.model.territory.RouteRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteInfluenceModifier implements InfluenceModifier {

    private final RouteRepository routeRepository;
    private final RouteService routeService;

    public RouteInfluenceModifier(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
        this.routeService = new RouteService();
    }

    @Override
    public double apply(Long teamId, String pointId, double currentModifier) {
        List<Route> allRoutes = routeRepository.findAll();
        if (allRoutes.isEmpty()) return currentModifier;

        List<RouteBonus> bonuses = routeService.calculateBonuses(allRoutes, teamId);
        double routeBonus = 0.0;

        for (RouteBonus bonus : bonuses) {
            boolean pointIsOnRoute = bonus.getRoute().getArenes().stream()
                    .anyMatch(a -> a.getId().equals(pointId));
            if (pointIsOnRoute && "SCORE_MULTIPLIER".equals(bonus.getBonusType())) {
                routeBonus += bonus.getBonusValue();
            }
        }

        return currentModifier + routeBonus;
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
