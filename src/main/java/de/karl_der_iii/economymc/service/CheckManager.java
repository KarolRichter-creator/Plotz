package de.karl_der_iii.economymc.service;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public final class CheckManager {
    public record CheckEntry(
        String id,
        UUID creatorId,
        String creatorName,
        int amount,
        String code,
        boolean redeemed,
        String redeemedBy
    ) {}

    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("plotz-checks.properties");
    private static final Properties PROPS = new Properties();
    private static boolean loaded = false;

    private CheckManager() {}

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        if (!Files.exists(FILE)) {
            PROPS.setProperty("nextId", "1");
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
                PROPS.store(out, "Plotz checks");
            }
        } catch (IOException ignored) {
        }
    }

    private static String nextId() {
        ensureLoaded();
        int next = Integer.parseInt(PROPS.getProperty("nextId", "1"));
        PROPS.setProperty("nextId", Integer.toString(next + 1));
        save();
        return Integer.toString(next);
    }

    public static CheckEntry createCheck(UUID creatorId, String creatorName, int amount, String code) {
        ensureLoaded();

        String id = nextId();
        String base = "check." + id + ".";
        PROPS.setProperty(base + "creatorId", creatorId.toString());
        PROPS.setProperty(base + "creatorName", creatorName);
        PROPS.setProperty(base + "amount", Integer.toString(amount));
        PROPS.setProperty(base + "code", code);
        PROPS.setProperty(base + "redeemed", "false");
        PROPS.setProperty(base + "redeemedBy", "");
        save();

        TransactionHistoryManager.add(creatorId, LanguageManager.format("history.check.create", amount));
        return getCheck(id);
    }

    public static CheckEntry getCheck(String id) {
        ensureLoaded();
        String base = "check." + id + ".";
        if (!PROPS.containsKey(base + "creatorId")) return null;

        return new CheckEntry(
            id,
            UUID.fromString(PROPS.getProperty(base + "creatorId")),
            PROPS.getProperty(base + "creatorName", "Unknown"),
            Integer.parseInt(PROPS.getProperty(base + "amount", "0")),
            PROPS.getProperty(base + "code", ""),
            Boolean.parseBoolean(PROPS.getProperty(base + "redeemed", "false")),
            PROPS.getProperty(base + "redeemedBy", "")
        );
    }

    public static List<CheckEntry> getAllChecks() {
        ensureLoaded();
        List<CheckEntry> result = new ArrayList<>();
        for (String key : PROPS.stringPropertyNames()) {
            if (key.startsWith("check.") && key.endsWith(".creatorId")) {
                String id = key.substring(6, key.length() - 10);
                CheckEntry entry = getCheck(id);
                if (entry != null) result.add(entry);
            }
        }
        result.sort(Comparator.comparingInt(a -> -Integer.parseInt(a.id())));
        return result;
    }

    public static boolean redeem(String id, String code, UUID redeemerId, String redeemerName) {
        CheckEntry entry = getCheck(id);
        if (entry == null || entry.redeemed()) return false;
        if (!entry.code().equals(code)) return false;

        String base = "check." + id + ".";
        PROPS.setProperty(base + "redeemed", "true");
        PROPS.setProperty(base + "redeemedBy", redeemerName);
        save();

        BalanceManager.addBalance(redeemerId, entry.amount());
        TransactionHistoryManager.add(redeemerId, LanguageManager.format("history.check.redeem", entry.amount()));
        return true;
    }
}