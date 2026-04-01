package de.karl_der_iii.economymc.service;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public final class AdminSettingsManager {
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("economymc-admin.properties");
    private static final Properties PROPS = new Properties();
    private static boolean loaded = false;

    private static final List<String> SUPPORTED_LANGUAGES = List.of(
        "de_de",
        "en_us",
        "pl_pl",
        "fr_fr",
        "es_es"
    );

    private AdminSettingsManager() {}

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        if (!Files.exists(FILE)) {
            PROPS.setProperty("jobsEnabled", "true");
            PROPS.setProperty("checksEnabled", "true");
            PROPS.setProperty("shopEnabled", "true");
            PROPS.setProperty("plotMarketEnabled", "true");
            PROPS.setProperty("serverModeEnabled", "true");
            PROPS.setProperty("minTaxPercent", "1");
            PROPS.setProperty("minOverduePercent", "1");
            PROPS.setProperty("minCancelPercent", "1");
            PROPS.setProperty("jobAcceptHour", "2");
            PROPS.setProperty("autoTaxEnabled", "true");
            PROPS.setProperty("language", "de_de");
            save();
            return;
        }

        try (InputStream in = Files.newInputStream(FILE)) {
            PROPS.load(in);
        } catch (IOException ignored) {
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                PROPS.store(out, "EconomyMC admin settings");
            }
        } catch (IOException ignored) {
        }
    }

    public static boolean jobsEnabled() {
        ensureLoaded();
        return Boolean.parseBoolean(PROPS.getProperty("jobsEnabled", "true"));
    }

    public static void setJobsEnabled(boolean value) {
        ensureLoaded();
        PROPS.setProperty("jobsEnabled", Boolean.toString(value));
        save();
    }

    public static boolean checksEnabled() {
        ensureLoaded();
        return Boolean.parseBoolean(PROPS.getProperty("checksEnabled", "true"));
    }

    public static void setChecksEnabled(boolean value) {
        ensureLoaded();
        PROPS.setProperty("checksEnabled", Boolean.toString(value));
        save();
    }

    public static boolean shopEnabled() {
        ensureLoaded();
        return Boolean.parseBoolean(PROPS.getProperty("shopEnabled", "true"));
    }

    public static void setShopEnabled(boolean value) {
        ensureLoaded();
        PROPS.setProperty("shopEnabled", Boolean.toString(value));
        save();
    }

    public static boolean plotMarketEnabled() {
        ensureLoaded();
        return Boolean.parseBoolean(PROPS.getProperty("plotMarketEnabled", "true"));
    }

    public static void setPlotMarketEnabled(boolean value) {
        ensureLoaded();
        PROPS.setProperty("plotMarketEnabled", Boolean.toString(value));
        save();
    }

    public static boolean serverModeEnabled() {
        ensureLoaded();
        return Boolean.parseBoolean(PROPS.getProperty("serverModeEnabled", "true"));
    }

    public static void setServerModeEnabled(boolean value) {
        ensureLoaded();
        PROPS.setProperty("serverModeEnabled", Boolean.toString(value));
        save();
    }

    public static boolean autoTaxEnabled() {
        ensureLoaded();
        return Boolean.parseBoolean(PROPS.getProperty("autoTaxEnabled", "true"));
    }

    public static void setAutoTaxEnabled(boolean value) {
        ensureLoaded();
        PROPS.setProperty("autoTaxEnabled", Boolean.toString(value));
        save();
    }

    public static String language() {
        ensureLoaded();
        String value = PROPS.getProperty("language", "de_de").toLowerCase();
        return SUPPORTED_LANGUAGES.contains(value) ? value : "de_de";
    }

    public static void setLanguage(String value) {
        ensureLoaded();
        String normalized = value == null ? "de_de" : value.toLowerCase();
        if (!SUPPORTED_LANGUAGES.contains(normalized)) {
            normalized = "de_de";
        }
        PROPS.setProperty("language", normalized);
        save();
    }

    public static String nextLanguage() {
        ensureLoaded();
        String current = language();
        int idx = SUPPORTED_LANGUAGES.indexOf(current);
        if (idx < 0 || idx + 1 >= SUPPORTED_LANGUAGES.size()) {
            return SUPPORTED_LANGUAGES.get(0);
        }
        return SUPPORTED_LANGUAGES.get(idx + 1);
    }

    public static int minTaxPercent() {
        ensureLoaded();
        return parseMin("minTaxPercent", 1);
    }

    public static void setMinTaxPercent(int value) {
        ensureLoaded();
        PROPS.setProperty("minTaxPercent", Integer.toString(Math.max(0, value)));
        save();
    }

    public static int minOverduePercent() {
        ensureLoaded();
        return parseMin("minOverduePercent", 1);
    }

    public static void setMinOverduePercent(int value) {
        ensureLoaded();
        PROPS.setProperty("minOverduePercent", Integer.toString(Math.max(0, value)));
        save();
    }

    public static int minCancelPercent() {
        ensureLoaded();
        return parseMin("minCancelPercent", 1);
    }

    public static void setMinCancelPercent(int value) {
        ensureLoaded();
        PROPS.setProperty("minCancelPercent", Integer.toString(Math.max(0, value)));
        save();
    }

    public static int jobAcceptHour() {
        ensureLoaded();
        try {
            return Math.max(0, Math.min(23, Integer.parseInt(PROPS.getProperty("jobAcceptHour", "2"))));
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    public static void setJobAcceptHour(int hour) {
        ensureLoaded();
        PROPS.setProperty("jobAcceptHour", Integer.toString(Math.max(0, Math.min(23, hour))));
        save();
    }

    private static int parseMin(String key, int fallback) {
        try {
            return Math.max(0, Integer.parseInt(PROPS.getProperty(key, Integer.toString(fallback))));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}