package org.SportsIn.model.mission;

import java.util.Map;

/**
 * Centralise les clés et catégories utilisées dans le payload JSON des missions.
 * <p>
 * Les missions {@link MissionType#DIVERSITY_SPORT} sont surchargées : la sous-catégorie
 * {@link #WEATHER_FLASH_CATEGORY} identifie les missions générées par le moteur météo
 * (Feature 7). Tout traitement générique sur DIVERSITY_SPORT doit utiliser
 * {@link #isWeatherFlashPayload(Map)} pour distinguer les deux familles, sinon les
 * comportements peuvent se croiser de façon inattendue.
 */
public final class MissionPayloadKeys {

    public static final String CATEGORY = "missionCategory";
    public static final String WEATHER_FLASH_CATEGORY = "WEATHER_FLASH";

    private MissionPayloadKeys() {
    }

    public static boolean isWeatherFlashPayload(Map<String, Object> payload) {
        if (payload == null) {
            return false;
        }
        Object category = payload.get(CATEGORY);
        return category != null && WEATHER_FLASH_CATEGORY.equalsIgnoreCase(category.toString());
    }
}
