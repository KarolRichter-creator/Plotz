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

            server.getCommands().performPrefixedCommand(source, "scoreboard objectives remove " + OBJECTIVE);

            if (!AdminSettingsManager.scoreboardEnabled()) {
                return;
            }

            String title = clean(LanguageManager.tr("scoreboard.balance"));
            if (title.isBlank() || title.equals("scoreboard.balance")) {
                title = "Balance";
            }

            server.getCommands().performPrefixedCommand(
                source,
                "scoreboard objectives add " + OBJECTIVE + " dummy \"" + escape(title) + "\""
            );
            server.getCommands().performPrefixedCommand(source, "scoreboard objectives setdisplay sidebar " + OBJECTIVE);

            clearTeams(source, server);

            List<Map.Entry<UUID, Long>> entries = new ArrayList<>(BalanceManager.getAllBalances().entrySet());
            entries.removeIf(e -> BalanceManager.TREASURY_ACCOUNT_ID.equals(e.getKey()));
            entries.sort(Map.Entry.<UUID, Long>comparingByValue(Comparator.reverseOrder()));

            int line = 0;
            for (Map.Entry<UUID, Long> entry : entries) {
                if (line >= 5) break;

                String team = "ec_line_" + line;
                String fake = "§" + Integer.toHexString(line);
                String name = clean(BalanceManager.resolveDisplayName(server, entry.getKey()));
                if (name.isBlank()) {
                    name = "Player";
                }

                server.getCommands().performPrefixedCommand(source, "scoreboard teams add " + team);
                server.getCommands().performPrefixedCommand(
                    source,
                    "scoreboard teams modify " + team + " prefix \"" + escape(trim(name)) + "\""
                );
                server.getCommands().performPrefixedCommand(
                    source,
                    "scoreboard teams join " + team + " \"" + fake + "\""
                );
                server.getCommands().performPrefixedCommand(
                    source,
                    "scoreboard players set \"" + fake + "\" " + OBJECTIVE + " " + entry.getValue()
                );

                line++;
            }

            String treasuryTeam = "ec_treasury";
            String treasuryFake = "§a";
            String treasuryName = clean(LanguageManager.tr("common.treasury"));
            if (treasuryName.isBlank() || treasuryName.equals("common.treasury")) {
                treasuryName = "Treasury";
            }

            server.getCommands().performPrefixedCommand(source, "scoreboard teams add " + treasuryTeam);
            server.getCommands().performPrefixedCommand(
                source,
                "scoreboard teams modify " + treasuryTeam + " prefix \"" + escape(trim(treasuryName)) + "\""
            );
            server.getCommands().performPrefixedCommand(
                source,
                "scoreboard teams join " + treasuryTeam + " \"" + treasuryFake + "\""
            );
            server.getCommands().performPrefixedCommand(
                source,
                "scoreboard players set \"" + treasuryFake + "\" " + OBJECTIVE + " " + TreasuryManager.getTreasury()
            );
        } catch (Exception ignored) {
        }
    }

    public static void clear(MinecraftServer server) {
        try {
            CommandSourceStack source = server.createCommandSourceStack()
                .withSuppressedOutput()
                .withPermission(4);

            clearTeams(source, server);
            server.getCommands().performPrefixedCommand(source, "scoreboard objectives remove " + OBJECTIVE);
        } catch (Exception ignored) {
        }
    }

    private static void clearTeams(CommandSourceStack source, MinecraftServer server) {
        for (int i = 0; i < 5; i++) {
            server.getCommands().performPrefixedCommand(source, "scoreboard teams remove ec_line_" + i);
        }
        server.getCommands().performPrefixedCommand(source, "scoreboard teams remove ec_treasury");
    }

    private static String clean(String input) {
        if (input == null) return "";
        return input.replaceAll("§.", "").trim();
    }

    private static String trim(String s) {
        return s.length() > 32 ? s.substring(0, 32) : s;
    }

    private static String escape(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}