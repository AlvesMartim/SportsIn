package org.SportsIn.strava;

import org.SportsIn.services.strava.StravaSportMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StravaSportMapperTest {

    private StravaSportMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new StravaSportMapper();
    }

    @Test
    @DisplayName("Run → RUNNING")
    void testRun() {
        assertEquals("RUNNING", mapper.mapToSportsInCode("Run"));
    }

    @Test
    @DisplayName("TrailRun → RUNNING")
    void testTrailRun() {
        assertEquals("RUNNING", mapper.mapToSportsInCode("TrailRun"));
    }

    @Test
    @DisplayName("Ride → CYCLING")
    void testRide() {
        assertEquals("CYCLING", mapper.mapToSportsInCode("Ride"));
    }

    @Test
    @DisplayName("MountainBikeRide → CYCLING")
    void testMountainBike() {
        assertEquals("CYCLING", mapper.mapToSportsInCode("MountainBikeRide"));
    }

    @Test
    @DisplayName("Walk → WALKING")
    void testWalk() {
        assertEquals("WALKING", mapper.mapToSportsInCode("Walk"));
    }

    @Test
    @DisplayName("Hike → WALKING")
    void testHike() {
        assertEquals("WALKING", mapper.mapToSportsInCode("Hike"));
    }

    @Test
    @DisplayName("Soccer → FOOTBALL")
    void testSoccer() {
        assertEquals("FOOTBALL", mapper.mapToSportsInCode("Soccer"));
    }

    @Test
    @DisplayName("WeightTraining → MUSCULATION")
    void testWeightTraining() {
        assertEquals("MUSCULATION", mapper.mapToSportsInCode("WeightTraining"));
    }

    @Test
    @DisplayName("Tennis → TENNIS")
    void testTennis() {
        assertEquals("TENNIS", mapper.mapToSportsInCode("Tennis"));
    }

    @Test
    @DisplayName("Basketball → BASKET")
    void testBasketball() {
        assertEquals("BASKET", mapper.mapToSportsInCode("Basketball"));
    }

    @Test
    @DisplayName("Type inconnu → OUTDOOR")
    void testUnknownType() {
        assertEquals("OUTDOOR", mapper.mapToSportsInCode("Kayaking"));
    }

    @Test
    @DisplayName("null → OUTDOOR")
    void testNullType() {
        assertEquals("OUTDOOR", mapper.mapToSportsInCode(null));
    }
}
