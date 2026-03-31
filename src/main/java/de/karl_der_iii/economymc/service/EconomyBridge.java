package de.karl_der_iii.economymc.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public final class EconomyBridge {
    private EconomyBridge() {}

    public static long getBalance(ServerPlayer player) {
        return BalanceManager.getBalance(player.getUUID());
    }

    public static boolean hasEnough(ServerPlayer player, long amount) {
        return getBalance(player) >= amount;
    }

    public static boolean removeMoney(ServerPlayer player, int amount) {
        boolean success = BalanceManager.removeBalance(player.getUUID(), amount);
        if (success) {
            ScoreboardManager.update(player.server);
        }
        return success;
    }

    public static boolean addMoney(MinecraftServer server, String playerName, int amount) {
        if (playerName == null || playerName.isBlank() || playerName.equals("System")) {
            return true;
        }

        Optional<UUID> resolved = BalanceManager.resolveKnownPlayer(server, playerName);
        if (resolved.isEmpty()) {
            return false;
        }

        BalanceManager.addBalance(resolved.get(), amount);
        ScoreboardManager.update(server);
        return true;
    }
}