package org.SportsIn.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.SportsIn.model.mission.Mission;
import org.SportsIn.model.mission.MissionPriority;
import org.SportsIn.model.mission.MissionStatus;
import org.SportsIn.model.mission.MissionType;

import java.util.Map;

public class MissionDetailDTO {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Long id;
    private Long teamId;
    private MissionType type;
    private MissionStatus status;
    private String title;
    private String description;
    private MissionPriority priority;
    private int rewardTeamPoints;
    private int rewardTeamXp;
    private String createdAt;
    private String startsAt;
    private String endsAt;
    private String completedAt;
    private Map<String, Object> payload;
    private int progressCurrent;
    private int progressTarget;

    public MissionDetailDTO() {
    }

    public static MissionDetailDTO from(Mission m) {
        MissionDetailDTO dto = new MissionDetailDTO();
        dto.id = m.getId();
        dto.teamId = m.getTeamId();
        dto.type = m.getType();
        dto.status = m.getStatus();
        dto.title = m.getTitle();
        dto.description = m.getDescription();
        dto.priority = m.getPriority();
        dto.rewardTeamPoints = m.getRewardTeamPoints();
        dto.rewardTeamXp = m.getRewardTeamXp();
        dto.createdAt = m.getCreatedAt();
        dto.startsAt = m.getStartsAt();
        dto.endsAt = m.getEndsAt();
        dto.completedAt = m.getCompletedAt();
        dto.progressCurrent = m.getProgressCurrent();
        dto.progressTarget = m.getProgressTarget();
        dto.payload = parsePayload(m.getPayloadJson());
        return dto;
    }

    private static Map<String, Object> parsePayload(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of("raw", json);
        }
    }

    // --- Getters ---

    public Long getId() { return id; }
    public Long getTeamId() { return teamId; }
    public MissionType getType() { return type; }
    public MissionStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public MissionPriority getPriority() { return priority; }
    public int getRewardTeamPoints() { return rewardTeamPoints; }
    public int getRewardTeamXp() { return rewardTeamXp; }
    public String getCreatedAt() { return createdAt; }
    public String getStartsAt() { return startsAt; }
    public String getEndsAt() { return endsAt; }
    public String getCompletedAt() { return completedAt; }
    public Map<String, Object> getPayload() { return payload; }
    public int getProgressCurrent() { return progressCurrent; }
    public int getProgressTarget() { return progressTarget; }
}
