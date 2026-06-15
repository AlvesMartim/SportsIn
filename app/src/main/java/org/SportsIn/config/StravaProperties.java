package org.SportsIn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "strava")
public class StravaProperties {

    private String clientId;
    private String clientSecret;
    private String webhookVerifyToken;
    private String redirectUri;
    private String frontendUrl;
    private String apiBaseUrl = "https://www.strava.com/api/v3";
    private String authUrl = "https://www.strava.com/oauth/authorize";
    private String tokenUrl = "https://www.strava.com/oauth/token";
    private int rateLimitPer15Min = 100;
    private int rateLimitPerDay = 1000;
    private int maxActivitiesPerSync = 200;

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getWebhookVerifyToken() { return webhookVerifyToken; }
    public void setWebhookVerifyToken(String webhookVerifyToken) { this.webhookVerifyToken = webhookVerifyToken; }

    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }

    public String getFrontendUrl() { return frontendUrl; }
    public void setFrontendUrl(String frontendUrl) { this.frontendUrl = frontendUrl; }

    public String getApiBaseUrl() { return apiBaseUrl; }
    public void setApiBaseUrl(String apiBaseUrl) { this.apiBaseUrl = apiBaseUrl; }

    public String getAuthUrl() { return authUrl; }
    public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }

    public String getTokenUrl() { return tokenUrl; }
    public void setTokenUrl(String tokenUrl) { this.tokenUrl = tokenUrl; }

    public int getRateLimitPer15Min() { return rateLimitPer15Min; }
    public void setRateLimitPer15Min(int rateLimitPer15Min) { this.rateLimitPer15Min = rateLimitPer15Min; }

    public int getRateLimitPerDay() { return rateLimitPerDay; }
    public void setRateLimitPerDay(int rateLimitPerDay) { this.rateLimitPerDay = rateLimitPerDay; }

    public int getMaxActivitiesPerSync() { return maxActivitiesPerSync; }
    public void setMaxActivitiesPerSync(int maxActivitiesPerSync) { this.maxActivitiesPerSync = maxActivitiesPerSync; }
}
