package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.service.AdminSettingsManager;
import de.karl_der_iii.economymc.service.BalanceManager;
import de.karl_der_iii.economymc.service.DailyRewardManager;
import de.karl_der_iii.economymc.service.LanguageManager;
import de.karl_der_iii.economymc.service.ScoreboardManager;
import de.karl_der_iii.economymc.service.TransactionHistoryManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class PlotzMainMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzMainMenu(containerId, inventory, player),
            Component.literal(LanguageManager.tr("main.menu.title"))
        ));
    }

    public PlotzMainMenu(int containerId, Inventory inventory, ServerPlayer viewer) {
        this(containerId, inventory, viewer, new SimpleContainer(45));
    }

    private PlotzMainMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x5, containerId, inventory, box, 5);
        this.viewer = viewer;
        this.box = box;
        refresh();
    }

    private ItemStack sectionItem(boolean enabled, ItemStack enabledStack, String disabledTitle) {
        if (enabled) {
            return enabledStack;
        }
        return MenuUtil.named(Items.BARRIER, disabledTitle);
    }

    private void refresh() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        box.setItem(4, MenuUtil.named(Items.NETHER_STAR, LanguageManager.tr("main.menu.title")));

        box.setItem(10, MenuUtil.named(Items.MAP, LanguageManager.tr("main.plots")));

        box.setItem(12, sectionItem(
            AdminSettingsManager.shopEnabled(),
            MenuUtil.named(Items.CHEST, LanguageManager.tr("main.shop")),
            LanguageManager.tr("main.disabled.shop")
        ));

        box.setItem(14, sectionItem(
            AdminSettingsManager.jobsEnabled(),
            MenuUtil.named(Items.BOOK, LanguageManager.tr("main.jobs")),
            LanguageManager.tr("main.disabled.jobs")
        ));

        box.setItem(16, sectionItem(
            AdminSettingsManager.checksEnabled(),
            MenuUtil.named(Items.PAPER, LanguageManager.tr("main.checks")),
            LanguageManager.tr("main.disabled.checks")
        ));

        box.setItem(28, MenuUtil.named(Items.GOLD_INGOT, LanguageManager.tr("main.bank")));
        box.setItem(30, MenuUtil.named(Items.CLOCK, LanguageManager.tr("main.history")));

        int dailyReward = DailyRewardManager.getCurrentReward(viewer.getUUID());
        int streak = DailyRewardManager.getStreak(viewer.getUUID());

        if (AdminSettingsManager.dailyEnabled()) {
            box.setItem(32, MenuUtil.named(
                Items.EMERALD,
                LanguageManager.tr("main.daily"),
                List.of(
                    LanguageManager.format("main.daily.reward", dailyReward),
                    LanguageManager.format("main.daily.streak", streak),
                    LanguageManager.format("main.daily.rate", AdminSettingsManager.dailyIncreasePercent()),
                    LanguageManager.format("main.daily.max", AdminSettingsManager.dailyMaxReward())
                )
            ));
        } else {
            box.setItem(32, MenuUtil.named(
                Items.GRAY_DYE,
                LanguageManager.tr("main.daily") + " §7(" + LanguageManager.tr("admin.off") + ")",
                List.of(LanguageManager.tr("daily.disabled"))
            ));
        }

        box.setItem(34, MenuUtil.named(Items.SUNFLOWER, LanguageManager.tr("main.pay")));

        box.setItem(38, sectionItem(
            AdminSettingsManager.serverModeEnabled(),
            MenuUtil.named(Items.IRON_BARS, LanguageManager.tr("main.servermode")),
            LanguageManager.tr("main.disabled.servermode")
        ));

        box.setItem(40, MenuUtil.named(Items.REDSTONE_TORCH, LanguageManager.tr("main.adminmode")));
        box.setItem(44, MenuUtil.playerInfoHead(viewer));

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }

        switch (slotId) {
            case 10 -> PlotzPlotsHubMenu.open(sp);
            case 12 -> {
                if (AdminSettingsManager.shopEnabled()) {
                    PlotzShopMenu.open(sp);
                } else {
                    sp.sendSystemMessage(Component.literal(LanguageManager.tr("msg.shop_disabled")));
                }
            }
            case 14 -> {
                if (AdminSettingsManager.jobsEnabled()) {
                    PlotzJobsMenu.open(sp, 0, true, false);
                } else {
                    sp.sendSystemMessage(Component.literal(LanguageManager.tr("msg.jobs_disabled")));
                }
            }
            case 16 -> {
                if (AdminSettingsManager.checksEnabled()) {
                    PlotzChecksMenu.open(sp, 0);
                } else {
                    sp.sendSystemMessage(Component.literal(LanguageManager.tr("msg.checks_disabled")));
                }
            }
            case 28 -> PlotzBankMenu.open(sp);
            case 30 -> PlotzHistoryMenu.open(sp, false);
            case 32 -> {
                if (!AdminSettingsManager.dailyEnabled()) {
                    sp.sendSystemMessage(Component.literal(LanguageManager.tr("daily.disabled")));
                    return;
                }

                if (!DailyRewardManager.canClaim(sp.getUUID())) {
                    long remaining = DailyRewardManager.getRemainingMs(sp.getUUID()) / 1000L;
                    long hours = remaining / 3600L;
                    long minutes = (remaining % 3600L) / 60L;
                    sp.sendSystemMessage(Component.literal(LanguageManager.format("daily.already", hours, minutes)));
                    return;
                }

                int reward = DailyRewardManager.getCurrentReward(sp.getUUID());
                BalanceManager.addBalance(sp.getUUID(), reward);
                TransactionHistoryManager.add(sp.getUUID(), LanguageManager.format("history.daily", reward));
                ScoreboardManager.update(sp.server);
                DailyRewardManager.markClaimed(sp.getUUID());
                sp.sendSystemMessage(Component.literal(LanguageManager.tr("daily.claimed")));
                refresh();
            }
            case 34 -> PlotzPayMenu.open(sp);
            case 38 -> {
                if (AdminSettingsManager.serverModeEnabled()) {
                    PlotzServerModeMenu.open(sp);
                } else {
                    sp.sendSystemMessage(Component.literal(LanguageManager.tr("msg.servermode_disabled")));
                }
            }
            case 40 -> {
                if (sp.hasPermissions(2)) {
                    PlotzAdminModeMenu.open(sp);
                }
            }
            default -> {
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
