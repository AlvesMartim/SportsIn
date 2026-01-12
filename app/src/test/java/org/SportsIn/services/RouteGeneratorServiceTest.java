package org.SportsIn.services;

import org.SportsIn.model.territory.PointSportif;
import org.SportsIn.model.territory.Route;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteGeneratorServiceTest {

    @Test
    void testGenerateRoutes_LinearChain() {
        RouteGeneratorService generator = new RouteGeneratorService();

        // Création de points alignés géographiquement (approximativement)
        // 1 degré de latitude ~= 111 km. 0.01 deg ~= 1.1 km.
        PointSportif p1 = new PointSportif(1L, "P1", 48.850, 2.350, null);
        PointSportif p2 = new PointSportif(2L, "P2", 48.860, 2.350, null); // ~1.1km de P1
        PointSportif p3 = new PointSportif(3L, "P3", 48.870, 2.350, null); // ~1.1km de P2
        PointSportif p4 = new PointSportif(4L, "P4", 48.950, 2.350, null); // Loin (~9km de P3)

        List<PointSportif> points = Arrays.asList(p1, p2, p3, p4);

        // On cherche des sauts max de 2km, min 3 points par route
        List<Route> routes = generator.generateRoutes(points, 2.0, 3);

        assertEquals(1, routes.size(), "Devrait générer une seule route");
        Route route = routes.get(0);
        assertEquals(3, route.getPoints().size(), "La route devrait contenir P1, P2, P3");
        
        // Vérification de l'ordre (P1 -> P2 -> P3 ou inverse selon l'ordre de la liste d'entrée)
        // Comme l'algo prend le premier de la liste (P1), il devrait faire P1 -> P2 -> P3
        assertEquals(1L, route.getPoints().get(0).getId());
        assertEquals(2L, route.getPoints().get(1).getId());
        assertEquals(3L, route.getPoints().get(2).getId());
    }

    @Test
    void testGenerateRoutes_TooFar() {
        RouteGeneratorService generator = new RouteGeneratorService();

        PointSportif p1 = new PointSportif(1L, "P1", 48.850, 2.350, null);
        PointSportif p2 = new PointSportif(2L, "P2", 48.900, 2.350, null); // ~5.5km

        List<PointSportif> points = Arrays.asList(p1, p2);

        // Max jump 2km
        List<Route> routes = generator.generateRoutes(points, 2.0, 2);

        assertTrue(routes.isEmpty(), "Aucune route ne devrait être générée car points trop loin");
    }
}
