package org.SportsIn.strava;

import org.SportsIn.config.StravaProperties;
import org.SportsIn.dto.StravaActivityDTO;
import org.SportsIn.dto.StravaSyncResultDTO;
import org.SportsIn.model.Arene;
import org.SportsIn.model.strava.StravaActivity;
import org.SportsIn.model.user.Equipe;
import org.SportsIn.model.user.Joueur;
import org.SportsIn.model.territory.RouteRepository;
import org.SportsIn.repository.AreneRepository;
import org.SportsIn.repository.JoueurRepository;
import org.SportsIn.repository.StravaActivityRepository;
import org.SportsIn.services.StravaApiClient;
import org.SportsIn.services.StravaPolylineDecoder;
import org.SportsIn.services.StravaSyncService;
import org.SportsIn.services.XpGrantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StravaSyncServiceTest {

    private StravaApiClient apiClient;
    private StravaActivityRepository activityRepo;
    private JoueurRepository joueurRepo;
    private AreneRepository areneRepo;
    private RouteRepository routeRepo;
    private StravaPolylineDecoder polylineDecoder;
    private XpGrantService xpGrantService;
    private StravaProperties props;
    private ApplicationEventPublisher eventPublisher;
    private StravaSyncService syncService;

    @BeforeEach
    void setUp() {
        apiClient = mock(StravaApiClient.class);
        activityRepo = mock(StravaActivityRepository.class);
        joueurRepo = mock(JoueurRepository.class);
        areneRepo = mock(AreneRepository.class);
        routeRepo = mock(RouteRepository.class);
        polylineDecoder = new StravaPolylineDecoder();
        xpGrantService = mock(XpGrantService.class);
        props = new StravaProperties();
        props.setMaxActivitiesPerSync(200);
        eventPublisher = mock(ApplicationEventPublisher.class);

        syncService = new StravaSyncService(apiClient, activityRepo, joueurRepo, areneRepo,
                routeRepo, polylineDecoder, xpGrantService, props, eventPublisher);

        // Par défaut : joueur sans équipe
        Joueur joueur = new Joueur("testeur", "test@test.com", "pw");
        joueur.setId(1L);
        when(joueurRepo.findById(1L)).thenReturn(Optional.of(joueur));

        when(areneRepo.findAll()).thenReturn(List.of());
        when(routeRepo.findAll()).thenReturn(List.of());
    }

    @Test
    void syncForJoueur_noActivities_returnsEmptyResult() {
        when(apiClient.fetchRecentActivities(1L, 200)).thenReturn(List.of());

        StravaSyncResultDTO result = syncService.syncForJoueur(1L);

        assertEquals(0, result.getActivitiesFetched());
        assertEquals(0, result.getActivitiesImported());
    }

    @Test
    void syncForJoueur_newActivity_isImported() {
        StravaActivityDTO dto = buildActivity(1L, "Run", 5000, 1500, 3.0);
        when(apiClient.fetchRecentActivities(1L, 200)).thenReturn(List.of(dto));
        when(activityRepo.existsByStravaActivityId("1")).thenReturn(false);
        when(activityRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StravaSyncResultDTO result = syncService.syncForJoueur(1L);

        assertEquals(1, result.getActivitiesFetched());
        assertEquals(1, result.getActivitiesImported());
        assertEquals(0, result.getActivitiesSkipped());
        verify(activityRepo).save(any(StravaActivity.class));
    }

    @Test
    void syncForJoueur_duplicateActivity_isSkipped() {
        StravaActivityDTO dto = buildActivity(42L, "Ride", 10000, 2000, 5.0);
        when(apiClient.fetchRecentActivities(1L, 200)).thenReturn(List.of(dto));
        when(activityRepo.existsByStravaActivityId("42")).thenReturn(true);

        StravaSyncResultDTO result = syncService.syncForJoueur(1L);

        assertEquals(1, result.getActivitiesFetched());
        assertEquals(0, result.getActivitiesImported());
        assertEquals(1, result.getActivitiesSkipped());
        verify(activityRepo, never()).save(any());
    }

    @Test
    void syncForJoueur_speedTooHigh_isAntiCheatFlagged() {
        // Run à 20 m/s (72 km/h) → flaggé
        StravaActivityDTO dto = buildActivity(2L, "Run", 5000, 250, 20.0);
        when(apiClient.fetchRecentActivities(1L, 200)).thenReturn(List.of(dto));
        when(activityRepo.existsByStravaActivityId("2")).thenReturn(false);
        when(activityRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StravaSyncResultDTO result = syncService.syncForJoueur(1L);

        assertEquals(1, result.getActivitiesImported()); // Importée mais flaggée
        assertEquals(1, result.getActivitiesFlagged());
        assertFalse(result.getFlaggedReasons().isEmpty());
    }

    @Test
    void syncForJoueur_distanceTooLow_isFlagged() {
        // Run avec seulement 50m → flaggé
        StravaActivityDTO dto = buildActivity(3L, "Run", 50, 30, 1.5);
        when(apiClient.fetchRecentActivities(1L, 200)).thenReturn(List.of(dto));
        when(activityRepo.existsByStravaActivityId("3")).thenReturn(false);
        when(activityRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StravaSyncResultDTO result = syncService.syncForJoueur(1L);

        assertEquals(1, result.getActivitiesFlagged());
    }

    @Test
    void syncForJoueur_withAreneProximity_setsZonesTraversed() {
        // Polyline simple autour de Paris (encodée avec l'arène dans le rayon)
        Arene arene = new Arene("ARENE_1", "Test Arène", 48.856, 2.352);
        when(areneRepo.findAll()).thenReturn(List.of(arene));

        // Activité Run valide (3 m/s) avec une polyline autour de Paris
        StravaActivityDTO dto = buildActivity(4L, "Run", 3000, 1000, 3.0);
        dto.setMap(buildMap("yhfnHsdpgBiDnxF")); // polyline proche de Paris

        when(apiClient.fetchRecentActivities(1L, 200)).thenReturn(List.of(dto));
        when(activityRepo.existsByStravaActivityId("4")).thenReturn(false);

        final StravaActivity[] saved = {null};
        when(activityRepo.save(any())).thenAnswer(inv -> {
            saved[0] = inv.getArgument(0);
            return saved[0];
        });

        syncService.syncForJoueur(1L);

        assertNotNull(saved[0]);
        // Les zones traversées doivent être un JSON array (peut être vide si la polyline est trop loin)
        assertNotNull(saved[0].getZonesTraversed());
    }

    @Test
    void syncForJoueur_withEquipe_grantsXp() {
        Equipe equipe = new Equipe("Rouges");
        equipe.setId(10L);
        Joueur joueur = new Joueur("testeur", "t@t.com", "pw");
        joueur.setId(1L);
        joueur.setEquipe(equipe);
        when(joueurRepo.findById(1L)).thenReturn(Optional.of(joueur));

        StravaActivityDTO dto = buildActivity(5L, "Run", 10000, 3000, 3.0);
        when(apiClient.fetchRecentActivities(1L, 200)).thenReturn(List.of(dto));
        when(activityRepo.existsByStravaActivityId("5")).thenReturn(false);
        when(activityRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        syncService.syncForJoueur(1L);

        // 10km * 5 xp/km = 50 XP
        verify(xpGrantService).grantActivityXp(eq(10L), eq(50));
    }

    @Test
    void syncForJoueur_weightTraining_noSpeedCheck() {
        // Musculation à "vitesse" 0 → ne doit PAS être flaggé
        StravaActivityDTO dto = buildActivity(6L, "WeightTraining", 0, 3600, 0.0);
        when(apiClient.fetchRecentActivities(1L, 200)).thenReturn(List.of(dto));
        when(activityRepo.existsByStravaActivityId("6")).thenReturn(false);
        when(activityRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StravaSyncResultDTO result = syncService.syncForJoueur(1L);

        assertEquals(0, result.getActivitiesFlagged());
    }

    @Test
    void syncForJoueur_influenceGranted_isPositive() {
        StravaActivityDTO dto = buildActivity(7L, "Run", 10000, 3600, 2.8);
        when(apiClient.fetchRecentActivities(1L, 200)).thenReturn(List.of(dto));
        when(activityRepo.existsByStravaActivityId("7")).thenReturn(false);

        final StravaActivity[] saved = {null};
        when(activityRepo.save(any())).thenAnswer(inv -> {
            saved[0] = inv.getArgument(0);
            return saved[0];
        });

        StravaSyncResultDTO result = syncService.syncForJoueur(1L);
        assertTrue(result.getTotalInfluenceGranted() > 0);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private StravaActivityDTO buildActivity(long id, String sport, double distance,
                                            int movingTime, double avgSpeed) {
        StravaActivityDTO dto = new StravaActivityDTO();
        dto.setId(id);
        dto.setName("Activité test " + id);
        dto.setSportType(sport);
        dto.setDistance(distance);
        dto.setMovingTime(movingTime);
        dto.setElapsedTime(movingTime);
        dto.setAverageSpeed(avgSpeed);
        dto.setMaxSpeed(avgSpeed * 1.2);
        dto.setTotalElevationGain(50.0);
        dto.setStartDate("2026-06-01T08:00:00Z");
        return dto;
    }

    private StravaActivityDTO.MapDTO buildMap(String polyline) {
        StravaActivityDTO.MapDTO map = new StravaActivityDTO.MapDTO();
        map.setSummaryPolyline(polyline);
        return map;
    }
}
