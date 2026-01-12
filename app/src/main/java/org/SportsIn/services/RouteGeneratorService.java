package org.SportsIn.services;

import org.SportsIn.model.territory.PointSportif;
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
     * Génère des routes en reliant les points les plus proches (Algorithme Greedy Nearest Neighbor).
     *
     * @param allPoints Liste de tous les points disponibles.
     * @param maxJumpDistanceKm Distance maximale entre deux points pour les relier (ex: 1.5 km).
     * @param minPointsPerRoute Taille minimale d'une route pour être validée (ex: 3 points).
     * @return Liste des routes générées.
     */
    public List<Route> generateRoutes(List<PointSportif> allPoints, double maxJumpDistanceKm, int minPointsPerRoute) {
        List<Route> routes = new ArrayList<>();
        Set<Long> visitedPoints = new HashSet<>();
        long routeIdCounter = 1;

        // On essaie de démarrer une route à partir de chaque point non visité
        for (PointSportif startPoint : allPoints) {
            if (visitedPoints.contains(startPoint.getId())) {
                continue;
            }

            List<PointSportif> currentRoutePoints = new ArrayList<>();
            currentRoutePoints.add(startPoint);
            visitedPoints.add(startPoint.getId());

            PointSportif currentPoint = startPoint;

            // On étend la route tant qu'on trouve un voisin proche
            while (true) {
                PointSportif nearestNeighbor = findNearestUnvisitedNeighbor(currentPoint, allPoints, visitedPoints, maxJumpDistanceKm);

                if (nearestNeighbor != null) {
                    currentRoutePoints.add(nearestNeighbor);
                    visitedPoints.add(nearestNeighbor.getId());
                    currentPoint = nearestNeighbor; // On avance
                } else {
                    break; // Plus de voisin, fin de la route
                }
            }

            // Si la route est assez longue, on la garde
            if (currentRoutePoints.size() >= minPointsPerRoute) {
                String routeName = "Route " + routeIdCounter + " (" + startPoint.getNom() + " -> " + currentPoint.getNom() + ")";
                Route route = new Route(routeIdCounter++, routeName, "Générée automatiquement", currentRoutePoints);
                routes.add(route);
            } else {
                // Optionnel : Si la route est trop courte, on pourrait "libérer" les points (retirer de visitedPoints)
                // pour qu'ils puissent être pris par une autre route partant d'ailleurs.
                // Pour simplifier ici, on considère qu'ils sont isolés.
            }
        }

        return routes;
    }

    private PointSportif findNearestUnvisitedNeighbor(PointSportif current, List<PointSportif> allPoints, Set<Long> visited, double maxDist) {
        PointSportif nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (PointSportif candidate : allPoints) {
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
