package org.SportsIn.services;

import org.SportsIn.model.Arene;
import org.SportsIn.model.territory.InMemoryRouteRepository;
import org.SportsIn.model.territory.Route;
import org.SportsIn.model.user.Equipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteInfluenceModifierTest {

    private RouteInfluenceModifier modifier;
    private InMemoryRouteRepository routeRepository;

    @BeforeEach
    void setUp() {
        routeRepository = new InMemoryRouteRepository();
        modifier = new RouteInfluenceModifier(routeRepository);
    }

    @Test
    void getOrder_returns10() {
        assertEquals(10, modifier.getOrder());
    }

    @Test
    void apply_noRoutes_returnsCurrentModifier() {
        double result = modifier.apply(1L, "arena1", 1.0);
        assertEquals(1.0, result, 0.001);
    }

    @Test
    void apply_routeWithBonus_addsBonus() {
        Equipe equipe = new Equipe("Rouge");
        equipe.setId(1L);

        Arene a1 = new Arene("A1", "A", 0, 0);
        a1.setControllingTeam(equipe);
        Arene a2 = new Arene("A2", "B", 0, 0);
        a2.setControllingTeam(equipe);
        Arene a3 = new Arene("A3", "C", 0, 0);
        a3.setControllingTeam(equipe);

        Route route = new Route(1L, "Route Test", "desc", List.of(a1, a2, a3));
        routeRepository.save(route);

        // Team 1 controls 3 consecutive arenas → bonus 0.10
        double result = modifier.apply(1L, "A1", 1.0);
        assertEquals(1.10, result, 0.001);
    }

    @Test
    void apply_pointNotOnRoute_noBonus() {
        Equipe equipe = new Equipe("Rouge");
        equipe.setId(1L);

        Arene a1 = new Arene("A1", "A", 0, 0);
        a1.setControllingTeam(equipe);
        Arene a2 = new Arene("A2", "B", 0, 0);
        a2.setControllingTeam(equipe);
        Arene a3 = new Arene("A3", "C", 0, 0);
        a3.setControllingTeam(equipe);

        Route route = new Route(2L, "Route Test", "desc", List.of(a1, a2, a3));
        routeRepository.save(route);

        // Point "X9" is not on the route
        double result = modifier.apply(1L, "X9", 1.0);
        assertEquals(1.0, result, 0.001);
    }

    @Test
    void apply_insufficientControlNoBonus() {
        Equipe equipe1 = new Equipe("Rouge");
        equipe1.setId(1L);
        Equipe equipe2 = new Equipe("Bleu");
        equipe2.setId(2L);

        Arene a1 = new Arene("A1", "A", 0, 0);
        a1.setControllingTeam(equipe1);
        Arene a2 = new Arene("A2", "B", 0, 0);
        a2.setControllingTeam(equipe2); // different team breaks the chain
        Arene a3 = new Arene("A3", "C", 0, 0);
        a3.setControllingTeam(equipe1);

        Route route = new Route(3L, "Route Test", "desc", List.of(a1, a2, a3));
        routeRepository.save(route);

        double result = modifier.apply(1L, "A1", 1.0);
        assertEquals(1.0, result, 0.001); // no bonus since chain is broken
    }
}
