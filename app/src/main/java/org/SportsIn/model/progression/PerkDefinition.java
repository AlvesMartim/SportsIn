package org.SportsIn.model.progression;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.util.Map;

@Entity
@Table(name = "perk_definition")
public class PerkDefinition {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "effect_type", nullable = false)
    private String effectType;

    @Column(name = "required_level", nullable = false)
    private int requiredLevel;

    @Column(name = "duration_seconds", nullable = false)
    private long durationSeconds;

    @Column(name = "cooldown_seconds", nullable = false)
    private long cooldownSeconds;

    @Column(name = "max_active_instances", nullable = false)
    private int maxActiveInstances;

    @Column(nullable = false)
    private boolean stackable;

    @Column(name = "parameters_json", columnDefinition = "TEXT")
    private String parametersJson;

    public PerkDefinition() {
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEffectType() {
        return effectType;
    }

    public void setEffectType(String effectType) {
        this.effectType = effectType;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public long getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(long cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public int getMaxActiveInstances() {
        return maxActiveInstances;
    }

    public void setMaxActiveInstances(int maxActiveInstances) {
        this.maxActiveInstances = maxActiveInstances;
    }

    public boolean isStackable() {
        return stackable;
    }

    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }

    public String getParametersJson() {
        return parametersJson;
    }

    public void setParametersJson(String parametersJson) {
        this.parametersJson = parametersJson;
    }

    public Map<String, Object> getParametersAsMap() {
        if (parametersJson == null || parametersJson.isBlank()) return Map.of();
        try {
            return MAPPER.readValue(parametersJson, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
