package de.karl_der_iii.economymc.service;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class TreasuryManager {
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("plotz-treasury.properties");
    private static final Properties PROPS = new Properties();
    private static boolean loaded = false;

    private TreasuryManager() {}

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        if (!Files.exists(FILE)) {
            PROPS.setProperty("treasury", "0");
            PROPS.setProperty("taxPercent", "1");
            PROPS.setProperty("overduePenaltyPercent", "1");
            PROPS.setProperty("cancelPenaltyPercent", "1");
            PROPS.setProperty("maxOverdueDays", "30");
            save();
            BalanceManager.setBalance(BalanceManager.SERVER_ACCOUNT_ID, 0);
            return;
        }

        try (InputStream in = Files.newInputStream(FILE)) {
            PROPS.load(in);
        } catch (IOException ignored) {
        }

        long treasury = readLong("treasury", 0L);
        BalanceManager.setBalance(BalanceManager.SERVER_ACCOUNT_ID, treasury);
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                PROPS.store(out, "Plotz treasury config");
            }
        } catch (IOException ignored) {
        }
    }

    public static long getTreasury() {
        ensureLoaded();
        long treasury = readLong("treasury", 0L);
        long serverBalance = BalanceManager.getBalance(BalanceManager.SERVER_ACCOUNT_ID);

        if (serverBalance != treasury) {
            treasury = serverBalance;
            PROPS.setProperty("treasury", Long.toString(treasury));
            save();
        }

        return treasury;
    }

    public static void setTreasury(long amount) {
        ensureLoaded();
        PROPS.setProperty("treasury", Long.toString(amount));
        save();
        BalanceManager.setBalance(BalanceManager.SERVER_ACCOUNT_ID, amount);
    }

    public static void addTreasury(long amount) {
        setTreasury(getTreasury() + amount);
    }

    public static boolean removeTreasury(long amount) {
        long current = getTreasury();
        if (current < amount) {
            return false;
        }
        setTreasury(current - amount);
        return true;
    }

    public static int getTaxPercent() {
        ensureLoaded();
        return parseClamped("taxPercent", AdminSettingsManager.minTaxPercent());
    }

    public static void setTaxPercent(int percent) {
        ensureLoaded();
        PROPS.setProperty("taxPercent", Integer.toString(Math.max(AdminSettingsManager.minTaxPercent(), Math.min(100, percent))));
        save();
    }

    public static int getOverduePenaltyPercent() {
        ensureLoaded();
        return parseClamped("overduePenaltyPercent", AdminSettingsManager.minOverduePercent());
    }

    public static void setOverduePenaltyPercent(int percent) {
        ensureLoaded();
        PROPS.setProperty("overduePenaltyPercent", Integer.toString(Math.max(AdminSettingsManager.minOverduePercent(), Math.min(100, percent))));
        save();
    }

    public static int getCancelPenaltyPercent() {
        ensureLoaded();
        return parseClamped("cancelPenaltyPercent", AdminSettingsManager.minCancelPercent());
    }

    public static void setCancelPenaltyPercent(int percent) {
        ensureLoaded();
        PROPS.setProperty("cancelPenaltyPercent", Integer.toString(Math.max(AdminSettingsManager.minCancelPercent(), Math.min(100, percent))));
        save();
    }

    public static int getMaxOverdueDays() {
        ensureLoaded();
        try {
            return Math.max(10, Integer.parseInt(PROPS.getProperty("maxOverdueDays", "30")));
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    public static void setMaxOverdueDays(int days) {
        ensureLoaded();
        PROPS.setProperty("maxOverdueDays", Integer.toString(Math.max(10, days)));
        save();
    }

    public static int calculateTax(int baseAmount) {
        return (int) Math.floor(baseAmount * (getTaxPercent() / 100.0));
    }

    public static int calculateTotalWithTax(int baseAmount) {
        return baseAmount + calculateTax(baseAmount);
    }

    public static int calculateCancelPenalty(int reward) {
        return (int) Math.floor(reward * (getCancelPenaltyPercent() / 100.0));
    }

    public static int calculateOverdueReduction(int reward, long overdueDays) {
        return (int) Math.floor(reward * ((getOverduePenaltyPercent() / 100.0) * overdueDays));
    }

    private static int parseClamped(String key, int fallback) {
        try {
            return Math.max(fallback, Math.min(100, Integer.parseInt(PROPS.getProperty(key, Integer.toString(fallback)))));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static long readLong(String key, long fallback) {
        try {
            return Long.parseLong(PROPS.getProperty(key, Long.toString(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}