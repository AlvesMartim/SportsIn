package org.SportsIn.services;

import org.SportsIn.model.progression.LevelThreshold;
import org.SportsIn.model.user.Equipe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeamProgressionServiceTest {

    @Test
    void team_with_0xp_is_level1() {
        Equipe team = new Equipe("TestTeam");
        team.setXp(0);
        assertEquals(1, LevelThreshold.levelForXp(team.getXp()));
    }

    @Test
    void team_with_300xp_is_level3() {
        Equipe team = new Equipe("TestTeam");
        team.setXp(300);
        assertEquals(3, LevelThreshold.levelForXp(team.getXp()));
    }

    @Test
    void team_with_599xp_is_level3() {
        Equipe team = new Equipe("TestTeam");
        team.setXp(599);
        assertEquals(3, LevelThreshold.levelForXp(team.getXp()));
    }

    @Test
    void team_with_600xp_is_level4() {
        Equipe team = new Equipe("TestTeam");
        team.setXp(600);
        assertEquals(4, LevelThreshold.levelForXp(team.getXp()));
    }

    @Test
    void xp_needed_for_next_level_from_level1() {
        // At 0 XP (level 1), need 100 XP to reach level 2
        assertEquals(100, LevelThreshold.xpForNextLevel(0));
    }

    @Test
    void xp_needed_midway_through_level() {
        // At 200 XP (level 2, threshold is 100), need 100 more to reach level 3 (300)
        assertEquals(100, LevelThreshold.xpForNextLevel(200));
    }

    @Test
    void progression_is_monotonic() {
        int previousLevel = 0;
        for (int xp = 0; xp <= 6000; xp += 10) {
            int level = LevelThreshold.levelForXp(xp);
            assertTrue(level >= previousLevel,
                    "Level should never decrease: at " + xp + " XP, got level " + level);
            previousLevel = level;
        }
    }
}
