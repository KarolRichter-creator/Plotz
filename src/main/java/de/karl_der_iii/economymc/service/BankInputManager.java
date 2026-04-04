package de.karl_der_iii.economymc.service;

import de.karl_der_iii.economymc.menu.PlotzBankMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class BankInputManager {
    private enum Stage {
        PLAYER_NAME,
        AMOUNT,
        DAYS
    }

    private record Draft(LoanManager.LoanTargetType targetType, Stage stage, String targetName, UUID targetPlayerId, int amount) {
        Draft withPlayer(String newName, UUID newId) {
            return new Draft(targetType, Stage.AMOUNT, newName, newId, amount);
        }

        Draft withAmount(int newAmount) {
            return new Draft(targetType, Stage.DAYS, targetName, targetPlayerId, newAmount);
        }
    }

    private static final Map<UUID, Draft> DRAFTS = new HashMap<>();

    private BankInputManager() {
    }

    public static void startServerRequest(ServerPlayer player) {
        DRAFTS.put(player.getUUID(), new Draft(LoanManager.LoanTargetType.SERVER, Stage.AMOUNT, "", null, 0));
        player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.input.amount")));
    }

    public static void startAllRequest(ServerPlayer player) {
        DRAFTS.put(player.getUUID(), new Draft(LoanManager.LoanTargetType.ALL_PLAYERS, Stage.AMOUNT, "", null, 0));
        player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.input.amount")));
    }

    public static void startPlayerRequest(ServerPlayer player) {
        DRAFTS.put(player.getUUID(), new Draft(LoanManager.LoanTargetType.SPECIFIC_PLAYER, Stage.PLAYER_NAME, "", null, 0));
        player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.input.target_name")));
    }

    public static boolean handleChat(ServerPlayer player, String message) {
        Draft draft = DRAFTS.get(player.getUUID());
        if (draft == null) {
            return false;
        }

        String input = message.trim();
        if (input.equalsIgnoreCase("cancel")) {
            DRAFTS.remove(player.getUUID());
            player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.input.cancelled")));
            PlotzBankMenu.open(player);
            return true;
        }

        switch (draft.stage()) {
            case PLAYER_NAME -> handlePlayerName(player, input, draft);
            case AMOUNT -> handleAmount(player, input, draft);
            case DAYS -> handleDays(player, input, draft);
        }
        return true;
    }

    private static void handlePlayerName(ServerPlayer player, String input, Draft draft) {
        Optional<UUID> targetOpt = BalanceManager.resolveKnownPlayer(player.server, input);
        if (targetOpt.isEmpty()) {
            player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.invalid_target")));
            return;
        }

        DRAFTS.put(player.getUUID(), draft.withPlayer(input, targetOpt.get()));
        player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.input.amount")));
    }

    private static void handleAmount(ServerPlayer player, String input, Draft draft) {
        int amount;
        try {
            amount = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.input.invalid_number")));
            return;
        }

        if (amount <= 0) {
            player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.input.amount_positive")));
            return;
        }

        DRAFTS.put(player.getUUID(), draft.withAmount(amount));
        player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.input.days")));
    }

    private static void handleDays(ServerPlayer player, String input, Draft draft) {
        int days;
        try {
            days = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.input.invalid_number")));
            return;
        }

        if (days <= 0) {
            player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.input.days_positive")));
            return;
        }

        LoanManager.createRequest(
            player.getUUID(),
            player.getGameProfile().getName(),
            draft.targetType(),
            draft.targetPlayerId(),
            draft.targetName(),
            draft.amount(),
            days
        );

        DRAFTS.remove(player.getUUID());
        player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.request.created")));
        PlotzBankMenu.open(player);
    }
}