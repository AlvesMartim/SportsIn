package org.SportsIn.controller;

import org.SportsIn.model.territory.Route;
import org.SportsIn.services.TerritoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final TerritoryService territoryService;

    public RouteController(TerritoryService territoryService) {
        this.territoryService = territoryService;
    }

    @GetMapping
    public List<Route> getAllRoutes() {
        return territoryService.getAllRoutes();
    }
}
