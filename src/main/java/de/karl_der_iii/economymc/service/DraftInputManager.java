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
        player.sendSystemMessage(Component.literal(LanguageManager.tr("draft.input.price")));
        player.closeContainer();
    }

    public static void waitForDescription(ServerPlayer player) {
        WAITING.put(player.getUUID(), Mode.DESCRIPTION);
        player.sendSystemMessage(Component.literal(LanguageManager.tr("draft.input.description")));
        player.closeContainer();
    }

    public static void waitForBuilt(ServerPlayer player) {
        WAITING.put(player.getUUID(), Mode.BUILT);
        player.sendSystemMessage(Component.literal(LanguageManager.tr("draft.input.built")));
        player.closeContainer();
    }

    public static void waitForJustification(ServerPlayer player) {
        WAITING.put(player.getUUID(), Mode.JUSTIFICATION);
        player.sendSystemMessage(Component.literal(LanguageManager.tr("draft.input.justification")));
        player.closeContainer();
    }

    public static boolean handleChat(ServerPlayer player, String message) {
        Mode mode = WAITING.remove(player.getUUID());
        if (mode == null) return false;

        PlotzStore.SaleDraft draft = PlotzStore.getDraft(player.getUUID());
        if (draft == null) {
            player.sendSystemMessage(Component.literal(LanguageManager.tr("draft.input.no_draft")));
            return true;
        }

        switch (mode) {
            case PRICE -> {
                try {
                    int price = Integer.parseInt(message.trim());
                    if (price <= 0) {
                        player.sendSystemMessage(Component.literal(LanguageManager.tr("draft.input.price_positive")));
                    } else {
                        PlotzStore.updateDraftPrice(player.getUUID(), price);
                        player.sendSystemMessage(Component.literal(LanguageManager.format("draft.input.price_set", price)));
                    }
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal(LanguageManager.tr("draft.input.invalid_number")));
                }
            }
            case DESCRIPTION -> {
                PlotzStore.updateDraftDescription(player.getUUID(), message);
                player.sendSystemMessage(Component.literal(LanguageManager.tr("draft.input.description_set")));
            }
            case BUILT -> {
                PlotzStore.updateDraftBuilt(player.getUUID(), message);
                player.sendSystemMessage(Component.literal(LanguageManager.tr("draft.input.built_set")));
            }
            case JUSTIFICATION -> {
                PlotzStore.updateDraftJustification(player.getUUID(), message);
                player.sendSystemMessage(Component.literal(LanguageManager.tr("draft.input.justification_set")));
            }
        }

        PlotzCreateSaleMenu.open(player);
        return true;
    }
}