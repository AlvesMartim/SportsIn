package org.SportsIn.controller;

import org.SportsIn.model.Arene;
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

    private final WeatherClient weatherClient;
    private final WeatherHardshipEngine hardshipEngine;
    private final AreneRepository areneRepository;

    public WeatherController(WeatherClient weatherClient,
                             WeatherHardshipEngine hardshipEngine,
                             AreneRepository areneRepository) {
        this.weatherClient = weatherClient;
        this.hardshipEngine = hardshipEngine;
        this.areneRepository = areneRepository;
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrent(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false) String sport) {

        Optional<WeatherSnapshot> snapshotOpt = weatherClient.getCurrentWeather(lat, lng);
        if (snapshotOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("available", false));
        }

        return ResponseEntity.ok(buildResponse(snapshotOpt.get(), sport));
    }

    @GetMapping("/arena/{id}")
    public ResponseEntity<Map<String, Object>> getForArena(
            @PathVariable String id,
            @RequestParam(required = false) String sport) {

        Optional<Arene> areneOpt = areneRepository.findById(id);
        if (areneOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Arene arene = areneOpt.get();
        Optional<WeatherSnapshot> snapshotOpt = weatherClient.getCurrentWeather(arene.getLatitude(), arene.getLongitude());
        if (snapshotOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("available", false));
        }

        return ResponseEntity.ok(buildResponse(snapshotOpt.get(), sport));
    }

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
}
