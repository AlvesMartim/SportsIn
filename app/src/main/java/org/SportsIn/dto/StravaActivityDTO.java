package org.SportsIn.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Activité Strava telle que retournée par l'API Strava v3. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StravaActivityDTO {

    @JsonProperty("id")
    private long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("sport_type")
    private String sportType;

    /** Fallback pour l'ancien champ "type" (déprécié par Strava). */
    @JsonProperty("type")
    private String type;

    @JsonProperty("distance")
    private double distance; // mètres

    @JsonProperty("moving_time")
    private int movingTime; // secondes

    @JsonProperty("elapsed_time")
    private int elapsedTime; // secondes

    @JsonProperty("total_elevation_gain")
    private double totalElevationGain;

    @JsonProperty("average_speed")
    private double averageSpeed; // m/s

    @JsonProperty("max_speed")
    private double maxSpeed; // m/s

    @JsonProperty("average_heartrate")
    private Double averageHeartrate;

    @JsonProperty("max_heartrate")
    private Double maxHeartrate;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("map")
    private MapDTO map;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MapDTO {
        @JsonProperty("summary_polyline")
        private String summaryPolyline;

        public String getSummaryPolyline() { return summaryPolyline; }
        public void setSummaryPolyline(String summaryPolyline) { this.summaryPolyline = summaryPolyline; }
    }

    public String getEffectiveSportType() {
        return (sportType != null && !sportType.isBlank()) ? sportType : type;
    }

    public String getSummaryPolyline() {
        return map != null ? map.getSummaryPolyline() : null;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSportType() { return sportType; }
    public void setSportType(String sportType) { this.sportType = sportType; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public int getMovingTime() { return movingTime; }
    public void setMovingTime(int movingTime) { this.movingTime = movingTime; }

    public int getElapsedTime() { return elapsedTime; }
    public void setElapsedTime(int elapsedTime) { this.elapsedTime = elapsedTime; }

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

    public MapDTO getMap() { return map; }
    public void setMap(MapDTO map) { this.map = map; }
}
