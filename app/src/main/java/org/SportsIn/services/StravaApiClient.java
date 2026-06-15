package org.SportsIn.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.SportsIn.config.StravaProperties;
import org.SportsIn.dto.StravaActivityDTO;
import org.SportsIn.dto.StravaTokenResponse;
import org.SportsIn.model.strava.ActivityProvider;
import org.SportsIn.model.strava.StravaAccount;
import org.SportsIn.repository.StravaAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;

/**
 * Client HTTP vers l'API Strava v3.
 * Implémente {@link ActivityProvider} pour permettre de futures substitutions
 * (Garmin, Apple Health, etc.).
 * Gère automatiquement le refresh du token avant chaque appel.
 */
@Service
public class StravaApiClient implements ActivityProvider {

    private static final String STRAVA_TOKEN_URL = "https://www.strava.com/oauth/token";

    private final StravaProperties props;
    private final StravaAccountRepository accountRepo;
    private final StravaRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public StravaApiClient(StravaProperties props,
                           StravaAccountRepository accountRepo,
                           StravaRateLimiter rateLimiter) {
        this.props = props;
        this.accountRepo = accountRepo;
        this.rateLimiter = rateLimiter;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    // ----------------------------------------------------------------
    // ActivityProvider implementation
    // ----------------------------------------------------------------

    @Override
    public List<StravaActivityDTO> fetchRecentActivities(Long joueurId, int limit) {
        StravaAccount account = getAccountOrThrow(joueurId);
        String token = refreshIfNeeded(account);

        if (!rateLimiter.tryAcquire()) {
            throw new StravaRateLimitException("Rate limit Strava atteint. Réessayez dans 15 min.");
        }

        int perPage = Math.min(limit, props.getMaxActivitiesPerSync());
        String url = props.getApiBaseUrl() + "/athlete/activities?per_page=" + perPage;

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new StravaApiException("Strava API error " + resp.statusCode() + ": " + resp.body());
            }
            return objectMapper.readValue(resp.body(), new TypeReference<List<StravaActivityDTO>>() {});

        } catch (StravaApiException | StravaRateLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new StravaApiException("Erreur lors de la récupération des activités Strava", e);
        }
    }

    @Override
    public StravaActivityDTO fetchActivity(Long joueurId, String providerActivityId) {
        StravaAccount account = getAccountOrThrow(joueurId);
        String token = refreshIfNeeded(account);

        if (!rateLimiter.tryAcquire()) {
            throw new StravaRateLimitException("Rate limit Strava atteint.");
        }

        String url = props.getApiBaseUrl() + "/activities/" + providerActivityId;

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new StravaApiException("Strava API error " + resp.statusCode() + ": " + resp.body());
            }
            return objectMapper.readValue(resp.body(), StravaActivityDTO.class);

        } catch (StravaApiException | StravaRateLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new StravaApiException("Erreur lors de la récupération de l'activité " + providerActivityId, e);
        }
    }

    @Override
    @Transactional
    public void disconnect(Long joueurId) {
        accountRepo.deleteByJoueurId(joueurId);
    }

    @Override
    public String getProviderName() {
        return "strava";
    }

    // ----------------------------------------------------------------
    // OAuth token exchange (utilisé par StravaOAuthService)
    // ----------------------------------------------------------------

    /**
     * Échange un code OAuth contre des tokens Strava.
     */
    public StravaTokenResponse exchangeCodeForTokens(String code) {
        String body = "client_id=" + props.getClientId()
                + "&client_secret=" + props.getClientSecret()
                + "&code=" + code
                + "&grant_type=authorization_code";
        return postToTokenUrl(body);
    }

    /**
     * Rafraîchit les tokens avec le refresh_token.
     */
    public StravaTokenResponse refreshToken(String refreshToken) {
        String body = "client_id=" + props.getClientId()
                + "&client_secret=" + props.getClientSecret()
                + "&refresh_token=" + refreshToken
                + "&grant_type=refresh_token";
        return postToTokenUrl(body);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private StravaTokenResponse postToTokenUrl(String formBody) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(STRAVA_TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            StravaTokenResponse tokenResp = objectMapper.readValue(resp.body(), StravaTokenResponse.class);
            if (tokenResp.isError()) {
                throw new StravaApiException("Erreur token Strava: " + tokenResp.getMessage());
            }
            return tokenResp;

        } catch (StravaApiException e) {
            throw e;
        } catch (Exception e) {
            throw new StravaApiException("Erreur lors de l'échange de tokens Strava", e);
        }
    }

    @Transactional
    private String refreshIfNeeded(StravaAccount account) {
        Instant expiresAt = Instant.parse(account.getTokenExpiresAt());
        // Rafraîchir si le token expire dans moins de 5 minutes
        if (Instant.now().plusSeconds(300).isAfter(expiresAt)) {
            StravaTokenResponse refreshed = refreshToken(account.getRefreshToken());
            account.setAccessToken(refreshed.getAccessToken());
            account.setRefreshToken(refreshed.getRefreshToken());
            account.setTokenExpiresAt(Instant.ofEpochSecond(refreshed.getExpiresAt()).toString());
            accountRepo.save(account);
        }
        return account.getAccessToken();
    }

    private StravaAccount getAccountOrThrow(Long joueurId) {
        return accountRepo.findByJoueurId(joueurId)
                .orElseThrow(() -> new IllegalStateException(
                        "Aucun compte Strava lié pour le joueur " + joueurId));
    }

    // ----------------------------------------------------------------
    // Exceptions internes
    // ----------------------------------------------------------------

    public static class StravaApiException extends RuntimeException {
        public StravaApiException(String msg) { super(msg); }
        public StravaApiException(String msg, Throwable cause) { super(msg, cause); }
    }

    public static class StravaRateLimitException extends RuntimeException {
        public StravaRateLimitException(String msg) { super(msg); }
    }
}
