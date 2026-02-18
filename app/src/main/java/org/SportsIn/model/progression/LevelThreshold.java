package org.SportsIn.model.progression;

public final class LevelThreshold {

    private static final int[] XP_TABLE = {
        0,      // Niveau 1 : 0 XP
        100,    // Niveau 2 : 100 XP
        300,    // Niveau 3 : 300 XP
        600,    // Niveau 4 : 600 XP
        1000,   // Niveau 5 : 1000 XP
        1500,   // Niveau 6
        2200,   // Niveau 7
        3000,   // Niveau 8
        4000,   // Niveau 9
        5500,   // Niveau 10
    };

    public static int levelForXp(int xp) {
        for (int i = XP_TABLE.length - 1; i >= 0; i--) {
            if (xp >= XP_TABLE[i]) return i + 1;
        }
        return 1;
    }

    public static int xpForNextLevel(int currentXp) {
        int currentLevel = levelForXp(currentXp);
        if (currentLevel >= XP_TABLE.length) return 0;
        return XP_TABLE[currentLevel] - currentXp;
    }

    public static int xpRequiredForLevel(int level) {
        if (level < 1 || level > XP_TABLE.length) return 0;
        return XP_TABLE[level - 1];
    }

    public static int getMaxLevel() {
        return XP_TABLE.length;
    }

    private LevelThreshold() {
    }
}
