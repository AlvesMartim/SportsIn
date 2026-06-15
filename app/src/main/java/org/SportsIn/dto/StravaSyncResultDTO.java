package org.SportsIn.dto;

import java.util.List;

/** Résultat d'une synchronisation Strava (manuelle ou webhook). */
public class StravaSyncResultDTO {

    private int activitiesFetched;
    private int activitiesImported;
    private int activitiesSkipped;    // déjà existantes
    private int activitiesFlagged;    // anti-cheat
    private double totalInfluenceGranted;
    private List<String> importedActivityIds;
    private List<String> flaggedReasons;

    public StravaSyncResultDTO() {}

    public int getActivitiesFetched() { return activitiesFetched; }
    public void setActivitiesFetched(int activitiesFetched) { this.activitiesFetched = activitiesFetched; }

    public int getActivitiesImported() { return activitiesImported; }
    public void setActivitiesImported(int activitiesImported) { this.activitiesImported = activitiesImported; }

    public int getActivitiesSkipped() { return activitiesSkipped; }
    public void setActivitiesSkipped(int activitiesSkipped) { this.activitiesSkipped = activitiesSkipped; }

    public int getActivitiesFlagged() { return activitiesFlagged; }
    public void setActivitiesFlagged(int activitiesFlagged) { this.activitiesFlagged = activitiesFlagged; }

    public double getTotalInfluenceGranted() { return totalInfluenceGranted; }
    public void setTotalInfluenceGranted(double totalInfluenceGranted) { this.totalInfluenceGranted = totalInfluenceGranted; }

    public List<String> getImportedActivityIds() { return importedActivityIds; }
    public void setImportedActivityIds(List<String> importedActivityIds) { this.importedActivityIds = importedActivityIds; }

    public List<String> getFlaggedReasons() { return flaggedReasons; }
    public void setFlaggedReasons(List<String> flaggedReasons) { this.flaggedReasons = flaggedReasons; }
}
