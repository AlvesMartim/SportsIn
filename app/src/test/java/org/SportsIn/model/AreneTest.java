package org.SportsIn.model;

import org.SportsIn.model.user.Equipe;
import org.SportsIn.model.user.Joueur;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AreneTest {

    @Test
    void defaultConstructor() {
        Arene arene = new Arene();
        assertNull(arene.getId());
        assertNull(arene.getNom());
    }

    @Test
    void parameterizedConstructor() {
        Arene arene = new Arene("A1", "Parc", 48.856, 2.352);
        assertEquals("A1", arene.getId());
        assertEquals("Parc", arene.getNom());
        assertEquals(48.856, arene.getLatitude(), 0.001);
        assertEquals(2.352, arene.getLongitude(), 0.001);
    }

    @Test
    void controllingTeamId_nullWhenNoTeam() {
        Arene arene = new Arene("A1", "Parc", 0, 0);
        assertNull(arene.getControllingTeamId());
    }

    @Test
    void controllingTeamId_returnsTeamId() {
        Arene arene = new Arene("A1", "Parc", 0, 0);
        Equipe equipe = new Equipe("Rouge");
        equipe.setId(1L);
        arene.setControllingTeam(equipe);
        assertEquals(1L, arene.getControllingTeamId());
    }

    @Test
    void sportsDisponibles_getterSetter() {
        Arene arene = new Arene("A1", "Parc", 0, 0);
        arene.setSportsDisponibles(List.of("FOOTBALL", "BASKET"));
        assertEquals(2, arene.getSportsDisponibles().size());
        assertTrue(arene.getSportsDisponibles().contains("FOOTBALL"));
    }

    @Test
    void toString_containsId() {
        Arene arene = new Arene("A1", "Parc", 48.856, 2.352);
        String str = arene.toString();
        assertTrue(str.contains("A1"));
        assertTrue(str.contains("Parc"));
    }
}
