package org.SportsIn.model.strava;

import org.SportsIn.dto.StravaActivityDTO;

import java.util.List;

/**
 * Interface Adapter pour les fournisseurs d'activités sportives externes.
 * Strava est la première implémentation ; Garmin, Apple Health, etc. pourront
 * implémenter cette interface sans modifier la logique métier existante.
 */
public interface ActivityProvider {

    /** Récupère les activités récentes d'un joueur (max {@code limit} activités). */
    List<StravaActivityDTO> fetchRecentActivities(Long joueurId, int limit);

    /** Récupère une activité spécifique par son ID fournisseur. */
    StravaActivityDTO fetchActivity(Long joueurId, String providerActivityId);

    /** Libère les credentials du fournisseur pour ce joueur (déconnexion RGPD). */
    void disconnect(Long joueurId);

    /** Identifiant du fournisseur, ex. "strava". */
    String getProviderName();
}
