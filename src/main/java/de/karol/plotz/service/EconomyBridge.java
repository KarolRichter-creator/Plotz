package de.karol.plotz.service;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class EconomyBridge {
    private EconomyBridge() {}

    public static boolean removeMoney(ServerPlayer player, int amount) {
        try {
            MinecraftServer server = player.server;
            CommandSourceStack source = server.createCommandSourceStack()
                .withSuppressedOutput()
                .withPermission(4);

            String name = player.getGameProfile().getName();
            server.getCommands().performPrefixedCommand(source, "eco removemoney " + name + " " + amount);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void addMoney(MinecraftServer server, String playerName, int amount) {
        try {
            if (playerName == null || playerName.isBlank() || playerName.equals("System")) {
                return;
            }

            CommandSourceStack source = server.createCommandSourceStack()
                .withSuppressedOutput()
                .withPermission(4);

            server.getCommands().performPrefixedCommand(source, "eco addmoney " + playerName + " " + amount);
        } catch (Exception ignored) {
        }
    }
}