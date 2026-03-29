package de.karol.plotz.service;

import de.karol.plotz.data.PlotzStore;
import de.karol.plotz.menu.PlotzShopSellMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ShopInputManager {
    private static final Map<UUID, Boolean> WAITING_FOR_PRICE = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> TRANSITIONING = new ConcurrentHashMap<>();

    private ShopInputManager() {}

    public static void waitForPrice(ServerPlayer player) {
        WAITING_FOR_PRICE.put(player.getUUID(), true);
        TRANSITIONING.put(player.getUUID(), true);
        player.sendSystemMessage(Component.literal("§eEnter the shop price in chat now."));
        player.closeContainer();
    }

    public static boolean isTransitioning(UUID playerId) {
        return TRANSITIONING.containsKey(playerId);
    }

    public static boolean handleChat(ServerPlayer player, String message) {
        if (!WAITING_FOR_PRICE.containsKey(player.getUUID())) {
            return false;
        }

        WAITING_FOR_PRICE.remove(player.getUUID());
        TRANSITIONING.remove(player.getUUID());

        PlotzStore.ShopDraft draft = PlotzStore.getShopDraft(player.getUUID());
        if (draft == null || draft.items().isEmpty()) {
            player.sendSystemMessage(Component.literal("§cNo shop draft selected."));
            return true;
        }

        try {
            int price = Integer.parseInt(message.trim());
            if (price <= 0) {
                player.sendSystemMessage(Component.literal("§cPrice must be above 0."));
            } else {
                PlotzStore.updateShopDraftPrice(player.getUUID(), price);
                player.sendSystemMessage(Component.literal("§aShop price set to $" + price));
            }
        } catch (NumberFormatException e) {
            player.sendSystemMessage(Component.literal("§cThat is not a valid number."));
        }

        PlotzShopSellMenu.open(player);
        return true;
    }
}