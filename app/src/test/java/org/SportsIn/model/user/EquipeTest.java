package org.SportsIn.model.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EquipeTest {

    @Test
    void defaultConstructor() {
        Equipe equipe = new Equipe();
        assertNull(equipe.getId());
        assertNull(equipe.getNom());
        assertEquals(0, equipe.getPoints());
        assertEquals(0, equipe.getXp());
        assertNotNull(equipe.getMembres());
        assertTrue(equipe.getMembres().isEmpty());
    }

    @Test
    void nomConstructor() {
        Equipe equipe = new Equipe("Rouge");
        assertEquals("Rouge", equipe.getNom());
    }

    @Test
    void nomCouleurConstructor() {
        Equipe equipe = new Equipe("Rouge", "#FF0000");
        assertEquals("Rouge", equipe.getNom());
        assertEquals("#FF0000", equipe.getCouleur());
    }

    @Test
    void settersAndGetters() {
        Equipe equipe = new Equipe();
        equipe.setId(1L);
        equipe.setNom("Bleu");
        equipe.setPoints(100);
        equipe.setXp(500);
        equipe.setCouleur("#0000FF");

        assertEquals(1L, equipe.getId());
        assertEquals("Bleu", equipe.getNom());
        assertEquals(100, equipe.getPoints());
        assertEquals(500, equipe.getXp());
        assertEquals("#0000FF", equipe.getCouleur());
    }

    @Test
    void addJoueur_addsAndSetsBackRef() {
        Equipe equipe = new Equipe("Rouge");
        Joueur joueur = new Joueur();
        joueur.setPseudo("John");

        equipe.addJoueur(joueur);
        assertEquals(1, equipe.getMembres().size());
        assertEquals(equipe, joueur.getEquipe());
    }

    @Test
    void removeJoueur_removesAndClearsBackRef() {
        Equipe equipe = new Equipe("Rouge");
        Joueur joueur = new Joueur();
        joueur.setPseudo("John");

        equipe.addJoueur(joueur);
        equipe.removeJoueur(joueur);
        assertTrue(equipe.getMembres().isEmpty());
        assertNull(joueur.getEquipe());
    }

    @Test
    void toString_containsId() {
        Equipe equipe = new Equipe("Test");
        equipe.setId(5L);
        String str = equipe.toString();
        assertTrue(str.contains("5"));
        assertTrue(str.contains("Test"));
    }
}
