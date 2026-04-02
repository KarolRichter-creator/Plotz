package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.data.PlotzStore;
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
        this(containerId, inventory, viewer, new SimpleContainer(54));
    }

    private PlotzMainMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x6, containerId, inventory, box, 6);
        this.viewer = viewer;
        this.box = box;
        refresh();
    }

    private void fillBackground() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }
    }

    private void refresh() {
        fillBackground();

        box.setItem(4, MenuUtil.named(Items.NETHER_STAR, LanguageManager.tr("main.menu.title")));

        box.setItem(10, MenuUtil.named(Items.MAP, LanguageManager.tr("main.plots")));
        box.setItem(11, MenuUtil.named(Items.CHEST, LanguageManager.tr("main.shop")));
        box.setItem(12, MenuUtil.named(Items.BOOK, LanguageManager.tr("main.jobs")));
        box.setItem(13, MenuUtil.named(Items.PAPER, LanguageManager.tr("main.checks")));
        box.setItem(14, MenuUtil.named(Items.GOLD_INGOT, LanguageManager.tr("main.bank")));
        box.setItem(15, MenuUtil.named(Items.CLOCK, LanguageManager.tr("main.history")));

        box.setItem(19, MenuUtil.named(
            Items.COMPASS,
            LanguageManager.tr("main.market") + " §7(" + PlotzStore.getListings().size() + ")"
        ));

        box.setItem(20, MenuUtil.named(
            Items.GRASS_BLOCK,
            LanguageManager.tr("main.myplots") + " §7(" + PlotzStore.getOwnedPlots(viewer.getUUID()).size() + ")"
        ));

        box.setItem(21, MenuUtil.named(
            Items.WRITABLE_BOOK,
            LanguageManager.tr("main.mysales") + " §7(" + PlotzStore.getListingsBySeller(viewer.getUUID()).size() + ")"
        ));

        box.setItem(22, MenuUtil.named(
            Items.EMERALD,
            LanguageManager.tr("main.daily")
        ));

        if (AdminSettingsManager.serverModeEnabled()) {
            box.setItem(24, MenuUtil.named(
                Items.IRON_BARS,
                LanguageManager.tr("main.servermode")
            ));
        } else {
            box.setItem(24, MenuUtil.named(
                Items.BARRIER,
                LanguageManager.tr("main.servermode.disabled")
            ));
        }

        box.setItem(31, MenuUtil.named(
            Items.REDSTONE_TORCH,
            LanguageManager.tr("main.adminmode")
        ));

        box.setItem(49, MenuUtil.named(
            Items.BARRIER,
            LanguageManager.tr("common.close")
        ));

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }

        switch (slotId) {
            case 10 -> PlotzPlotsHubMenu.open(sp);
            case 11 -> {
                if (AdminSettingsManager.shopEnabled()) {
                    PlotzShopMenu.open(sp);
                } else {
                    sp.sendSystemMessage(Component.literal(LanguageManager.tr("msg.shop_disabled")));
                }
            }
            case 12 -> {
                if (AdminSettingsManager.jobsEnabled()) {
                    PlotzJobsMenu.open(sp, 0, true, false);
                } else {
                    sp.sendSystemMessage(Component.literal(LanguageManager.tr("msg.jobs_disabled")));
                }
            }
            case 13 -> {
                if (AdminSettingsManager.checksEnabled()) {
                    PlotzChecksMenu.open(sp, 0);
                } else {
                    sp.sendSystemMessage(Component.literal(LanguageManager.tr("msg.checks_disabled")));
                }
            }
            case 14 -> PlotzBankMenu.open(sp);
            case 15 -> PlotzHistoryMenu.open(sp, false);
            case 19 -> PlotzMarketMenu.open(sp);
            case 20 -> PlotzMyPlotsMenu.open(sp);
            case 21 -> PlotzMySalesMenu.open(sp);
            case 22 -> {
                if (!DailyRewardManager.canClaim(sp.getUUID())) {
                    long remaining = DailyRewardManager.getRemainingMs(sp.getUUID()) / 1000L;
                    long hours = remaining / 3600L;
                    long minutes = (remaining % 3600L) / 60L;
                    sp.sendSystemMessage(Component.literal(LanguageManager.format("daily.already", hours, minutes)));
                    return;
                }

                BalanceManager.addBalance(sp.getUUID(), 100);
                TransactionHistoryManager.add(sp.getUUID(), LanguageManager.format("history.daily", 100));
                ScoreboardManager.update(sp.server);
                DailyRewardManager.markClaimed(sp.getUUID());
                sp.sendSystemMessage(Component.literal(LanguageManager.tr("daily.claimed")));
                refresh();
            }
            case 24 -> {
                if (AdminSettingsManager.serverModeEnabled()) {
                    PlotzServerModeMenu.open(sp);
                } else {
                    sp.sendSystemMessage(Component.literal(LanguageManager.tr("msg.servermode_disabled")));
                }
            }
            case 31 -> {
                if (sp.hasPermissions(2)) {
                    PlotzAdminModeMenu.open(sp);
                }
            }
            case 49 -> sp.closeContainer();
            default -> {
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}