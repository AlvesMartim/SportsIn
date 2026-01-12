package org.SportsIn.config;

import org.SportsIn.services.TerritoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteInitializer {

    @Bean
    public CommandLineRunner initRoutes(TerritoryService territoryService) {
        return args -> {
            System.out.println("--- Initialisation des Routes Sportives ---");
            // Génère des routes en reliant les points distants de max 2.0 km
            // Une route doit contenir au moins 3 points pour être valide.
            territoryService.initializeRoutesAutomatically(2.0, 3);
            System.out.println("--- Fin de l'initialisation des Routes ---");
        };
    }
}
