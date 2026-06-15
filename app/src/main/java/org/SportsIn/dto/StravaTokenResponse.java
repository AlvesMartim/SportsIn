package org.SportsIn.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Réponse de l'endpoint token Strava (échange code → tokens). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StravaTokenResponse {

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_at")
    private long expiresAt; // epoch seconds

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("athlete")
    private StravaAthleteDTO athlete;

    @JsonProperty("errors")
    private Object errors;

    @JsonProperty("message")
    private String message;

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public int getExpiresIn() { return expiresIn; }
    public void setExpiresIn(int expiresIn) { this.expiresIn = expiresIn; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public StravaAthleteDTO getAthlete() { return athlete; }
    public void setAthlete(StravaAthleteDTO athlete) { this.athlete = athlete; }

    public Object getErrors() { return errors; }
    public void setErrors(Object errors) { this.errors = errors; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isError() {
        return errors != null || accessToken == null || accessToken.isBlank();
    }
}
