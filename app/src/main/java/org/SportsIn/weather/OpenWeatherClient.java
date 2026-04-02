package org.SportsIn.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class OpenWeatherClient implements WeatherClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenWeatherClient.class);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String apiKey;
    private final boolean enabled;
    private final Duration timeout;

    public OpenWeatherClient(ObjectMapper objectMapper,
                             @Value("${weather.openweather.base-url:https://api.openweathermap.org}") String baseUrl,
                             @Value("${weather.openweather.api-key:}") String apiKey,
                             @Value("${weather.openweather.enabled:true}") boolean enabled,
                             @Value("${weather.openweather.timeout-ms:3500}") long timeoutMs) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.enabled = enabled;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(this.timeout)
                .build();
    }

    @Override
    public Optional<WeatherSnapshot> getCurrentWeather(double latitude, double longitude) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("lat", Double.toString(latitude));
        params.put("lon", Double.toString(longitude));
        params.put("units", "metric");

        Optional<JsonNode> payload = getJson("/data/2.5/weather", params);
        if (payload.isEmpty()) {
            return Optional.empty();
        }

        JsonNode root = payload.get();
        JsonNode main = root.path("main");
        JsonNode wind = root.path("wind");
        JsonNode weather = root.path("weather").isArray() && root.path("weather").size() > 0
                ? root.path("weather").get(0)
                : objectMapper.createObjectNode();

        Instant at = Instant.ofEpochSecond(root.path("dt").asLong(Instant.now().getEpochSecond()));
        double precipitation = readPrecipitation(root.path("rain"), "1h") + readPrecipitation(root.path("snow"), "1h");

        WeatherSnapshot snapshot = new WeatherSnapshot(
                main.path("temp").asDouble(0.0),
                wind.path("speed").asDouble(0.0),
                precipitation,
                weather.path("id").asInt(800),
                weather.path("main").asText("Unknown"),
                weather.path("description").asText(""),
                at
        );

        return Optional.of(snapshot);
    }

    @Override
    public List<WeatherForecastEntry> getForecast(double latitude, double longitude, int nextHours) {
        if (!isConfigured()) {
            return List.of();
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("lat", Double.toString(latitude));
        params.put("lon", Double.toString(longitude));
        params.put("units", "metric");

        Optional<JsonNode> payload = getJson("/data/2.5/forecast", params);
        if (payload.isEmpty()) {
            return List.of();
        }

        List<WeatherForecastEntry> entries = new ArrayList<>();
        Instant cutoff = Instant.now().plus(nextHours, ChronoUnit.HOURS);

        JsonNode list = payload.get().path("list");
        if (!list.isArray()) {
            return List.of();
        }

        for (JsonNode item : list) {
            JsonNode main = item.path("main");
            JsonNode wind = item.path("wind");
            JsonNode weather = item.path("weather").isArray() && item.path("weather").size() > 0
                    ? item.path("weather").get(0)
                    : objectMapper.createObjectNode();

            Instant at = Instant.ofEpochSecond(item.path("dt").asLong(Instant.now().getEpochSecond()));
            if (nextHours > 0 && at.isAfter(cutoff)) {
                continue;
            }

            double precipitation = readPrecipitation(item.path("rain"), "3h")
                    + readPrecipitation(item.path("snow"), "3h");

            entries.add(new WeatherForecastEntry(
                    at,
                    main.path("temp").asDouble(0.0),
                    wind.path("speed").asDouble(0.0),
                    precipitation,
                    weather.path("id").asInt(800),
                    weather.path("main").asText("Unknown"),
                    weather.path("description").asText("")
            ));
        }

        return entries;
    }

    private boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    private Optional<JsonNode> getJson(String path, Map<String, String> queryParams) {
        try {
            Map<String, String> params = new LinkedHashMap<>(queryParams);
            params.put("appid", apiKey);

            URI uri = buildUri(path, params);
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(timeout)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOGGER.warn("OpenWeather API returned status {} for {}", response.statusCode(), path);
                return Optional.empty();
            }

            return Optional.of(objectMapper.readTree(response.body()));
        } catch (Exception e) {
            LOGGER.warn("OpenWeather request failed on {}: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    private URI buildUri(String path, Map<String, String> params) {
        String query = params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        return URI.create(baseUrl + path + "?" + query);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private double readPrecipitation(JsonNode node, String key) {
        if (node == null || node.isMissingNode()) {
            return 0.0;
        }
        JsonNode keyNode = node.path(key);
        if (!keyNode.isNumber()) {
            return 0.0;
        }
        return keyNode.asDouble(0.0);
    }
}
