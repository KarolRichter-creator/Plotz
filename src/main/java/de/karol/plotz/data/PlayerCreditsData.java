package de.karol.plotz.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCreditsData {
    private static final Map<UUID, Integer> NORMAL_CREDITS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> CAPITAL_CREDITS = new ConcurrentHashMap<>();

    private PlayerCreditsData() {}

    public static int getNormalCredits(UUID playerId) {
        return NORMAL_CREDITS.getOrDefault(playerId, 0);
    }

    public static int getCapitalCredits(UUID playerId) {
        return CAPITAL_CREDITS.getOrDefault(playerId, 0);
    }

    public static void addNormalCredits(UUID playerId, int amount) {
        NORMAL_CREDITS.put(playerId, getNormalCredits(playerId) + amount);
    }

    public static void addCapitalCredits(UUID playerId, int amount) {
        CAPITAL_CREDITS.put(playerId, getCapitalCredits(playerId) + amount);
    }

    public static boolean spendNormalCredit(UUID playerId) {
        int current = getNormalCredits(playerId);
        if (current <= 0) return false;
        NORMAL_CREDITS.put(playerId, current - 1);
        return true;
    }

    public static boolean spendCapitalCredit(UUID playerId) {
        int current = getCapitalCredits(playerId);
        if (current <= 0) return false;
        CAPITAL_CREDITS.put(playerId, current - 1);
        return true;
    }
}
