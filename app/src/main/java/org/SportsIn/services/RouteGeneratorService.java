package org.SportsIn.services;

import org.SportsIn.model.Arene;
import org.SportsIn.model.territory.Route;
import org.SportsIn.utils.GeoUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service pour générer automatiquement des routes sportives basées sur la proximité géographique.
 */
public class RouteGeneratorService {

    /**
     * Génère des routes en reliant les arènes les plus proches (Algorithme Greedy Nearest Neighbor).
     *
     * @param allArenes Liste de toutes les arènes disponibles.
     * @param maxJumpDistanceKm Distance maximale entre deux arènes pour les relier (ex: 1.5 km).
     * @param minArenesPerRoute Taille minimale d'une route pour être validée (ex: 3 arènes).
     * @return Liste des routes générées.
     */
    public List<Route> generateRoutes(List<Arene> allArenes, double maxJumpDistanceKm, int minArenesPerRoute) {
        List<Route> routes = new ArrayList<>();
        Set<String> visitedArenes = new HashSet<>();
        long routeIdCounter = 1;

        for (Arene startArene : allArenes) {
            if (visitedArenes.contains(startArene.getId())) {
                continue;
            }

            List<Arene> currentRouteArenes = new ArrayList<>();
            currentRouteArenes.add(startArene);
            visitedArenes.add(startArene.getId());

            Arene currentArene = startArene;

            while (true) {
                Arene nearestNeighbor = findNearestUnvisitedNeighbor(currentArene, allArenes, visitedArenes, maxJumpDistanceKm);

                if (nearestNeighbor != null) {
                    currentRouteArenes.add(nearestNeighbor);
                    visitedArenes.add(nearestNeighbor.getId());
                    currentArene = nearestNeighbor;
                } else {
                    break;
                }
            }

            if (currentRouteArenes.size() >= minArenesPerRoute) {
                String routeName = "Route " + routeIdCounter + " (" + startArene.getNom() + " -> " + currentArene.getNom() + ")";
                Route route = new Route(routeIdCounter++, routeName, "Générée automatiquement", currentRouteArenes);
                routes.add(route);
            }
        }

        return routes;
    }

    private Arene findNearestUnvisitedNeighbor(Arene current, List<Arene> allArenes, Set<String> visited, double maxDist) {
        Arene nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Arene candidate : allArenes) {
            if (candidate.getId().equals(current.getId()) || visited.contains(candidate.getId())) {
                continue;
            }

            double dist = GeoUtils.calculateDistance(
                    current.getLatitude(), current.getLongitude(),
                    candidate.getLatitude(), candidate.getLongitude()
            );

            if (dist <= maxDist && dist < minDistance) {
                minDistance = dist;
                nearest = candidate;
            }
        }

        return nearest;
    }
}
