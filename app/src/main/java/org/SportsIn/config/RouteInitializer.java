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
            System.out.println("--- Initialisation des Zones & Routes Sportives ---");
            // Chaque arène forme sa propre zone (arènes réparties sur toute la France)
            territoryService.initializeZonesAutomatically(50.0, 1);
            // Routes entre arènes proches (max 2 km)
            territoryService.initializeRoutesAutomatically(2.0, 3);
            System.out.println("--- Fin de l'initialisation ---");
        };
    }
}
