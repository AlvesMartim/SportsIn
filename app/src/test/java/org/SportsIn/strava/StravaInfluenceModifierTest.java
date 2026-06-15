package org.SportsIn.strava;

import org.SportsIn.model.strava.StravaActivity;
import org.SportsIn.repository.StravaActivityRepository;
import org.SportsIn.services.StravaInfluenceModifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StravaInfluenceModifierTest {

    private StravaActivityRepository activityRepo;
    private StravaInfluenceModifier modifier;

    @BeforeEach
    void setUp() {
        activityRepo = mock(StravaActivityRepository.class);
        modifier = new StravaInfluenceModifier(activityRepo);
    }

    @Test
    void apply_noActivities_returnsUnchangedModifier() {
        when(activityRepo.findValidByEquipeSince(anyLong(), anyString())).thenReturn(List.of());
        double result = modifier.apply(1L, "ARENE_1", 0.0);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    void apply_activityNotTraversingArene_returnsUnchangedModifier() {
        StravaActivity act = buildActivity(10.0, "[\"ARENE_OTHER\"]");
        when(activityRepo.findValidByEquipeSince(anyLong(), anyString())).thenReturn(List.of(act));

        double result = modifier.apply(1L, "ARENE_1", 0.0);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    void apply_activityTraversingArene_addsBonus() {
        StravaActivity act = buildActivity(10.0, "[\"ARENE_1\",\"ARENE_2\"]");
        when(activityRepo.findValidByEquipeSince(anyLong(), anyString())).thenReturn(List.of(act));

        double result = modifier.apply(1L, "ARENE_1", 0.0);
        // bonus = 10.0 * 0.02 = 0.2
        assertEquals(0.2, result, 0.001);
    }

    @Test
    void apply_multipleActivities_sumsBonuses() {
        StravaActivity act1 = buildActivity(5.0, "[\"ARENE_1\"]");
        StravaActivity act2 = buildActivity(5.0, "[\"ARENE_1\"]");
        when(activityRepo.findValidByEquipeSince(anyLong(), anyString())).thenReturn(List.of(act1, act2));

        double result = modifier.apply(1L, "ARENE_1", 0.0);
        // 5*0.02 + 5*0.02 = 0.2
        assertEquals(0.2, result, 0.001);
    }

    @Test
    void apply_bonusCappedAt040() {
        // 30 activités de 10 d'influence chacune → 30 * 0.2 = 6.0 → capé à 0.40
        List<StravaActivity> acts = new java.util.ArrayList<>();
        for (int i = 0; i < 30; i++) {
            acts.add(buildActivity(10.0, "[\"ARENE_1\"]"));
        }
        when(activityRepo.findValidByEquipeSince(anyLong(), anyString())).thenReturn(acts);

        double result = modifier.apply(1L, "ARENE_1", 0.0);
        assertEquals(0.40, result, 0.001);
    }

    @Test
    void apply_addsToExistingModifier() {
        StravaActivity act = buildActivity(10.0, "[\"ARENE_1\"]");
        when(activityRepo.findValidByEquipeSince(anyLong(), anyString())).thenReturn(List.of(act));

        double result = modifier.apply(1L, "ARENE_1", 0.10);
        assertEquals(0.30, result, 0.001); // 0.10 + 0.20
    }

    @Test
    void apply_nullTeamId_returnsUnchanged() {
        double result = modifier.apply(null, "ARENE_1", 0.5);
        assertEquals(0.5, result, 0.001);
        verifyNoInteractions(activityRepo);
    }

    @Test
    void apply_nullPointId_returnsUnchanged() {
        double result = modifier.apply(1L, null, 0.5);
        assertEquals(0.5, result, 0.001);
        verifyNoInteractions(activityRepo);
    }

    @Test
    void getOrder_returns30() {
        assertEquals(30, modifier.getOrder());
    }

    private StravaActivity buildActivity(double influence, String zonesTraversed) {
        StravaActivity act = new StravaActivity();
        act.setInfluenceGranted(influence);
        act.setZonesTraversed(zonesTraversed);
        act.setAntiCheatFlagged(false);
        return act;
    }
}
