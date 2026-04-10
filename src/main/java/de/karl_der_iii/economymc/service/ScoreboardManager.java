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

    private ScoreboardManager() {
    }

    public static void update(MinecraftServer server) {
        try {
            CommandSourceStack source = server.createCommandSourceStack()
                .withSuppressedOutput()
                .withPermission(4);

            clear(server);

            if (!AdminSettingsManager.scoreboardEnabled()) {
                return;
            }

            String title = LanguageManager.tr("scoreboard.balance");
            if (title == null || title.isBlank() || title.equals("scoreboard.balance")) {
                title = "Balance";
            }

            server.getCommands().performPrefixedCommand(
                source,
                "scoreboard objectives add " + OBJECTIVE + " dummy \"" + escape(clean(title)) + "\""
            );
            server.getCommands().performPrefixedCommand(source, "scoreboard objectives setdisplay sidebar " + OBJECTIVE);

            List<Map.Entry<UUID, Long>> entries = new ArrayList<>(BalanceManager.getAllBalances().entrySet());
            entries.removeIf(e -> BalanceManager.TREASURY_ACCOUNT_ID.equals(e.getKey()));
            entries.sort(Map.Entry.<UUID, Long>comparingByValue(Comparator.reverseOrder()));

            int score = 15;
            int i = 0;

            for (Map.Entry<UUID, Long> entry : entries) {
                if (i >= 5) break;

                String team = "ec_line_" + i;
                String fake = "fake" + i;
                String name = BalanceManager.resolveDisplayName(server, entry.getKey());
                if (name == null || name.isBlank()) {
                    name = "Player";
                }
                name = trim(clean(name));

                server.getCommands().performPrefixedCommand(source, "scoreboard teams add " + team);
                server.getCommands().performPrefixedCommand(source, "scoreboard teams modify " + team + " prefix \"" + escape(name) + "\"");
                server.getCommands().performPrefixedCommand(source, "scoreboard teams join " + team + " " + fake);
                server.getCommands().performPrefixedCommand(source, "scoreboard players set " + fake + " " + OBJECTIVE + " " + score);

                score--;
                i++;
            }

            String treasury = LanguageManager.tr("common.treasury");
            if (treasury == null || treasury.isBlank() || treasury.equals("common.treasury")) {
                treasury = "Treasury";
            }
            treasury = trim(clean(treasury));

            server.getCommands().performPrefixedCommand(source, "scoreboard teams add ec_treasury");
            server.getCommands().performPrefixedCommand(source, "scoreboard teams modify ec_treasury prefix \"" + escape(treasury) + "\"");
            server.getCommands().performPrefixedCommand(source, "scoreboard teams join ec_treasury treasuryfake");
            server.getCommands().performPrefixedCommand(source, "scoreboard players set treasuryfake " + OBJECTIVE + " " + score);
        } catch (Exception ignored) {
        }
    }

    public static void clear(MinecraftServer server) {
        try {
            CommandSourceStack source = server.createCommandSourceStack()
                .withSuppressedOutput()
                .withPermission(4);

            for (int i = 0; i < 5; i++) {
                server.getCommands().performPrefixedCommand(source, "scoreboard teams remove ec_line_" + i);
            }
            server.getCommands().performPrefixedCommand(source, "scoreboard teams remove ec_treasury");
            server.getCommands().performPrefixedCommand(source, "scoreboard objectives remove " + OBJECTIVE);
        } catch (Exception ignored) {
        }
    }

    private static String clean(String s) {
        return s == null ? "" : s.replaceAll("§.", "").trim();
    }

    private static String trim(String s) {
        return s.length() > 32 ? s.substring(0, 32) : s;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}