package org.SportsIn.services;

import org.SportsIn.model.Arene;
import org.SportsIn.model.territory.Route;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteGeneratorServiceTest {

    @Test
    void testGenerateRoutes_LinearChain() {
        RouteGeneratorService generator = new RouteGeneratorService();

        Arene a1 = new Arene("1", "P1", 48.850, 2.350);
        Arene a2 = new Arene("2", "P2", 48.860, 2.350); // ~1.1km de A1
        Arene a3 = new Arene("3", "P3", 48.870, 2.350); // ~1.1km de A2
        Arene a4 = new Arene("4", "P4", 48.950, 2.350); // Loin (~9km de A3)

        List<Arene> arenes = Arrays.asList(a1, a2, a3, a4);

        List<Route> routes = generator.generateRoutes(arenes, 2.0, 3);

        assertEquals(1, routes.size(), "Devrait générer une seule route");
        Route route = routes.get(0);
        assertEquals(3, route.getArenes().size(), "La route devrait contenir A1, A2, A3");
        
        assertEquals("1", route.getArenes().get(0).getId());
        assertEquals("2", route.getArenes().get(1).getId());
        assertEquals("3", route.getArenes().get(2).getId());
    }

    @Test
    void testGenerateRoutes_TooFar() {
        RouteGeneratorService generator = new RouteGeneratorService();

        Arene a1 = new Arene("1", "P1", 48.850, 2.350);
        Arene a2 = new Arene("2", "P2", 48.900, 2.350); // ~5.5km

        List<Arene> arenes = Arrays.asList(a1, a2);

        List<Route> routes = generator.generateRoutes(arenes, 2.0, 2);

        assertTrue(routes.isEmpty(), "Aucune route ne devrait être générée car arènes trop loin");
    }
}
