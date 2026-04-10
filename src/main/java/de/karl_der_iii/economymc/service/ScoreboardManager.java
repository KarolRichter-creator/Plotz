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

            String title = safe(LanguageManager.tr("scoreboard.balance"));
            if (title.isBlank()) {
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

            int score = 15;
            int line = 0;
            for (Map.Entry<UUID, Long> entry : entries) {
                if (line >= 5) break;

                String team = "ec_line_" + line;
                String fake = fakeEntry(line);
                String name = BalanceManager.resolveDisplayName(server, entry.getKey());
                String prefix = trim(safe(name));

                server.getCommands().performPrefixedCommand(source, "scoreboard teams add " + team);
                server.getCommands().performPrefixedCommand(source, "scoreboard teams modify " + team + " prefix \"" + escape(prefix) + "\"");
                server.getCommands().performPrefixedCommand(source, "scoreboard teams join " + team + " \"" + fake + "\"");
                server.getCommands().performPrefixedCommand(source, "scoreboard players set \"" + fake + "\" " + OBJECTIVE + " " + entry.getValue());

                score--;
                line++;
            }

            String treasuryTeam = "ec_treasury";
            String treasuryFake = "§a";
            String treasuryPrefix = trim(safe(LanguageManager.tr("scoreboard.treasury")));

            server.getCommands().performPrefixedCommand(source, "scoreboard teams add " + treasuryTeam);
            server.getCommands().performPrefixedCommand(source, "scoreboard teams modify " + treasuryTeam + " prefix \"" + escape(treasuryPrefix) + "\"");
            server.getCommands().performPrefixedCommand(source, "scoreboard teams join " + treasuryTeam + " \"" + treasuryFake + "\"");
            server.getCommands().performPrefixedCommand(source, "scoreboard players set \"" + treasuryFake + "\" " + OBJECTIVE + " " + TreasuryManager.getTreasury());
        } catch (Exception ignored) {
        }
    }

    private static void clearTeams(CommandSourceStack source, MinecraftServer server) {
        for (int i = 0; i < 5; i++) {
            server.getCommands().performPrefixedCommand(source, "scoreboard teams remove ec_line_" + i);
        }
        server.getCommands().performPrefixedCommand(source, "scoreboard teams remove ec_treasury");
    }

    private static String fakeEntry(int index) {
        return "§" + Integer.toHexString(index);
    }

    private static String safe(String input) {
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
