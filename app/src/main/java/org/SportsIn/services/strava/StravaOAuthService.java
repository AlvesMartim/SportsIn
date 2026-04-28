package org.SportsIn.services.strava;

import org.SportsIn.config.StravaProperties;
import org.SportsIn.model.strava.JoueurStravaData;
import org.SportsIn.repository.JoueurStravaDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

/**
 * Gère le flux OAuth2 Strava : construction de l'URL d'autorisation,
 * échange du code, et refresh automatique du token.
 */
@Service
public class StravaOAuthService {

    private static final Logger log = LoggerFactory.getLogger(StravaOAuthService.class);
    private static final String STRAVA_AUTH_URL = "https://www.strava.com/oauth/authorize";

    private final StravaProperties properties;
    private final StravaApiClient apiClient;
    private final JoueurStravaDataRepository stravaDataRepository;

    public StravaOAuthService(StravaProperties properties,
                              StravaApiClient apiClient,
                              JoueurStravaDataRepository stravaDataRepository) {
        this.properties = properties;
        this.apiClient = apiClient;
        this.stravaDataRepository = stravaDataRepository;
    }

    /**
     * Construit l'URL d'autorisation Strava.
     * Le joueurId est passé dans le paramètre "state" pour le retrouver au callback.
     */
    public String buildAuthorizationUrl(Long joueurId) {
        String encodedRedirect = URLEncoder.encode(properties.getRedirectUri(), StandardCharsets.UTF_8);
        return STRAVA_AUTH_URL
            + "?client_id=" + properties.getClientId()
            + "&redirect_uri=" + encodedRedirect
            + "&response_type=code"
            + "&approval_prompt=auto"
            + "&scope=read,activity:read_all"
            + "&state=" + joueurId;
    }

    /**
     * Traite le callback OAuth : échange le code contre des tokens et les persiste.
     */
    @Transactional
    public JoueurStravaData processCallback(String code, Long joueurId) {
        Map<String, Object> tokenData = apiClient.exchangeCodeForToken(code);

        JoueurStravaData data = stravaDataRepository.findByJoueurId(joueurId)
            .orElse(new JoueurStravaData(joueurId));

        applyTokenData(data, tokenData);
        data.setStravaConnected(true);

        JoueurStravaData saved = stravaDataRepository.save(data);
        log.info("Joueur {} connecté à Strava (athlete {})", joueurId, data.getStravaAthleteId());
        return saved;
    }

    /**
     * Rafraîchit le token si celui-ci est expiré ou sur le point d'expirer.
     * Sauvegarde les nouveaux tokens automatiquement.
     */
    @Transactional
    public void refreshIfNeeded(JoueurStravaData data) {
        long now = Instant.now().getEpochSecond();
        // Refresh si le token expire dans moins de 5 minutes
        if (data.getStravaTokenExpiresAt() == null || data.getStravaTokenExpiresAt() <= now + 300) {
            log.info("Rafraîchissement du token Strava pour le joueur {}", data.getJoueurId());
            Map<String, Object> refreshed = apiClient.refreshToken(data.getStravaRefreshToken());
            applyTokenData(data, refreshed);
            stravaDataRepository.save(data);
        }
    }

    /**
     * Déconnecte un joueur de Strava (supprime les tokens).
     */
    @Transactional
    public void disconnect(Long joueurId) {
        stravaDataRepository.findByJoueurId(joueurId).ifPresent(data -> {
            data.setStravaConnected(false);
            data.setStravaAccessToken(null);
            data.setStravaRefreshToken(null);
            data.setStravaTokenExpiresAt(null);
            stravaDataRepository.save(data);
            log.info("Joueur {} déconnecté de Strava", joueurId);
        });
    }

    private void applyTokenData(JoueurStravaData data, Map<String, Object> tokenData) {
        Object accessToken = tokenData.get("access_token");
        if (accessToken != null) data.setStravaAccessToken(accessToken.toString());

        Object refreshToken = tokenData.get("refresh_token");
        if (refreshToken != null) data.setStravaRefreshToken(refreshToken.toString());

        Object expiresAt = tokenData.get("expires_at");
        if (expiresAt instanceof Number n) data.setStravaTokenExpiresAt(n.longValue());

        // Récupérer l'ID athlete depuis la réponse d'exchange (pas présent dans refresh)
        Object athlete = tokenData.get("athlete");
        if (athlete instanceof Map<?, ?> athleteMap) {
            Object athleteId = athleteMap.get("id");
            if (athleteId instanceof Number n) data.setStravaAthleteId(n.longValue());
        }
    }
}
