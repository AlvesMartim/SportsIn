package org.SportsIn.model.strava;

import jakarta.persistence.*;

/**
 * Stocke les tokens OAuth Strava pour un joueur.
 * Table séparée de joueur pour éviter de modifier le schéma existant.
 */
@Entity
@Table(name = "joueur_strava")
public class JoueurStravaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "joueur_id", nullable = false, unique = true)
    private Long joueurId;

    @Column(name = "strava_athlete_id")
    private Long stravaAthleteId;

    @Column(name = "strava_access_token", length = 512)
    private String stravaAccessToken;

    @Column(name = "strava_refresh_token", length = 512)
    private String stravaRefreshToken;

    /** Timestamp Unix (secondes) d'expiration de l'access token */
    @Column(name = "strava_token_expires_at")
    private Long stravaTokenExpiresAt;

    @Column(name = "strava_connected")
    private boolean stravaConnected;

    public JoueurStravaData() {}

    public JoueurStravaData(Long joueurId) {
        this.joueurId = joueurId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getJoueurId() { return joueurId; }
    public void setJoueurId(Long joueurId) { this.joueurId = joueurId; }

    public Long getStravaAthleteId() { return stravaAthleteId; }
    public void setStravaAthleteId(Long stravaAthleteId) { this.stravaAthleteId = stravaAthleteId; }

    public String getStravaAccessToken() { return stravaAccessToken; }
    public void setStravaAccessToken(String stravaAccessToken) { this.stravaAccessToken = stravaAccessToken; }

    public String getStravaRefreshToken() { return stravaRefreshToken; }
    public void setStravaRefreshToken(String stravaRefreshToken) { this.stravaRefreshToken = stravaRefreshToken; }

    public Long getStravaTokenExpiresAt() { return stravaTokenExpiresAt; }
    public void setStravaTokenExpiresAt(Long stravaTokenExpiresAt) { this.stravaTokenExpiresAt = stravaTokenExpiresAt; }

    public boolean isStravaConnected() { return stravaConnected; }
    public void setStravaConnected(boolean stravaConnected) { this.stravaConnected = stravaConnected; }
}
