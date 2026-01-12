package org.SportsIn.services;

import org.SportsIn.model.territory.PointSportif;
import org.SportsIn.model.territory.Route;
import org.SportsIn.model.territory.RouteBonus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteServiceTest {

    private RouteService routeService;
    private Route routeRerB;
    private PointSportif pA, pB, pC, pD, pE;
    private Long teamRouge = 1L;
    private Long teamBleu = 2L;

    @BeforeEach
    void setUp() {
        routeService = new RouteService();

        pA = new PointSportif(1L, "A", 0, 0, null);
        pB = new PointSportif(2L, "B", 0, 0, null);
        pC = new PointSportif(3L, "C", 0, 0, null);
        pD = new PointSportif(4L, "D", 0, 0, null);
        pE = new PointSportif(5L, "E", 0, 0, null);

        routeRerB = new Route(100L, "RER B Sud", "A -> E", new ArrayList<>(Arrays.asList(pA, pB, pC, pD, pE)));
    }

    @Test
    void testGetMaxConsecutivePoints_NoControl() {
        int max = routeService.getMaxConsecutivePoints(routeRerB, teamRouge);
        assertEquals(0, max, "Aucun point contrôlé devrait donner 0");
    }

    @Test
    void testGetMaxConsecutivePoints_SinglePoint() {
        pA.setControllingTeamId(teamRouge);
        int max = routeService.getMaxConsecutivePoints(routeRerB, teamRouge);
        assertEquals(1, max, "Un seul point contrôlé devrait donner 1");
    }

    @Test
    void testGetMaxConsecutivePoints_ThreeConsecutive() {
        // A, B, C contrôlés par Rouge
        pA.setControllingTeamId(teamRouge);
        pB.setControllingTeamId(teamRouge);
        pC.setControllingTeamId(teamRouge);
        
        int max = routeService.getMaxConsecutivePoints(routeRerB, teamRouge);
        assertEquals(3, max, "A, B, C contrôlés devrait donner 3");
    }

    @Test
    void testGetMaxConsecutivePoints_BrokenSequence() {
        // A, B contrôlés, C non, D contrôlé
        pA.setControllingTeamId(teamRouge);
        pB.setControllingTeamId(teamRouge);
        // pC neutre
        pD.setControllingTeamId(teamRouge);

        int max = routeService.getMaxConsecutivePoints(routeRerB, teamRouge);
        assertEquals(2, max, "La plus longue suite est A, B (donc 2)");
    }

    @Test
    void testCalculateBonuses_BonusUnlocked() {
        // A, B, C contrôlés par Rouge -> Bonus attendu
        pA.setControllingTeamId(teamRouge);
        pB.setControllingTeamId(teamRouge);
        pC.setControllingTeamId(teamRouge);

        List<RouteBonus> bonuses = routeService.calculateBonuses(Collections.singletonList(routeRerB), teamRouge);
        
        assertFalse(bonuses.isEmpty(), "Un bonus devrait être débloqué");
        assertEquals(1, bonuses.size());
        RouteBonus bonus = bonuses.get(0);
        assertEquals(3, bonus.getConsecutivePoints());
        assertEquals("SCORE_MULTIPLIER", bonus.getBonusType());
        assertEquals(0.10, bonus.getBonusValue(), 0.001);
    }

    @Test
    void testCalculateBonuses_BonusNotUnlocked() {
        // A, B contrôlés par Rouge -> Pas de bonus (min 3)
        pA.setControllingTeamId(teamRouge);
        pB.setControllingTeamId(teamRouge);

        List<RouteBonus> bonuses = routeService.calculateBonuses(Collections.singletonList(routeRerB), teamRouge);
        
        assertTrue(bonuses.isEmpty(), "Pas de bonus pour seulement 2 points consécutifs");
    }
}
