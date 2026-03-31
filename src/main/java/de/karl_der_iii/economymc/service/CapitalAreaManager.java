package de.karl_der_iii.economymc.service;

import net.minecraft.core.BlockPos;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class CapitalAreaManager {
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("plotz-capital.properties");

    private static BlockPos tempPos1;
    private static BlockPos tempPos2;

    private static Integer minX;
    private static Integer maxX;
    private static Integer minZ;
    private static Integer maxZ;

    static {
        load();
    }

    private CapitalAreaManager() {}

    public static void setPos1(BlockPos pos) {
        tempPos1 = pos;
    }

    public static void setPos2(BlockPos pos) {
        tempPos2 = pos;
    }

    public static boolean canCreateArea() {
        return tempPos1 != null && tempPos2 != null;
    }

    public static void applyArea() {
        if (!canCreateArea()) {
            return;
        }

        minX = Math.min(tempPos1.getX(), tempPos2.getX());
        maxX = Math.max(tempPos1.getX(), tempPos2.getX());
        minZ = Math.min(tempPos1.getZ(), tempPos2.getZ());
        maxZ = Math.max(tempPos1.getZ(), tempPos2.getZ());

        save();
    }

    public static void clearArea() {
        tempPos1 = null;
        tempPos2 = null;
        minX = null;
        maxX = null;
        minZ = null;
        maxZ = null;
        save();
    }

    public static boolean hasArea() {
        return minX != null && maxX != null && minZ != null && maxZ != null;
    }

    public static boolean isInside(BlockPos pos) {
        if (!hasArea()) {
            return false;
        }

        return pos.getX() >= minX
            && pos.getX() <= maxX
            && pos.getZ() >= minZ
            && pos.getZ() <= maxZ;
    }

    private static void load() {
        if (!Files.exists(FILE)) {
            return;
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            props.load(in);

            minX = parseNullable(props.getProperty("minX"));
            maxX = parseNullable(props.getProperty("maxX"));
            minZ = parseNullable(props.getProperty("minZ"));
            maxZ = parseNullable(props.getProperty("maxZ"));
        } catch (IOException ignored) {
        }
    }

    private static void save() {
        Properties props = new Properties();

        props.setProperty("minX", valueOrEmpty(minX));
        props.setProperty("maxX", valueOrEmpty(maxX));
        props.setProperty("minZ", valueOrEmpty(minZ));
        props.setProperty("maxZ", valueOrEmpty(maxZ));

        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                props.store(out, "Plotz capital area");
            }
        } catch (IOException ignored) {
        }
    }

    private static Integer parseNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private static String valueOrEmpty(Integer value) {
        return value == null ? "" : Integer.toString(value);
    }
}