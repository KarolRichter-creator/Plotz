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
        player.sendSystemMessage(Component.literal(LanguageManager.tr("common.price")));
        player.closeContainer();
    }

    public static void waitForDescription(ServerPlayer player) {
        WAITING.put(player.getUUID(), Mode.DESCRIPTION);
        player.sendSystemMessage(Component.literal(LanguageManager.tr("common.description")));
        player.closeContainer();
    }

    public static void waitForBuilt(ServerPlayer player) {
        WAITING.put(player.getUUID(), Mode.BUILT);
        player.sendSystemMessage(Component.literal(LanguageManager.tr("common.edit")));
        player.closeContainer();
    }

    public static void waitForJustification(ServerPlayer player) {
        WAITING.put(player.getUUID(), Mode.JUSTIFICATION);
        player.sendSystemMessage(Component.literal(LanguageManager.tr("common.edit")));
        player.closeContainer();
    }

    public static boolean handleChat(ServerPlayer player, String message) {
        Mode mode = WAITING.remove(player.getUUID());
        if (mode == null) return false;

        PlotzStore.SaleDraft draft = PlotzStore.getDraft(player.getUUID());
        if (draft == null) {
            player.sendSystemMessage(Component.literal(LanguageManager.tr("common.cancel")));
            return true;
        }

        switch (mode) {
            case PRICE -> {
                try {
                    int price = Integer.parseInt(message.trim());
                    if (price <= 0) {
                        player.sendSystemMessage(Component.literal(LanguageManager.tr("common.cancel")));
                    } else {
                        PlotzStore.updateDraftPrice(player.getUUID(), price);
                        player.sendSystemMessage(Component.literal(LanguageManager.tr("common.confirm")));
                    }
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal(LanguageManager.tr("common.cancel")));
                }
            }
            case DESCRIPTION -> {
                PlotzStore.updateDraftDescription(player.getUUID(), message);
                player.sendSystemMessage(Component.literal(LanguageManager.tr("common.confirm")));
            }
            case BUILT -> {
                PlotzStore.updateDraftBuilt(player.getUUID(), message);
                player.sendSystemMessage(Component.literal(LanguageManager.tr("common.confirm")));
            }
            case JUSTIFICATION -> {
                PlotzStore.updateDraftJustification(player.getUUID(), message);
                player.sendSystemMessage(Component.literal(LanguageManager.tr("common.confirm")));
            }
        }

        PlotzCreateSaleMenu.open(player);
        return true;
    }
}