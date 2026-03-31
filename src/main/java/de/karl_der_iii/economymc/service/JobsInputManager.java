package de.karl_der_iii.economymc.service;

import de.karl_der_iii.economymc.menu.PlotzJobsMenu;
import de.karl_der_iii.economymc.menu.PlotzServerModeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JobsInputManager {
    private enum Stage {
        TITLE,
        DESCRIPTION,
        REWARD,
        DUE_DAYS
    }

    private record Draft(boolean serverJob, Stage stage, String title, String description, int reward) {}

    private static final Map<UUID, Draft> DRAFTS = new ConcurrentHashMap<>();

    private JobsInputManager() {}

    public static void startPlayerJob(ServerPlayer player) {
        DRAFTS.put(player.getUUID(), new Draft(false, Stage.TITLE, "", "", 0));
        player.sendSystemMessage(Component.literal("§eEnter the job title in chat now."));
        player.closeContainer();
    }

    public static void startServerJob(ServerPlayer player) {
        DRAFTS.put(player.getUUID(), new Draft(true, Stage.TITLE, "", "", 0));
        player.sendSystemMessage(Component.literal("§eEnter the server job title in chat now."));
        player.closeContainer();
    }

    public static boolean handleChat(ServerPlayer player, String message) {
        Draft draft = DRAFTS.get(player.getUUID());
        if (draft == null) {
            return false;
        }

        switch (draft.stage()) {
            case TITLE -> {
                DRAFTS.put(player.getUUID(), new Draft(draft.serverJob(), Stage.DESCRIPTION, message, "", 0));
                player.sendSystemMessage(Component.literal("§eEnter the job description in chat now."));
                return true;
            }
            case DESCRIPTION -> {
                DRAFTS.put(player.getUUID(), new Draft(draft.serverJob(), Stage.REWARD, draft.title(), message, 0));
                player.sendSystemMessage(Component.literal("§eEnter the reward amount in chat now."));
                return true;
            }
            case REWARD -> {
                int reward;
                try {
                    reward = Integer.parseInt(message.trim());
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("§cThat is not a valid number."));
                    return true;
                }

                if (reward <= 0) {
                    player.sendSystemMessage(Component.literal("§cReward must be above 0."));
                    return true;
                }

                DRAFTS.put(player.getUUID(), new Draft(draft.serverJob(), Stage.DUE_DAYS, draft.title(), draft.description(), reward));
                player.sendSystemMessage(Component.literal("§eEnter the due time in full days now."));
                return true;
            }
            case DUE_DAYS -> {
                int dueDays;
                try {
                    dueDays = Integer.parseInt(message.trim());
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("§cThat is not a valid number."));
                    return true;
                }

                if (dueDays <= 0) {
                    player.sendSystemMessage(Component.literal("§cDue days must be above 0."));
                    return true;
                }

                DRAFTS.remove(player.getUUID());

                if (draft.serverJob()) {
                    if (!TreasuryManager.removeTreasury(draft.reward())) {
                        player.sendSystemMessage(Component.literal("§cThe treasury does not have enough money for this server job."));
                        PlotzServerModeMenu.open(player);
                        return true;
                    }
                } else {
                    if (!BalanceManager.removeBalance(player.getUUID(), draft.reward())) {
                        player.sendSystemMessage(Component.literal("§cYou do not have enough money to create this job."));
                        PlotzJobsMenu.open(player, 0, true, false);
                        return true;
                    }
                }

                JobManager.createJob(
                    draft.serverJob() ? null : player.getUUID(),
                    draft.serverJob() ? "Server" : player.getGameProfile().getName(),
                    draft.title(),
                    draft.description(),
                    draft.reward(),
                    dueDays,
                    draft.serverJob()
                );

                player.sendSystemMessage(Component.literal("§aJob created."));
                if (draft.serverJob()) {
                    PlotzServerModeMenu.open(player);
                } else {
                    PlotzJobsMenu.open(player, 0, true, false);
                }
                return true;
            }
        }

        return false;
    }
}