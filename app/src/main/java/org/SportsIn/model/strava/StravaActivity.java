package org.SportsIn.model.strava;

import jakarta.persistence.*;

/**
 * Activité Strava importée et persistée.
 * Le champ stravaActivityId sert de clé de déduplication.
 */
@Entity
@Table(name = "strava_activity")
public class StravaActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID unique Strava — clé de dédup, ne pas importer deux fois */
    @Column(name = "strava_activity_id", nullable = false, unique = true)
    private String stravaActivityId;

    @Column(name = "joueur_id", nullable = false)
    private Long joueurId;

    @Column(name = "equipe_id")
    private Long equipeId;

    @Column(name = "name")
    private String name;

    /** Type Strava : Run, Ride, Swim, WeightTraining, etc. */
    @Column(name = "sport_type", nullable = false)
    private String sportType;

    @Column(name = "distance_meters")
    private double distanceMeters;

    @Column(name = "moving_time_seconds")
    private int movingTimeSeconds;

    @Column(name = "elapsed_time_seconds")
    private int elapsedTimeSeconds;

    @Column(name = "total_elevation_gain")
    private double totalElevationGain;

    /** Vitesse en m/s */
    @Column(name = "average_speed")
    private double averageSpeed;

    @Column(name = "max_speed")
    private double maxSpeed;

    @Column(name = "average_heartrate")
    private Double averageHeartrate;

    @Column(name = "max_heartrate")
    private Double maxHeartrate;

    @Column(name = "start_date", nullable = false)
    private String startDate;

    /** Polyline encodée Google (summary) */
    @Column(name = "summary_polyline", columnDefinition = "TEXT")
    private String summaryPolyline;

    /** Influence totale accordée à l'équipe via cette activité */
    @Column(name = "influence_granted")
    private double influenceGranted;

    /** JSON array de IDs d'arènes traversées */
    @Column(name = "zones_traversed")
    private String zonesTraversed;

    @Column(name = "route_bonus_triggered")
    private boolean routeBonusTriggered;

    /** True si l'activité a été flaggée anti-cheat (vitesse irréaliste) */
    @Column(name = "anti_cheat_flagged")
    private boolean antiCheatFlagged;

    @Column(name = "anti_cheat_reason")
    private String antiCheatReason;

    @Column(name = "synced_at", nullable = false)
    private String syncedAt;

    public StravaActivity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStravaActivityId() { return stravaActivityId; }
    public void setStravaActivityId(String stravaActivityId) { this.stravaActivityId = stravaActivityId; }

    public Long getJoueurId() { return joueurId; }
    public void setJoueurId(Long joueurId) { this.joueurId = joueurId; }

    public Long getEquipeId() { return equipeId; }
    public void setEquipeId(Long equipeId) { this.equipeId = equipeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSportType() { return sportType; }
    public void setSportType(String sportType) { this.sportType = sportType; }

    public double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(double distanceMeters) { this.distanceMeters = distanceMeters; }

    public int getMovingTimeSeconds() { return movingTimeSeconds; }
    public void setMovingTimeSeconds(int movingTimeSeconds) { this.movingTimeSeconds = movingTimeSeconds; }

    public int getElapsedTimeSeconds() { return elapsedTimeSeconds; }
    public void setElapsedTimeSeconds(int elapsedTimeSeconds) { this.elapsedTimeSeconds = elapsedTimeSeconds; }

    public double getTotalElevationGain() { return totalElevationGain; }
    public void setTotalElevationGain(double totalElevationGain) { this.totalElevationGain = totalElevationGain; }

    public double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(double averageSpeed) { this.averageSpeed = averageSpeed; }

    public double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(double maxSpeed) { this.maxSpeed = maxSpeed; }

    public Double getAverageHeartrate() { return averageHeartrate; }
    public void setAverageHeartrate(Double averageHeartrate) { this.averageHeartrate = averageHeartrate; }

    public Double getMaxHeartrate() { return maxHeartrate; }
    public void setMaxHeartrate(Double maxHeartrate) { this.maxHeartrate = maxHeartrate; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getSummaryPolyline() { return summaryPolyline; }
    public void setSummaryPolyline(String summaryPolyline) { this.summaryPolyline = summaryPolyline; }

    public double getInfluenceGranted() { return influenceGranted; }
    public void setInfluenceGranted(double influenceGranted) { this.influenceGranted = influenceGranted; }

    public String getZonesTraversed() { return zonesTraversed; }
    public void setZonesTraversed(String zonesTraversed) { this.zonesTraversed = zonesTraversed; }

    public boolean isRouteBonusTriggered() { return routeBonusTriggered; }
    public void setRouteBonusTriggered(boolean routeBonusTriggered) { this.routeBonusTriggered = routeBonusTriggered; }

    public boolean isAntiCheatFlagged() { return antiCheatFlagged; }
    public void setAntiCheatFlagged(boolean antiCheatFlagged) { this.antiCheatFlagged = antiCheatFlagged; }

    public String getAntiCheatReason() { return antiCheatReason; }
    public void setAntiCheatReason(String antiCheatReason) { this.antiCheatReason = antiCheatReason; }

    public String getSyncedAt() { return syncedAt; }
    public void setSyncedAt(String syncedAt) { this.syncedAt = syncedAt; }
}
