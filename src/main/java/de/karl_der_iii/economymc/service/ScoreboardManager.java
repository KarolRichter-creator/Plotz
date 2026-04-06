package de.karl_der_iii.economymc.service;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ScoreboardManager {
    private static final String OBJECTIVE = "economymc_balance";

    private ScoreboardManager() {}

    public static void update(MinecraftServer server) {
        try {
            CommandSourceStack source = server.createCommandSourceStack()
                .withSuppressedOutput()
                .withPermission(4);

            server.getCommands().performPrefixedCommand(source, "scoreboard objectives remove " + OBJECTIVE);
            server.getCommands().performPrefixedCommand(
                source,
                "scoreboard objectives add " + OBJECTIVE + " dummy \"" + sanitizeTitle(LanguageManager.tr("scoreboard.title")) + "\""
            );
            server.getCommands().performPrefixedCommand(source, "scoreboard objectives setdisplay sidebar " + OBJECTIVE);

            List<Map.Entry<UUID, Long>> entries = new ArrayList<>(BalanceManager.getAllBalances().entrySet());
            entries.removeIf(e -> BalanceManager.TREASURY_ACCOUNT_ID.equals(e.getKey()));
            entries.sort(Map.Entry.<UUID, Long>comparingByValue(Comparator.reverseOrder()));

            int count = 0;
            for (Map.Entry<UUID, Long> entry : entries) {
                if (count >= 5) break;

                String name = sanitize(BalanceManager.resolveDisplayName(server, entry.getKey()));
                server.getCommands().performPrefixedCommand(
                    source,
                    "scoreboard players set " + name + " " + OBJECTIVE + " " + entry.getValue()
                );
                count++;
            }

            long treasury = TreasuryManager.getTreasury();
            server.getCommands().performPrefixedCommand(
                source,
                "scoreboard players set " + sanitize(LanguageManager.tr("common.treasury")) + " " + OBJECTIVE + " " + treasury
            );
            server.getCommands().performPrefixedCommand(source, "scoreboard objectives setdisplay sidebar " + OBJECTIVE);
        } catch (Exception ignored) {
        }
    }

    private static String sanitize(String input) {
        return input.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static String sanitizeTitle(String input) {
        return input.replace("\"", "");
    }
}