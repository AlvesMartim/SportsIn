package org.SportsIn.strava;

import org.SportsIn.config.StravaProperties;
import org.SportsIn.dto.StravaAthleteDTO;
import org.SportsIn.dto.StravaTokenResponse;
import org.SportsIn.model.strava.StravaAccount;
import org.SportsIn.repository.StravaAccountRepository;
import org.SportsIn.services.StravaApiClient;
import org.SportsIn.services.StravaOAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StravaOAuthServiceTest {

    private StravaProperties props;
    private StravaApiClient apiClient;
    private StravaAccountRepository accountRepo;
    private StravaOAuthService service;

    @BeforeEach
    void setUp() {
        props = new StravaProperties();
        props.setClientId("TEST_CLIENT_ID");
        props.setClientSecret("TEST_CLIENT_SECRET");
        props.setRedirectUri("http://localhost:8080/api/strava/callback");
        props.setAuthUrl("https://www.strava.com/oauth/authorize");

        apiClient = mock(StravaApiClient.class);
        accountRepo = mock(StravaAccountRepository.class);
        service = new StravaOAuthService(props, apiClient, accountRepo);
    }

    @Test
    void buildAuthorizationUrl_containsRequiredParams() {
        String url = service.buildAuthorizationUrl(42L);
        assertTrue(url.contains("client_id=TEST_CLIENT_ID"));
        assertTrue(url.contains("redirect_uri="));
        assertTrue(url.contains("state=42"));
        assertTrue(url.contains("scope=read,activity:read_all"));
        assertTrue(url.contains("response_type=code"));
    }

    @Test
    void handleCallback_newAccount_createsAndSaves() {
        StravaTokenResponse tokenResp = buildTokenResponse(12345L, "acc_token", "ref_token", 9999999999L);
        when(apiClient.exchangeCodeForTokens("AUTH_CODE")).thenReturn(tokenResp);
        when(accountRepo.findByJoueurId(1L)).thenReturn(Optional.empty());
        when(accountRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StravaAccount result = service.handleCallback("AUTH_CODE", 1L);

        assertNotNull(result);
        assertEquals(1L, result.getJoueurId());
        assertEquals("12345", result.getStravaAthleteId());
        assertEquals("acc_token", result.getAccessToken());
        assertEquals("ref_token", result.getRefreshToken());
        verify(accountRepo).save(any(StravaAccount.class));
    }

    @Test
    void handleCallback_existingAccount_updatesTokens() {
        StravaAccount existing = new StravaAccount();
        existing.setJoueurId(1L);
        existing.setStravaAthleteId("12345");
        existing.setAccessToken("OLD_TOKEN");

        StravaTokenResponse tokenResp = buildTokenResponse(12345L, "NEW_TOKEN", "NEW_REF", 9999999999L);
        when(apiClient.exchangeCodeForTokens("CODE")).thenReturn(tokenResp);
        when(accountRepo.findByJoueurId(1L)).thenReturn(Optional.of(existing));
        when(accountRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StravaAccount result = service.handleCallback("CODE", 1L);
        assertEquals("NEW_TOKEN", result.getAccessToken());
        assertEquals("NEW_REF", result.getRefreshToken());
    }

    @Test
    void isConnected_accountExists_returnsTrue() {
        when(accountRepo.existsByJoueurId(1L)).thenReturn(true);
        assertTrue(service.isConnected(1L));
    }

    @Test
    void isConnected_noAccount_returnsFalse() {
        when(accountRepo.existsByJoueurId(99L)).thenReturn(false);
        assertFalse(service.isConnected(99L));
    }

    @Test
    void disconnect_callsApiClientDisconnect() {
        service.disconnect(1L);
        verify(apiClient).disconnect(1L);
    }

    @Test
    void getAccount_returnsFromRepo() {
        StravaAccount acc = new StravaAccount();
        acc.setJoueurId(1L);
        when(accountRepo.findByJoueurId(1L)).thenReturn(Optional.of(acc));

        Optional<StravaAccount> result = service.getAccount(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getJoueurId());
    }

    private StravaTokenResponse buildTokenResponse(long athleteId, String access,
                                                    String refresh, long expiresAt) {
        StravaAthleteDTO athlete = new StravaAthleteDTO();
        athlete.setId(athleteId);
        athlete.setFirstname("Test");

        StravaTokenResponse resp = new StravaTokenResponse();
        resp.setAccessToken(access);
        resp.setRefreshToken(refresh);
        resp.setExpiresAt(expiresAt);
        resp.setAthlete(athlete);
        return resp;
    }
}
