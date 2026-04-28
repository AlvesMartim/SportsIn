package org.SportsIn.model.strava;

import jakarta.persistence.*;

/**
 * Représente une activité sportive importée depuis l'API Strava.
 */
@Entity
@Table(name = "strava_activity",
    uniqueConstraints = @UniqueConstraint(columnNames = {"joueur_id", "strava_activity_id"}))
public class StravaActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "joueur_id", nullable = false)
    private Long joueurId;

    /** ID Strava de l'activité (unique par joueur) */
    @Column(name = "strava_activity_id", nullable = false)
    private Long stravaActivityId;

    /** Type Strava brut : "Run", "Ride", "Walk", "Soccer", etc. */
    @Column(name = "strava_type")
    private String stravaType;

    /** Code sport SportsIn mappé : "RUNNING", "CYCLING", "FOOTBALL", etc. */
    @Column(name = "sport_code")
    private String sportCode;

    private String name;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "distance_meters")
    private Double distanceMeters;

    @Column(name = "moving_time_seconds")
    private Long movingTimeSeconds;

    @Column(name = "elevation_gain")
    private Double elevationGain;

    @Column(name = "polyline_encoded", length = 8192)
    private String polylineEncoded;

    /** true si cette activité a déjà été comptabilisée comme session SportsIn */
    @Column(name = "imported_as_session")
    private boolean importedAsSession;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    public StravaActivity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getJoueurId() { return joueurId; }
    public void setJoueurId(Long joueurId) { this.joueurId = joueurId; }

    public Long getStravaActivityId() { return stravaActivityId; }
    public void setStravaActivityId(Long stravaActivityId) { this.stravaActivityId = stravaActivityId; }

    public String getStravaType() { return stravaType; }
    public void setStravaType(String stravaType) { this.stravaType = stravaType; }

    public String getSportCode() { return sportCode; }
    public void setSportCode(String sportCode) { this.sportCode = sportCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public Double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }

    public Long getMovingTimeSeconds() { return movingTimeSeconds; }
    public void setMovingTimeSeconds(Long movingTimeSeconds) { this.movingTimeSeconds = movingTimeSeconds; }

    public Double getElevationGain() { return elevationGain; }
    public void setElevationGain(Double elevationGain) { this.elevationGain = elevationGain; }

    public String getPolylineEncoded() { return polylineEncoded; }
    public void setPolylineEncoded(String polylineEncoded) { this.polylineEncoded = polylineEncoded; }

    public boolean isImportedAsSession() { return importedAsSession; }
    public void setImportedAsSession(boolean importedAsSession) { this.importedAsSession = importedAsSession; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
