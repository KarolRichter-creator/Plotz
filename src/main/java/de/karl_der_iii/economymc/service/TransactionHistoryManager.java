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

public final class TransactionHistoryManager {
    public record Entry(
        String id,
        long timestamp,
        UUID ownerId,
        String text
    ) {}

    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("economymc-history.properties");
    private static final Properties PROPS = new Properties();
    private static boolean loaded = false;

    private TransactionHistoryManager() {}

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
                PROPS.store(out, "EconomyMC history");
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

    public static void add(UUID ownerId, String text) {
        ensureLoaded();
        String id = nextId();
        String base = "entry." + id + ".";
        PROPS.setProperty(base + "time", Long.toString(System.currentTimeMillis()));
        PROPS.setProperty(base + "owner", ownerId.toString());
        PROPS.setProperty(base + "text", text);
        save();
    }

    public static void addTreasury(String text) {
        add(BalanceManager.TREASURY_ACCOUNT_ID, text);
    }

    public static List<Entry> getEntries(UUID ownerId, int limit) {
        ensureLoaded();
        List<Entry> result = new ArrayList<>();

        for (String key : PROPS.stringPropertyNames()) {
            if (!key.startsWith("entry.") || !key.endsWith(".owner")) continue;

            String id = key.substring(6, key.length() - 6);
            String ownerRaw = PROPS.getProperty("entry." + id + ".owner", "");
            if (!ownerId.toString().equals(ownerRaw)) continue;

            long time = parseLong(PROPS.getProperty("entry." + id + ".time", "0"), 0L);
            String text = PROPS.getProperty("entry." + id + ".text", "");
            result.add(new Entry(id, time, ownerId, text));
        }

        result.sort(Comparator.comparingLong(Entry::timestamp).reversed());
        if (result.size() > limit) {
            return new ArrayList<>(result.subList(0, limit));
        }
        return result;
    }

    private static long parseLong(String raw, long fallback) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}