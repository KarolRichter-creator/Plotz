package de.karl_der_iii.economymc.service;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

public final class DailyRewardManager {
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("plotz-daily.properties");
    private static final Properties PROPS = new Properties();
    private static boolean loaded = false;

    private DailyRewardManager() {}

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        if (!Files.exists(FILE)) return;

        try (InputStream in = Files.newInputStream(FILE)) {
            PROPS.load(in);
        } catch (IOException ignored) {
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                PROPS.store(out, "Plotz daily rewards");
            }
        } catch (IOException ignored) {
        }
    }

    public static boolean canClaim(UUID playerId) {
        ensureLoaded();
        long now = System.currentTimeMillis();
        long last = getLastClaim(playerId);
        return now - last >= 24L * 60L * 60L * 1000L;
    }

    public static long getRemainingMs(UUID playerId) {
        ensureLoaded();
        long now = System.currentTimeMillis();
        long last = getLastClaim(playerId);
        long needed = 24L * 60L * 60L * 1000L;
        long passed = now - last;
        return Math.max(0L, needed - passed);
    }

    public static void markClaimed(UUID playerId) {
        ensureLoaded();
        PROPS.setProperty(playerId.toString(), Long.toString(System.currentTimeMillis()));
        save();
    }

    private static long getLastClaim(UUID playerId) {
        ensureLoaded();
        String value = PROPS.getProperty(playerId.toString(), "0");
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}