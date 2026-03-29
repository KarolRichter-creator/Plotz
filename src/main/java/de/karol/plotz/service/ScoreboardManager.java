package de.karol.plotz.service;

import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ScoreboardManager {
    private static final String OBJECTIVE = "plotz_balance";

    private ScoreboardManager() {}

    public static void update(MinecraftServer server) {
        try {
            CommandSourceStack source = server.createCommandSourceStack()
                .withSuppressedOutput()
                .withPermission(4);

            server.getCommands().performPrefixedCommand(source, "scoreboard objectives remove " + OBJECTIVE);
            server.getCommands().performPrefixedCommand(source, "scoreboard objectives add " + OBJECTIVE + " dummy Balance");
            server.getCommands().performPrefixedCommand(source, "scoreboard objectives setdisplay sidebar " + OBJECTIVE);

            List<Map.Entry<UUID, Long>> entries = new ArrayList<>(BalanceManager.getAllBalances().entrySet());
            entries.sort(Map.Entry.<UUID, Long>comparingByValue(Comparator.reverseOrder()));

            int count = 0;
            for (Map.Entry<UUID, Long> entry : entries) {
                if (count >= 5) break;

                String name = resolveName(server, entry.getKey());
                String fakeName = (count + 1) + "." + sanitize(name);
                server.getCommands().performPrefixedCommand(
                    source,
                    "scoreboard players set " + fakeName + " " + OBJECTIVE + " " + entry.getValue()
                );
                count++;
            }
        } catch (Exception ignored) {
        }
    }

    private static String sanitize(String input) {
        return input.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static String resolveName(MinecraftServer server, UUID uuid) {
        ServerPlayer online = server.getPlayerList().getPlayer(uuid);
        if (online != null) {
            return online.getGameProfile().getName();
        }

        if (server.getProfileCache() != null) {
            Optional<GameProfile> cached = server.getProfileCache().get(uuid);
            if (cached.isPresent()) {
                return cached.get().getName();
            }
        }

        return uuid.toString().substring(0, 8);
    }
}