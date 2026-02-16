package org.SportsIn.model.mission;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "mission")
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionStatus status = MissionStatus.ACTIVE;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionPriority priority = MissionPriority.MEDIUM;

    @Column(name = "reward_team_points", nullable = false)
    private int rewardTeamPoints;

    @Column(name = "reward_team_xp", nullable = false)
    private int rewardTeamXp;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "starts_at", nullable = false)
    private String startsAt;

    @Column(name = "ends_at", nullable = false)
    private String endsAt;

    @Column(name = "completed_at")
    private String completedAt;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "progress_current", nullable = false)
    private int progressCurrent;

    @Column(name = "progress_target", nullable = false)
    private int progressTarget = 1;

    @Column(name = "last_evaluated_at")
    private String lastEvaluatedAt;

    public Mission() {
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public MissionType getType() {
        return type;
    }

    public void setType(MissionType type) {
        this.type = type;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MissionPriority getPriority() {
        return priority;
    }

    public void setPriority(MissionPriority priority) {
        this.priority = priority;
    }

    public int getRewardTeamPoints() {
        return rewardTeamPoints;
    }

    public void setRewardTeamPoints(int rewardTeamPoints) {
        this.rewardTeamPoints = rewardTeamPoints;
    }

    public int getRewardTeamXp() {
        return rewardTeamXp;
    }

    public void setRewardTeamXp(int rewardTeamXp) {
        this.rewardTeamXp = rewardTeamXp;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(String startsAt) {
        this.startsAt = startsAt;
    }

    public String getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(String endsAt) {
        this.endsAt = endsAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public int getProgressCurrent() {
        return progressCurrent;
    }

    public void setProgressCurrent(int progressCurrent) {
        this.progressCurrent = progressCurrent;
    }

    public int getProgressTarget() {
        return progressTarget;
    }

    public void setProgressTarget(int progressTarget) {
        this.progressTarget = progressTarget;
    }

    public String getLastEvaluatedAt() {
        return lastEvaluatedAt;
    }

    public void setLastEvaluatedAt(String lastEvaluatedAt) {
        this.lastEvaluatedAt = lastEvaluatedAt;
    }

    // --- Convenience methods using Instant ---

    public Instant getEndsAtInstant() {
        return endsAt != null ? Instant.parse(endsAt) : null;
    }

    public Instant getStartsAtInstant() {
        return startsAt != null ? Instant.parse(startsAt) : null;
    }

    public Instant getCreatedAtInstant() {
        return createdAt != null ? Instant.parse(createdAt) : null;
    }

    public void setTimestampsFromInstant(Instant created, Instant starts, Instant ends) {
        this.createdAt = created.toString();
        this.startsAt = starts.toString();
        this.endsAt = ends.toString();
    }

    public boolean isActive() {
        return status == MissionStatus.ACTIVE;
    }

    public boolean isExpired() {
        return isActive() && Instant.now().isAfter(getEndsAtInstant());
    }

    public String payloadKey() {
        return type.name() + ":" + (payloadJson != null ? payloadJson : "");
    }
}
