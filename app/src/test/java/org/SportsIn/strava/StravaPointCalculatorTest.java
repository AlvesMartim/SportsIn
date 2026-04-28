package org.SportsIn.strava;

import org.SportsIn.services.strava.StravaPointCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StravaPointCalculatorTest {

    @Test
    @DisplayName("5200 m → 5 points")
    void test5kmRun() {
        assertEquals(5, StravaPointCalculator.calculate(5200.0, 1800L, 0.0));
    }

    @Test
    @DisplayName("10 km → 10 points")
    void test10km() {
        assertEquals(10, StravaPointCalculator.calculate(10000.0, null, null));
    }

    @Test
    @DisplayName("Distance nulle → 1 point minimum")
    void testZeroDistance() {
        assertEquals(1, StravaPointCalculator.calculate(0.0, null, null));
    }

    @Test
    @DisplayName("Distance null → 1 point minimum")
    void testNullDistance() {
        assertEquals(1, StravaPointCalculator.calculate(null, null, null));
    }

    @Test
    @DisplayName("Bonus effort : longue sortie avec dénivelé")
    void testBonusLongActivityWithElevation() {
        // 5 km + 30 min + 100 m dénivelé → 5 + 1 bonus = 6
        assertEquals(6, StravaPointCalculator.calculate(5000.0, 1800L, 100.0));
    }

    @Test
    @DisplayName("Pas de bonus si dénivelé insuffisant")
    void testNoBonusLowElevation() {
        // 5 km + 30 min + 50 m dénivelé → 5 seulement
        assertEquals(5, StravaPointCalculator.calculate(5000.0, 1800L, 50.0));
    }

    @Test
    @DisplayName("Pas de bonus si durée insuffisante")
    void testNoBonusShortActivity() {
        // 5 km + 20 min + 200 m dénivelé → 5 seulement (durée < 30 min)
        assertEquals(5, StravaPointCalculator.calculate(5000.0, 1200L, 200.0));
    }

    @Test
    @DisplayName("500 m → arrondi à 1 point minimum")
    void testSubKilometerRound() {
        assertEquals(1, StravaPointCalculator.calculate(500.0, null, null));
    }

    @Test
    @DisplayName("1500 m → 2 points (arrondi)")
    void testRounding() {
        assertEquals(2, StravaPointCalculator.calculate(1500.0, null, null));
    }
}
