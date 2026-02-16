package org.SportsIn.dto;

import org.SportsIn.model.mission.Mission;
import org.SportsIn.model.mission.MissionPriority;
import org.SportsIn.model.mission.MissionStatus;
import org.SportsIn.model.mission.MissionType;

public class MissionSummaryDTO {

    private Long id;
    private MissionType type;
    private MissionStatus status;
    private String title;
    private MissionPriority priority;
    private String endsAt;
    private int rewardTeamPoints;
    private int progressCurrent;
    private int progressTarget;

    public MissionSummaryDTO() {
    }

    public static MissionSummaryDTO from(Mission m) {
        MissionSummaryDTO dto = new MissionSummaryDTO();
        dto.id = m.getId();
        dto.type = m.getType();
        dto.status = m.getStatus();
        dto.title = m.getTitle();
        dto.priority = m.getPriority();
        dto.endsAt = m.getEndsAt();
        dto.rewardTeamPoints = m.getRewardTeamPoints();
        dto.progressCurrent = m.getProgressCurrent();
        dto.progressTarget = m.getProgressTarget();
        return dto;
    }

    // --- Getters ---

    public Long getId() { return id; }
    public MissionType getType() { return type; }
    public MissionStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public MissionPriority getPriority() { return priority; }
    public String getEndsAt() { return endsAt; }
    public int getRewardTeamPoints() { return rewardTeamPoints; }
    public int getProgressCurrent() { return progressCurrent; }
    public int getProgressTarget() { return progressTarget; }
}
