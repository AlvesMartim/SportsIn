package org.SportsIn.services.strava;

import org.SportsIn.config.StravaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Client HTTP bas niveau pour l'API Strava v3.
 * Toutes les communications réseau avec Strava passent par ici.
 */
@Service
public class StravaApiClient {

    private static final Logger log = LoggerFactory.getLogger(StravaApiClient.class);

    private static final String STRAVA_TOKEN_URL = "https://www.strava.com/oauth/token";
    private static final String STRAVA_API_BASE = "https://www.strava.com/api/v3";

    private final StravaProperties properties;
    private final RestTemplate restTemplate;

    public StravaApiClient(StravaProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Échange un code OAuth contre des tokens Strava.
     */
    public Map<String, Object> exchangeCodeForToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", properties.getClientId());
        params.add("client_secret", properties.getClientSecret());
        params.add("code", code);
        params.add("grant_type", "authorization_code");

        return postToTokenEndpoint(params);
    }

    /**
     * Rafraîchit un access token expiré via le refresh token.
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", properties.getClientId());
        params.add("client_secret", properties.getClientSecret());
        params.add("refresh_token", refreshToken);
        params.add("grant_type", "refresh_token");

        return postToTokenEndpoint(params);
    }

    /**
     * Récupère les activités récentes de l'athlète authentifié.
     *
     * @param accessToken token d'accès valide
     * @param perPage     nombre d'activités max à retourner
     */
    public List<Map<String, Object>> getAthleteActivities(String accessToken, int perPage) {
        String url = STRAVA_API_BASE + "/athlete/activities?per_page=" + perPage;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, request,
                new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new StravaTokenExpiredException("Token Strava expiré ou invalide");
            }
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new StravaRateLimitException("Limite de requêtes Strava atteinte, réessayez dans quelques minutes");
            }
            log.error("Erreur Strava lors de la récupération des activités: {} {}", e.getStatusCode(), e.getMessage());
            throw new StravaApiException("Erreur de l'API Strava: " + e.getMessage());
        }
    }

    /**
     * Récupère une activité précise par son ID Strava.
     */
    public Map<String, Object> getActivityById(String accessToken, Long activityId) {
        String url = STRAVA_API_BASE + "/activities/" + activityId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.GET, request,
                new ParameterizedTypeReference<>() {}
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new IllegalArgumentException("Activité Strava introuvable: " + activityId);
            }
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new StravaTokenExpiredException("Token Strava expiré ou invalide");
            }
            log.error("Erreur Strava lors de la récupération de l'activité {}: {}", activityId, e.getMessage());
            throw new StravaApiException("Erreur de l'API Strava: " + e.getMessage());
        }
    }

    private Map<String, Object> postToTokenEndpoint(MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                STRAVA_TOKEN_URL, HttpMethod.POST, request,
                new ParameterizedTypeReference<>() {}
            );
            if (response.getBody() == null) {
                throw new StravaApiException("Réponse vide de Strava lors de l'échange de token");
            }
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Erreur Strava lors de l'échange de token: {} - {}", e.getStatusCode(), e.getMessage());
            throw new StravaApiException("Échec de l'authentification Strava: " + e.getMessage());
        }
    }

    // --- Exceptions internes ---

    public static class StravaApiException extends RuntimeException {
        public StravaApiException(String message) { super(message); }
    }

    public static class StravaTokenExpiredException extends StravaApiException {
        public StravaTokenExpiredException(String message) { super(message); }
    }

    public static class StravaRateLimitException extends StravaApiException {
        public StravaRateLimitException(String message) { super(message); }
    }
}
