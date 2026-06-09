package org.SportsIn.controller;

import org.SportsIn.model.Arene;
import org.SportsIn.model.Session;
import org.SportsIn.model.SessionRepository;
import org.SportsIn.model.SessionState;
import org.SportsIn.repository.AreneRepository;
import org.SportsIn.weather.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class WeatherController {

    private static final List<String> ALL_SPORTS = List.of(
            "FOOTBALL", "BASKET", "TENNIS", "RUNNING", "MUSCULATION"
    );

    private static final Map<String, String> SPORT_LABELS = Map.of(
            "FOOTBALL", "Football",
            "BASKET", "Basketball",
            "TENNIS", "Tennis",
            "RUNNING", "Course à pied",
            "MUSCULATION", "Musculation"
    );

    // Badge definitions: tag → {id, name, icon, description, required wins}
    private static final List<Map<String, Object>> BADGE_DEFS = List.of(
            badge("RAIN_WARRIOR",      "RAIN",    "🌧️", "Guerrier de la pluie",   "Remporter une victoire sous la pluie",             1),
            badge("STORM_MASTER",      "STORM",   "⛈️", "Maître de l'orage",      "Remporter 3 victoires sous orage",                 3),
            badge("ICE_BREAKER",       "COLD",    "🥶", "Briseur de glace",       "Remporter une victoire par grand froid",            1),
            badge("WIND_RIDER",        "WIND",    "💨", "Cavalier du vent",       "Remporter une victoire sous vent fort",             1),
            badge("HEAT_KING",         "HEAT",    "🔥", "Roi de la canicule",     "Remporter une victoire par forte chaleur",          1),
            badge("EXTREME_SURVIVOR",  "EXTREME", "🌪️", "Survivant extrême",      "Remporter une victoire par conditions extrêmes",    1)
    );

    private final WeatherClient weatherClient;
    private final WeatherHardshipEngine hardshipEngine;
    private final AreneRepository areneRepository;
    private final SessionRepository sessionRepository;

    public WeatherController(WeatherClient weatherClient,
                             WeatherHardshipEngine hardshipEngine,
                             AreneRepository areneRepository,
                             SessionRepository sessionRepository) {
        this.weatherClient = weatherClient;
        this.hardshipEngine = hardshipEngine;
        this.areneRepository = areneRepository;
        this.sessionRepository = sessionRepository;
    }

    // ── Météo courante à des coordonnées ──────────────────────────────────────

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrent(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false) String sport) {

        Optional<WeatherSnapshot> snap = weatherClient.getCurrentWeather(lat, lng);
        if (snap.isEmpty()) return ResponseEntity.ok(Map.of("available", false));
        return ResponseEntity.ok(buildResponse(snap.get(), sport));
    }

    // ── Météo courante d'une arène ────────────────────────────────────────────

    @GetMapping("/arena/{id}")
    public ResponseEntity<Map<String, Object>> getForArena(
            @PathVariable String id,
            @RequestParam(required = false) String sport) {

        Optional<Arene> areneOpt = areneRepository.findById(id);
        if (areneOpt.isEmpty()) return ResponseEntity.notFound().build();

        Arene arene = areneOpt.get();
        Optional<WeatherSnapshot> snap = weatherClient.getCurrentWeather(arene.getLatitude(), arene.getLongitude());
        if (snap.isEmpty()) return ResponseEntity.ok(Map.of("available", false));
        return ResponseEntity.ok(buildResponse(snap.get(), sport));
    }

    // ── Feature 2 : Arènes sous alerte météo extrême ─────────────────────────

    @GetMapping("/alerts")
    public ResponseEntity<List<Map<String, Object>>> getAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();

        for (Arene arene : areneRepository.findAll()) {
            Optional<WeatherSnapshot> snap = weatherClient.getCurrentWeather(
                    arene.getLatitude(), arene.getLongitude());
            if (snap.isEmpty()) continue;

            Set<WeatherConditionTag> tags = WeatherClassifier.classify(snap.get());
            if (!WeatherClassifier.isExtreme(tags)) continue;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("arenaId", arene.getId());
            entry.put("arenaName", arene.getNom());
            entry.put("latitude", arene.getLatitude());
            entry.put("longitude", arene.getLongitude());
            entry.put("tags", tags.stream().map(Enum::name).collect(Collectors.toList()));
            entry.put("dominantLabel", WeatherClassifier.dominantLabel(tags));
            entry.put("temperatureC", snap.get().temperatureC());
            entry.put("windSpeedMps", snap.get().windSpeedMps());
            entry.put("influenceMultiplier", 2.0);
            alerts.add(entry);
        }

        return ResponseEntity.ok(alerts);
    }

    // ── Feature 3 : Prévisions 24h d'une arène ───────────────────────────────

    @GetMapping("/arena/{id}/forecast")
    public ResponseEntity<Map<String, Object>> getForecastForArena(@PathVariable String id) {
        Optional<Arene> areneOpt = areneRepository.findById(id);
        if (areneOpt.isEmpty()) return ResponseEntity.notFound().build();

        Arene arene = areneOpt.get();
        List<WeatherForecastEntry> raw = weatherClient.getForecast(arene.getLatitude(), arene.getLongitude(), 24);

        List<Map<String, Object>> windows = raw.stream().map(entry -> {
            Set<WeatherConditionTag> tags = WeatherClassifier.classify(entry);
            boolean extreme = WeatherClassifier.isExtreme(tags);
            WeatherSnapshot snap = new WeatherSnapshot(entry.temperatureC(), entry.windSpeedMps(),
                    entry.precipitationMm(), entry.weatherCode(), entry.weatherMain(), entry.description(), entry.at());
            double hardship = hardshipEngine.computeHardshipIndex("FOOTBALL", snap);

            Map<String, Object> w = new LinkedHashMap<>();
            w.put("at", entry.at().toString());
            w.put("temperatureC", entry.temperatureC());
            w.put("windSpeedMps", entry.windSpeedMps());
            w.put("precipitationMm", entry.precipitationMm());
            w.put("tags", tags.stream().map(Enum::name).collect(Collectors.toList()));
            w.put("isExtreme", extreme);
            w.put("dominantLabel", WeatherClassifier.dominantLabel(tags));
            w.put("influenceMultiplier", extreme ? 2.0 : Math.max(1.0, hardship));
            return w;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("arenaId", arene.getId());
        result.put("arenaName", arene.getNom());
        result.put("windows", windows);
        result.put("hasExtremeEvent", windows.stream().anyMatch(w -> Boolean.TRUE.equals(w.get("isExtreme"))));
        return ResponseEntity.ok(result);
    }

    // ── Feature 8 : Meilleur sport selon la météo actuelle ───────────────────

    @GetMapping("/arena/{id}/best-sport")
    public ResponseEntity<Map<String, Object>> getBestSport(@PathVariable String id) {
        Optional<Arene> areneOpt = areneRepository.findById(id);
        if (areneOpt.isEmpty()) return ResponseEntity.notFound().build();

        Arene arene = areneOpt.get();
        Optional<WeatherSnapshot> snap = weatherClient.getCurrentWeather(arene.getLatitude(), arene.getLongitude());
        if (snap.isEmpty()) return ResponseEntity.ok(Map.of("available", false));

        WeatherSnapshot snapshot = snap.get();
        List<Map<String, Object>> ranked = ALL_SPORTS.stream()
                .map(sport -> {
                    double hardship = hardshipEngine.computeHardshipIndex(sport, snapshot);
                    double bonus = Math.max(0.0, hardship - 1.0);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("sport", sport);
                    m.put("label", SPORT_LABELS.getOrDefault(sport, sport));
                    m.put("hardshipIndex", hardship);
                    m.put("influenceBonus", bonus);
                    m.put("bonusPct", (int) Math.round(bonus * 100));
                    return m;
                })
                .sorted(Comparator.comparingDouble(m -> -((Double) m.get("influenceBonus"))))
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("available", true);
        result.put("arenaId", arene.getId());
        result.put("sports", ranked);
        result.put("recommended", ranked.isEmpty() ? null : ranked.get(0).get("sport"));
        result.put("recommendedLabel", ranked.isEmpty() ? null : ranked.get(0).get("label"));
        result.put("recommendedBonusPct", ranked.isEmpty() ? 0 : ranked.get(0).get("bonusPct"));
        return ResponseEntity.ok(result);
    }

    // ── Feature 1 : Badges météo d'une équipe ────────────────────────────────

    @GetMapping("/teams/{teamId}/badges")
    public ResponseEntity<List<Map<String, Object>>> getTeamBadges(@PathVariable String teamId) {
        List<Session> won = sessionRepository.findByState(SessionState.TERMINATED).stream()
                .filter(s -> teamId.equals(s.getWinnerParticipantId()))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> def : BADGE_DEFS) {
            String tag = (String) def.get("tag");
            int required = (int) def.get("required");

            long count = won.stream()
                    .filter(s -> s.getResult() != null
                            && s.getResult().getWeatherTags() != null
                            && Arrays.asList(s.getResult().getWeatherTags().split(","))
                                     .stream().map(String::trim)
                                     .anyMatch(t -> t.equalsIgnoreCase(tag)))
                    .count();

            Map<String, Object> badge = new LinkedHashMap<>();
            badge.put("id", def.get("id"));
            badge.put("name", def.get("name"));
            badge.put("icon", def.get("icon"));
            badge.put("description", def.get("description"));
            badge.put("required", required);
            badge.put("progress", (int) Math.min(count, required));
            badge.put("unlocked", count >= required);
            result.add(badge);
        }

        return ResponseEntity.ok(result);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> buildResponse(WeatherSnapshot snapshot, String sport) {
        Set<WeatherConditionTag> tags = WeatherClassifier.classify(snapshot);
        double hardshipIndex = sport != null && !sport.isBlank()
                ? hardshipEngine.computeHardshipIndex(sport, snapshot)
                : 1.0;
        double influenceBonus = Math.max(0.0, hardshipIndex - 1.0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("available", true);
        result.put("temperatureC", snapshot.temperatureC());
        result.put("windSpeedMps", snapshot.windSpeedMps());
        result.put("precipitationMm", snapshot.precipitationMm());
        result.put("weatherMain", snapshot.weatherMain());
        result.put("description", snapshot.description());
        result.put("tags", tags.stream().map(Enum::name).collect(Collectors.toList()));
        result.put("hardshipIndex", hardshipIndex);
        result.put("influenceBonus", influenceBonus);
        result.put("isExtreme", WeatherClassifier.isExtreme(tags));
        result.put("dominantLabel", WeatherClassifier.dominantLabel(tags));
        return result;
    }

    private static Map<String, Object> badge(String id, String tag, String icon,
                                              String name, String description, int required) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("tag", tag);
        m.put("icon", icon);
        m.put("name", name);
        m.put("description", description);
        m.put("required", required);
        return m;
    }
}
