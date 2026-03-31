package de.karl_der_iii.economymc.service;

import de.karl_der_iii.economymc.data.PlotzStore;
import de.karl_der_iii.economymc.menu.PlotzCreateSaleMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DraftInputManager {
    public enum Mode {
        PRICE,
        DESCRIPTION,
        BUILT,
        JUSTIFICATION
    }

    private static final Map<UUID, Mode> WAITING = new ConcurrentHashMap<>();

    private DraftInputManager() {}

    public static void waitForPrice(ServerPlayer player) {
        WAITING.put(player.getUUID(), Mode.PRICE);
        player.sendSystemMessage(Component.literal("§eEnter the price in chat now."));
        player.closeContainer();
    }

    public static void waitForDescription(ServerPlayer player) {
        WAITING.put(player.getUUID(), Mode.DESCRIPTION);
        player.sendSystemMessage(Component.literal("§eEnter the description in chat now."));
        player.closeContainer();
    }

    public static void waitForBuilt(ServerPlayer player) {
        WAITING.put(player.getUUID(), Mode.BUILT);
        player.sendSystemMessage(Component.literal("§eEnter what is built on the plot in chat now."));
        player.closeContainer();
    }

    public static void waitForJustification(ServerPlayer player) {
        WAITING.put(player.getUUID(), Mode.JUSTIFICATION);
        player.sendSystemMessage(Component.literal("§eEnter the price justification in chat now."));
        player.closeContainer();
    }

    public static boolean handleChat(ServerPlayer player, String message) {
        Mode mode = WAITING.remove(player.getUUID());
        if (mode == null) return false;

        PlotzStore.SaleDraft draft = PlotzStore.getDraft(player.getUUID());
        if (draft == null) {
            player.sendSystemMessage(Component.literal("§cNo sale draft selected."));
            return true;
        }

        switch (mode) {
            case PRICE -> {
                try {
                    int price = Integer.parseInt(message.trim());
                    if (price <= 0) {
                        player.sendSystemMessage(Component.literal("§cPrice must be above 0."));
                    } else {
                        PlotzStore.updateDraftPrice(player.getUUID(), price);
                        player.sendSystemMessage(Component.literal("§aDraft price set to $" + price));
                    }
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("§cThat is not a valid number."));
                }
            }
            case DESCRIPTION -> {
                PlotzStore.updateDraftDescription(player.getUUID(), message);
                player.sendSystemMessage(Component.literal("§aDraft description updated."));
            }
            case BUILT -> {
                PlotzStore.updateDraftBuilt(player.getUUID(), message);
                player.sendSystemMessage(Component.literal("§aDraft built-on-plot text updated."));
            }
            case JUSTIFICATION -> {
                PlotzStore.updateDraftJustification(player.getUUID(), message);
                player.sendSystemMessage(Component.literal("§aDraft price justification updated."));
            }
        }

        PlotzCreateSaleMenu.open(player);
        return true;
    }
}