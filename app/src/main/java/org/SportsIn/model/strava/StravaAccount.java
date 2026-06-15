package org.SportsIn.model.strava;

import jakarta.persistence.*;

/**
 * Compte Strava lié à un joueur SportsIn.
 * Stocke les tokens OAuth2 Strava.
 * En production, chiffrer access_token et refresh_token avec AES-256.
 */
@Entity
@Table(name = "strava_account")
public class StravaAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "joueur_id", nullable = false, unique = true)
    private Long joueurId;

    @Column(name = "strava_athlete_id", nullable = false, unique = true)
    private String stravaAthleteId;

    @Column(name = "access_token", nullable = false)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    /** Instant ISO-8601 d'expiration du access_token */
    @Column(name = "token_expires_at", nullable = false)
    private String tokenExpiresAt;

    @Column(name = "connected_at", nullable = false)
    private String connectedAt;

    public StravaAccount() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getJoueurId() { return joueurId; }
    public void setJoueurId(Long joueurId) { this.joueurId = joueurId; }

    public String getStravaAthleteId() { return stravaAthleteId; }
    public void setStravaAthleteId(String stravaAthleteId) { this.stravaAthleteId = stravaAthleteId; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(String tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }

    public String getConnectedAt() { return connectedAt; }
    public void setConnectedAt(String connectedAt) { this.connectedAt = connectedAt; }
}
