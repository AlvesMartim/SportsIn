package org.SportsIn.model.progression;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LevelThresholdTest {

    @Test
    void level1_at_0xp() {
        assertEquals(1, LevelThreshold.levelForXp(0));
    }

    @Test
    void level2_at_exactly_100xp() {
        assertEquals(2, LevelThreshold.levelForXp(100));
    }

    @Test
    void level1_at_99xp() {
        assertEquals(1, LevelThreshold.levelForXp(99));
    }

    @Test
    void level3_at_300xp() {
        assertEquals(3, LevelThreshold.levelForXp(300));
    }

    @Test
    void level5_at_1000xp() {
        assertEquals(5, LevelThreshold.levelForXp(1000));
    }

    @Test
    void maxLevel_at_very_high_xp() {
        assertEquals(10, LevelThreshold.levelForXp(999999));
    }

    @Test
    void maxLevel_at_exact_threshold() {
        assertEquals(10, LevelThreshold.levelForXp(5500));
    }

    @Test
    void xpForNextLevel_at_0xp() {
        assertEquals(100, LevelThreshold.xpForNextLevel(0));
    }

    @Test
    void xpForNextLevel_at_50xp() {
        assertEquals(50, LevelThreshold.xpForNextLevel(50));
    }

    @Test
    void xpForNextLevel_at_maxLevel() {
        assertEquals(0, LevelThreshold.xpForNextLevel(5500));
    }

    @Test
    void xpForNextLevel_at_beyond_maxLevel() {
        assertEquals(0, LevelThreshold.xpForNextLevel(10000));
    }

    @Test
    void xpRequiredForLevel_valid() {
        assertEquals(0, LevelThreshold.xpRequiredForLevel(1));
        assertEquals(100, LevelThreshold.xpRequiredForLevel(2));
        assertEquals(5500, LevelThreshold.xpRequiredForLevel(10));
    }

    @Test
    void xpRequiredForLevel_invalid() {
        assertEquals(0, LevelThreshold.xpRequiredForLevel(0));
        assertEquals(0, LevelThreshold.xpRequiredForLevel(11));
    }

    @Test
    void negative_xp_returns_level1() {
        assertEquals(1, LevelThreshold.levelForXp(-100));
    }
}
