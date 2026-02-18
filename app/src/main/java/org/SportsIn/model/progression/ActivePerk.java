package org.SportsIn.model.progression;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "active_perk")
public class ActivePerk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "perk_definition_id", nullable = false)
    private Long perkDefinitionId;

    @Column(name = "target_id")
    private String targetId;

    @Column(name = "activated_at", nullable = false)
    private String activatedAt;

    @Column(name = "expires_at", nullable = false)
    private String expiresAt;

    @Column(name = "last_used_at")
    private String lastUsedAt;

    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    public ActivePerk() {
    }

    // --- Lifecycle methods ---

    public boolean isExpired() {
        return Instant.now().isAfter(Instant.parse(expiresAt));
    }

    public boolean isActive() {
        Instant now = Instant.now();
        return now.isAfter(Instant.parse(activatedAt)) && now.isBefore(Instant.parse(expiresAt));
    }

    public Duration getRemainingDuration() {
        Instant expires = Instant.parse(expiresAt);
        Instant now = Instant.now();
        if (now.isAfter(expires)) return Duration.ZERO;
        return Duration.between(now, expires);
    }

    public Instant getExpiresAtInstant() {
        return expiresAt != null ? Instant.parse(expiresAt) : null;
    }

    public Instant getActivatedAtInstant() {
        return activatedAt != null ? Instant.parse(activatedAt) : null;
    }

    // --- Getters & Setters ---

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

    public Long getPerkDefinitionId() {
        return perkDefinitionId;
    }

    public void setPerkDefinitionId(Long perkDefinitionId) {
        this.perkDefinitionId = perkDefinitionId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(String activatedAt) {
        this.activatedAt = activatedAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(String lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }
}
