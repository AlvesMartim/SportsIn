package org.SportsIn.services;

import org.SportsIn.model.Arene;
import org.SportsIn.model.user.Equipe;
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
    private Arene aA, aB, aC, aD, aE;
    private Equipe equipeRouge;
    private Equipe equipeBleu;
    private Long teamRouge = 1L;
    private Long teamBleu = 2L;

    @BeforeEach
    void setUp() {
        routeService = new RouteService();

        equipeRouge = new Equipe("Rouge");
        equipeRouge.setId(teamRouge);
        equipeBleu = new Equipe("Bleu");
        equipeBleu.setId(teamBleu);

        aA = new Arene("1", "A", 0, 0);
        aB = new Arene("2", "B", 0, 0);
        aC = new Arene("3", "C", 0, 0);
        aD = new Arene("4", "D", 0, 0);
        aE = new Arene("5", "E", 0, 0);

        routeRerB = new Route(100L, "RER B Sud", "A -> E", new ArrayList<>(Arrays.asList(aA, aB, aC, aD, aE)));
    }

    @Test
    void testGetMaxConsecutiveArenes_NoControl() {
        int max = routeService.getMaxConsecutiveArenes(routeRerB, teamRouge);
        assertEquals(0, max, "Aucune arène contrôlée devrait donner 0");
    }

    @Test
    void testGetMaxConsecutiveArenes_SingleArene() {
        aA.setControllingTeam(equipeRouge);
        int max = routeService.getMaxConsecutiveArenes(routeRerB, teamRouge);
        assertEquals(1, max, "Une seule arène contrôlée devrait donner 1");
    }

    @Test
    void testGetMaxConsecutiveArenes_ThreeConsecutive() {
        aA.setControllingTeam(equipeRouge);
        aB.setControllingTeam(equipeRouge);
        aC.setControllingTeam(equipeRouge);
        
        int max = routeService.getMaxConsecutiveArenes(routeRerB, teamRouge);
        assertEquals(3, max, "A, B, C contrôlés devrait donner 3");
    }

    @Test
    void testGetMaxConsecutiveArenes_BrokenSequence() {
        aA.setControllingTeam(equipeRouge);
        aB.setControllingTeam(equipeRouge);
        // aC neutre
        aD.setControllingTeam(equipeRouge);

        int max = routeService.getMaxConsecutiveArenes(routeRerB, teamRouge);
        assertEquals(2, max, "La plus longue suite est A, B (donc 2)");
    }

    @Test
    void testCalculateBonuses_BonusUnlocked() {
        aA.setControllingTeam(equipeRouge);
        aB.setControllingTeam(equipeRouge);
        aC.setControllingTeam(equipeRouge);

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
        aA.setControllingTeam(equipeRouge);
        aB.setControllingTeam(equipeRouge);

        List<RouteBonus> bonuses = routeService.calculateBonuses(Collections.singletonList(routeRerB), teamRouge);
        
        assertTrue(bonuses.isEmpty(), "Pas de bonus pour seulement 2 arènes consécutives");
    }
}
