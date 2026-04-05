package de.karl_der_iii.economymc.service;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerModeConfirmManager {
    public enum PendingAction {
        AUTO_TAX_DISABLE,
        BUDGET_CHANGE
    }

    private static final String CONFIRM_TEXT = "ich bin der gefahr bewusst";
    private static final Map<UUID, PendingAction> PENDING = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> PENDING_BUDGET_VALUES = new ConcurrentHashMap<>();

    private ServerModeConfirmManager() {
    }

    public static void requestAutoTaxDisable(ServerPlayer player) {
        PENDING.put(player.getUUID(), PendingAction.AUTO_TAX_DISABLE);
        player.sendSystemMessage(Component.literal(LanguageManager.tr("server.confirm.type_phrase")));
        player.sendSystemMessage(Component.literal(LanguageManager.tr("server.confirm.admin_needed")));
        player.sendSystemMessage(Component.literal(LanguageManager.tr("server.confirm.type_phrase_exact") + CONFIRM_TEXT));
    }

    public static void requestBudgetChange(ServerPlayer player, long newBudget) {
        PENDING.put(player.getUUID(), PendingAction.BUDGET_CHANGE);
        PENDING_BUDGET_VALUES.put(player.getUUID(), newBudget);
        player.sendSystemMessage(Component.literal(LanguageManager.tr("server.budget.confirm")));
        player.sendSystemMessage(Component.literal(LanguageManager.tr("server.confirm.admin_needed")));
        player.sendSystemMessage(Component.literal(LanguageManager.tr("server.confirm.type_phrase_exact") + CONFIRM_TEXT));
    }

    public static boolean handleChat(ServerPlayer player, String message) {
        PendingAction action = PENDING.get(player.getUUID());
        if (action == null) {
            return false;
        }

        if (!message.trim().equalsIgnoreCase(CONFIRM_TEXT)) {
            player.sendSystemMessage(Component.literal(LanguageManager.tr("server.confirm.failed")));
            PENDING.remove(player.getUUID());
            PENDING_BUDGET_VALUES.remove(player.getUUID());
            return true;
        }

        if (action == PendingAction.AUTO_TAX_DISABLE) {
            if (!AdminSettingsManager.hasPendingAutoTaxDisableRequest()) {
                AdminSettingsManager.createPendingAutoTaxDisableRequest(player.getGameProfile().getName());
                player.sendSystemMessage(Component.literal(LanguageManager.tr("server.auto_tax.disable_request_sent")));
            } else {
                player.sendSystemMessage(Component.literal(LanguageManager.tr("server.auto_tax.disable_pending")));
            }
        } else if (action == PendingAction.BUDGET_CHANGE) {
            long value = PENDING_BUDGET_VALUES.getOrDefault(player.getUUID(), AdminSettingsManager.treasuryTargetBudget());
            AdminSettingsManager.createPendingBudgetChange(player.getGameProfile().getName(), value);
            player.sendSystemMessage(Component.literal(LanguageManager.tr("server.budget.request_sent")));
        }

        PENDING.remove(player.getUUID());
        PENDING_BUDGET_VALUES.remove(player.getUUID());
        return true;
    }

    public static boolean hasPending(ServerPlayer player) {
        return PENDING.containsKey(player.getUUID());
    }
}