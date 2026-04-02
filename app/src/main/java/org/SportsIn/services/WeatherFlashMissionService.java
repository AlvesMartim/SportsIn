package org.SportsIn.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.SportsIn.model.Arene;
import org.SportsIn.model.mission.Mission;
import org.SportsIn.model.mission.MissionPriority;
import org.SportsIn.model.mission.MissionStatus;
import org.SportsIn.model.mission.MissionType;
import org.SportsIn.model.user.Equipe;
import org.SportsIn.repository.AreneRepository;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.repository.MissionRepository;
import org.SportsIn.weather.WeatherClassifier;
import org.SportsIn.weather.WeatherClient;
import org.SportsIn.weather.WeatherConditionTag;
import org.SportsIn.weather.WeatherForecastEntry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WeatherFlashMissionService {

    private static final int MAX_ACTIVE_MISSIONS_PER_TEAM = 3;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final MissionRepository missionRepository;
    private final EquipeRepository equipeRepository;
    private final AreneRepository areneRepository;
    private final WeatherClient weatherClient;

    public WeatherFlashMissionService(MissionRepository missionRepository,
                                      EquipeRepository equipeRepository,
                                      AreneRepository areneRepository,
                                      WeatherClient weatherClient) {
        this.missionRepository = missionRepository;
        this.equipeRepository = equipeRepository;
        this.areneRepository = areneRepository;
        this.weatherClient = weatherClient;
    }

    public int generateFlashMissionsForAllTeams() {
        List<WeatherFlashEvent> events = detectUpcomingEvents();
        if (events.isEmpty()) {
            return 0;
        }

        List<Equipe> teams = equipeRepository.findAll();
        int created = 0;

        for (Equipe team : teams) {
            long activeCount = missionRepository.countActiveByTeam(team.getId());
            if (activeCount >= MAX_ACTIVE_MISSIONS_PER_TEAM) {
                continue;
            }

            int remainingSlots = (int) (MAX_ACTIVE_MISSIONS_PER_TEAM - activeCount);
            Set<String> existingKeys = missionRepository.findActiveByTeam(team.getId()).stream()
                    .map(Mission::payloadKey)
                    .collect(Collectors.toCollection(HashSet::new));

            for (WeatherFlashEvent event : events) {
                if (remainingSlots <= 0) {
                    break;
                }

                Mission mission = buildMission(team.getId(), event);
                if (existingKeys.contains(mission.payloadKey())) {
                    continue;
                }

                missionRepository.save(mission);
                existingKeys.add(mission.payloadKey());
                remainingSlots--;
                created++;
            }
        }

        return created;
    }

    private List<WeatherFlashEvent> detectUpcomingEvents() {
        List<WeatherFlashEvent> events = new ArrayList<>();
        Instant now = Instant.now();

        for (Arene arena : areneRepository.findAll()) {
            List<WeatherForecastEntry> forecast = weatherClient.getForecast(arena.getLatitude(), arena.getLongitude(), 24);
            if (forecast.isEmpty()) {
                continue;
            }

            for (WeatherForecastEntry entry : forecast) {
                Set<WeatherConditionTag> tags = WeatherClassifier.classify(entry);
                if (!tags.contains(WeatherConditionTag.EXTREME)) {
                    continue;
                }

                Instant alertDeadline = entry.at().minus(1, ChronoUnit.HOURS);
                if (alertDeadline.isBefore(now.plus(1, ChronoUnit.HOURS))) {
                    continue;
                }

                events.add(new WeatherFlashEvent(
                        arena.getId(),
                        arena.getNom(),
                        entry.at(),
                        WeatherClassifier.dominantLabel(tags)
                ));
                break;
            }
        }

        events.sort(Comparator.comparing(WeatherFlashEvent::eventStartsAt));
        return events.stream().limit(3).toList();
    }

    private Mission buildMission(Long teamId, WeatherFlashEvent event) {
        Instant now = Instant.now();

        Map<String, Object> payload = Map.of(
                "missionCategory", "WEATHER_FLASH",
                "arenaId", event.arenaId(),
                "arenaName", event.arenaName(),
                "eventType", event.eventType(),
                "eventStartsAt", event.eventStartsAt().toString()
        );

        Mission mission = new Mission();
        mission.setTeamId(teamId);
        // Kept as DIVERSITY_SPORT to remain compatible with existing DB constraints.
        mission.setType(MissionType.DIVERSITY_SPORT);
        mission.setStatus(MissionStatus.ACTIVE);
        mission.setTitle("Alerte " + event.eventType() + " a " + event.arenaName());
        mission.setDescription("Prenez l'avantage sur l'arene '" + event.arenaName()
                + "' avant l'arrivee de " + event.eventType().toLowerCase()
                + ". Les captures realisees avant l'evenement rapportent plus !");
        mission.setPriority(MissionPriority.HIGH);
        mission.setRewardTeamPoints(60);
        mission.setRewardTeamXp(45);
        mission.setTimestampsFromInstant(now, now, event.eventStartsAt());
        mission.setPayloadJson(toJson(payload));
        mission.setProgressCurrent(0);
        mission.setProgressTarget(1);
        return mission;
    }

    private String toJson(Map<String, Object> map) {
        try {
            return MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }

    private record WeatherFlashEvent(String arenaId,
                                     String arenaName,
                                     Instant eventStartsAt,
                                     String eventType) {
    }
}
