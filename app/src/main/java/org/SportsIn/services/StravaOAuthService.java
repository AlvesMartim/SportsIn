package org.SportsIn.services;

import org.SportsIn.config.StravaProperties;
import org.SportsIn.dto.StravaTokenResponse;
import org.SportsIn.model.strava.StravaAccount;
import org.SportsIn.repository.StravaAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Gère le cycle de vie OAuth2 avec Strava :
 * génération de l'URL d'autorisation, échange code → tokens, déconnexion.
 */
@Service
public class StravaOAuthService {

    private final StravaProperties props;
    private final StravaApiClient stravaApiClient;
    private final StravaAccountRepository accountRepo;

    public StravaOAuthService(StravaProperties props,
                              StravaApiClient stravaApiClient,
                              StravaAccountRepository accountRepo) {
        this.props = props;
        this.stravaApiClient = stravaApiClient;
        this.accountRepo = accountRepo;
    }

    /**
     * Construit l'URL d'autorisation Strava (étape 1 du flow OAuth2).
     * @param joueurId utilisé comme "state" pour retrouver le joueur au callback
     */
    public String buildAuthorizationUrl(Long joueurId) {
        return props.getAuthUrl()
                + "?client_id=" + props.getClientId()
                + "&response_type=code"
                + "&redirect_uri=" + props.getRedirectUri()
                + "&approval_prompt=auto"
                + "&scope=read,activity:read_all"
                + "&state=" + joueurId;
    }

    /**
     * Traite le callback OAuth2 : échange le code contre des tokens et persiste le compte Strava.
     * @param code code reçu de Strava
     * @param joueurId extrait du paramètre "state"
     * @return le compte Strava créé ou mis à jour
     */
    @Transactional
    public StravaAccount handleCallback(String code, Long joueurId) {
        StravaTokenResponse tokenResponse = stravaApiClient.exchangeCodeForTokens(code);

        String stravaAthleteId = String.valueOf(tokenResponse.getAthlete().getId());
        String expiresAt = Instant.ofEpochSecond(tokenResponse.getExpiresAt()).toString();

        // Upsert : on met à jour si le compte existe déjà
        StravaAccount account = accountRepo.findByJoueurId(joueurId)
                .orElseGet(StravaAccount::new);

        account.setJoueurId(joueurId);
        account.setStravaAthleteId(stravaAthleteId);
        account.setAccessToken(tokenResponse.getAccessToken());
        account.setRefreshToken(tokenResponse.getRefreshToken());
        account.setTokenExpiresAt(expiresAt);
        account.setConnectedAt(Instant.now().toString());

        return accountRepo.save(account);
    }

    /**
     * Déconnecte le compte Strava d'un joueur (conformité RGPD).
     * Supprime les tokens de la base ; les activités importées restent (données sportives légitimes).
     */
    @Transactional
    public void disconnect(Long joueurId) {
        stravaApiClient.disconnect(joueurId);
    }

    /** Retourne true si le joueur a un compte Strava lié. */
    public boolean isConnected(Long joueurId) {
        return accountRepo.existsByJoueurId(joueurId);
    }

    /** Retourne le compte Strava d'un joueur, ou empty si non connecté. */
    public Optional<StravaAccount> getAccount(Long joueurId) {
        return accountRepo.findByJoueurId(joueurId);
    }
}
