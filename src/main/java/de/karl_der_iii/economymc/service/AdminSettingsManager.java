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
        "de_de", "en_us", "pl_pl", "fr_fr", "es_es",
        "pt_br", "ru_ru", "tr_tr", "zh_cn", "ja_jp"
    );

    private AdminSettingsManager() {
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        if (!Files.exists(FILE)) {
            PROPS.setProperty("jobsEnabled", "true");
            PROPS.setProperty("checksEnabled", "true");
            PROPS.setProperty("shopEnabled", "true");
            PROPS.setProperty("plotMarketEnabled", "true");
            PROPS.setProperty("serverModeEnabled", "true");
            PROPS.setProperty("serverShopEnabled", "true");
            PROPS.setProperty("dailyEnabled", "true");

            PROPS.setProperty("minTaxPercent", "1");
            PROPS.setProperty("minOverduePercent", "1");
            PROPS.setProperty("minCancelPercent", "1");
            PROPS.setProperty("jobAcceptHour", "2");

            PROPS.setProperty("autoTaxEnabled", "true");
            PROPS.setProperty("autoTaxReactionStrength", "5");
            PROPS.setProperty("autoTaxMinReactionStrength", "1");
            PROPS.setProperty("treasuryTargetBudget", "200000");

            PROPS.setProperty("pendingAutoTaxDisable", "false");
            PROPS.setProperty("pendingAutoTaxDisableRequester", "");

            PROPS.setProperty("pendingBudgetChange", "false");
            PROPS.setProperty("pendingBudgetChangeRequester", "");
            PROPS.setProperty("pendingBudgetValue", "200000");

            PROPS.setProperty("language", "de_de");
            PROPS.setProperty("dailyBaseReward", "100");
            PROPS.setProperty("dailyIncreasePercent", "1");
            PROPS.setProperty("dailyMaxReward", "200");

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

    public static boolean serverShopEnabled() {
        ensureLoaded();
        return Boolean.parseBoolean(PROPS.getProperty("serverShopEnabled", "true"));
    }

    public static void setServerShopEnabled(boolean value) {
        ensureLoaded();
        PROPS.setProperty("serverShopEnabled", Boolean.toString(value));
        save();
    }

    public static boolean dailyEnabled() {
        ensureLoaded();
        return Boolean.parseBoolean(PROPS.getProperty("dailyEnabled", "true"));
    }

    public static void setDailyEnabled(boolean value) {
        ensureLoaded();
        PROPS.setProperty("dailyEnabled", Boolean.toString(value));
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

    public static int autoTaxReactionStrength() {
        ensureLoaded();
        try {
            return Math.max(1, Math.min(10, Integer.parseInt(PROPS.getProperty("autoTaxReactionStrength", "5"))));
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    public static void setAutoTaxReactionStrength(int value) {
        ensureLoaded();
        int min = autoTaxMinReactionStrength();
        PROPS.setProperty("autoTaxReactionStrength", Integer.toString(Math.max(min, Math.min(10, value))));
        save();
    }

    public static int autoTaxMinReactionStrength() {
        ensureLoaded();
        try {
            return Math.max(0, Math.min(10, Integer.parseInt(PROPS.getProperty("autoTaxMinReactionStrength", "1"))));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public static void setAutoTaxMinReactionStrength(int value) {
        ensureLoaded();
        int clamped = Math.max(0, Math.min(10, value));
        PROPS.setProperty("autoTaxMinReactionStrength", Integer.toString(clamped));

        int current = autoTaxReactionStrength();
        if (current < clamped) {
            PROPS.setProperty("autoTaxReactionStrength", Integer.toString(clamped));
        }

        save();
    }

    public static long treasuryTargetBudget() {
        ensureLoaded();
        try {
            return Math.max(10000L, Long.parseLong(PROPS.getProperty("treasuryTargetBudget", "200000")));
        } catch (NumberFormatException e) {
            return 200000L;
        }
    }

    public static void setTreasuryTargetBudget(long value) {
        ensureLoaded();
        PROPS.setProperty("treasuryTargetBudget", Long.toString(Math.max(10000L, value)));
        save();
    }

    public static boolean hasPendingAutoTaxDisableRequest() {
        ensureLoaded();
        return Boolean.parseBoolean(PROPS.getProperty("pendingAutoTaxDisable", "false"));
    }

    public static String pendingAutoTaxDisableRequester() {
        ensureLoaded();
        return PROPS.getProperty("pendingAutoTaxDisableRequester", "");
    }

    public static void createPendingAutoTaxDisableRequest(String requesterName) {
        ensureLoaded();
        PROPS.setProperty("pendingAutoTaxDisable", "true");
        PROPS.setProperty("pendingAutoTaxDisableRequester", requesterName == null ? "" : requesterName);
        save();
    }

    public static void approvePendingAutoTaxDisableRequest() {
        ensureLoaded();
        PROPS.setProperty("pendingAutoTaxDisable", "false");
        PROPS.setProperty("pendingAutoTaxDisableRequester", "");
        PROPS.setProperty("autoTaxEnabled", "false");
        save();
    }

    public static void denyPendingAutoTaxDisableRequest() {
        ensureLoaded();
        PROPS.setProperty("pendingAutoTaxDisable", "false");
        PROPS.setProperty("pendingAutoTaxDisableRequester", "");
        save();
    }

    public static boolean hasPendingBudgetChange() {
        ensureLoaded();
        return Boolean.parseBoolean(PROPS.getProperty("pendingBudgetChange", "false"));
    }

    public static String pendingBudgetChangeRequester() {
        ensureLoaded();
        return PROPS.getProperty("pendingBudgetChangeRequester", "");
    }

    public static long pendingBudgetValue() {
        ensureLoaded();
        try {
            return Long.parseLong(PROPS.getProperty("pendingBudgetValue", "200000"));
        } catch (NumberFormatException e) {
            return treasuryTargetBudget();
        }
    }

    public static void createPendingBudgetChange(String requesterName, long value) {
        ensureLoaded();
        PROPS.setProperty("pendingBudgetChange", "true");
        PROPS.setProperty("pendingBudgetChangeRequester", requesterName == null ? "" : requesterName);
        PROPS.setProperty("pendingBudgetValue", Long.toString(Math.max(10000L, value)));
        save();
    }

    public static void approvePendingBudgetChange() {
        ensureLoaded();
        long value = pendingBudgetValue();
        PROPS.setProperty("treasuryTargetBudget", Long.toString(Math.max(10000L, value)));
        PROPS.setProperty("pendingBudgetChange", "false");
        PROPS.setProperty("pendingBudgetChangeRequester", "");
        PROPS.setProperty("pendingBudgetValue", Long.toString(value));
        save();
    }

    public static void denyPendingBudgetChange() {
        ensureLoaded();
        PROPS.setProperty("pendingBudgetChange", "false");
        PROPS.setProperty("pendingBudgetChangeRequester", "");
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

    public static int dailyBaseReward() {
        ensureLoaded();
        try {
            return Math.max(1, Integer.parseInt(PROPS.getProperty("dailyBaseReward", "100")));
        } catch (NumberFormatException e) {
            return 100;
        }
    }

    public static void setDailyBaseReward(int value) {
        ensureLoaded();
        PROPS.setProperty("dailyBaseReward", Integer.toString(Math.max(1, value)));
        save();
    }

    public static int dailyIncreasePercent() {
        ensureLoaded();
        try {
            return Math.max(0, Integer.parseInt(PROPS.getProperty("dailyIncreasePercent", "1")));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public static void setDailyIncreasePercent(int value) {
        ensureLoaded();
        PROPS.setProperty("dailyIncreasePercent", Integer.toString(Math.max(0, value)));
        save();
    }

    public static int dailyMaxReward() {
        ensureLoaded();
        try {
            return Math.max(dailyBaseReward(), Integer.parseInt(PROPS.getProperty("dailyMaxReward", "200")));
        } catch (NumberFormatException e) {
            return 200;
        }
    }

    public static void setDailyMaxReward(int value) {
        ensureLoaded();
        PROPS.setProperty("dailyMaxReward", Integer.toString(Math.max(dailyBaseReward(), value)));
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
