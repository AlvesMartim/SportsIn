package org.SportsIn.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

    @Test
    void samePoint_returnsZero() {
        double distance = GeoUtils.calculateDistance(48.8566, 2.3522, 48.8566, 2.3522);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    void paris_to_lyon_approx390km() {
        // Paris (48.8566, 2.3522) → Lyon (45.7640, 4.8357) ≈ 392 km
        double distance = GeoUtils.calculateDistance(48.8566, 2.3522, 45.7640, 4.8357);
        assertTrue(distance > 380 && distance < 400,
                "Distance Paris-Lyon should be ~392km, got: " + distance);
    }

    @Test
    void paris_to_london_approx340km() {
        // Paris (48.8566, 2.3522) → London (51.5074, -0.1278) ≈ 344 km
        double distance = GeoUtils.calculateDistance(48.8566, 2.3522, 51.5074, -0.1278);
        assertTrue(distance > 330 && distance < 355,
                "Distance Paris-London should be ~344km, got: " + distance);
    }

    @Test
    void symmetry_aToB_equalsB_toA() {
        double ab = GeoUtils.calculateDistance(48.8566, 2.3522, 45.7640, 4.8357);
        double ba = GeoUtils.calculateDistance(45.7640, 4.8357, 48.8566, 2.3522);
        assertEquals(ab, ba, 0.001);
    }

    @Test
    void veryClosePoints_smallDistance() {
        // Two points ~111 meters apart (0.001° latitude ≈ 111m)
        double distance = GeoUtils.calculateDistance(48.8566, 2.3522, 48.8576, 2.3522);
        assertTrue(distance < 0.15, "Very close points should be < 150m, got: " + distance + "km");
        assertTrue(distance > 0.05, "Very close points should be > 50m, got: " + distance + "km");
    }

    @Test
    void antipodes_halfCircumference() {
        // North pole to South pole ≈ 20015 km
        double distance = GeoUtils.calculateDistance(90, 0, -90, 0);
        assertTrue(distance > 19900 && distance < 20100,
                "Pole to pole should be ~20015km, got: " + distance);
    }

    @Test
    void equator_points_180degrees() {
        // Two points on equator, 180° apart ≈ 20015 km
        double distance = GeoUtils.calculateDistance(0, 0, 0, 180);
        assertTrue(distance > 19900 && distance < 20100,
                "Equator 180° should be ~20015km, got: " + distance);
    }
}
