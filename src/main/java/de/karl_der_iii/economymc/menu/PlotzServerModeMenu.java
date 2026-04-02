package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.service.AdminSettingsManager;
import de.karl_der_iii.economymc.service.JobsInputManager;
import de.karl_der_iii.economymc.service.LanguageManager;
import de.karl_der_iii.economymc.service.TreasuryManager;
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

public class PlotzServerModeMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzServerModeMenu(containerId, inventory, player),
            Component.literal(LanguageManager.tr("server.mode.title"))
        ));
    }

    public PlotzServerModeMenu(int containerId, Inventory inventory, ServerPlayer viewer) {
        this(containerId, inventory, viewer, new SimpleContainer(54));
    }

    private PlotzServerModeMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x6, containerId, inventory, box, 6);
        this.viewer = viewer;
        this.box = box;
        refresh();
    }

    private ItemStack stateItem(boolean enabled, String title) {
        return MenuUtil.named(
            enabled ? Items.LIME_DYE : Items.GRAY_DYE,
            (enabled ? "§a" : "§7") + title + ": " + (enabled ? LanguageManager.tr("admin.on") : LanguageManager.tr("admin.off"))
        );
    }

    private void refresh() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        box.setItem(4, MenuUtil.named(Items.IRON_BARS, LanguageManager.tr("server.mode.title")));

        box.setItem(10, MenuUtil.named(Items.RED_CONCRETE, LanguageManager.tr("server.tax_minus")));
        box.setItem(11, MenuUtil.named(Items.PAPER, LanguageManager.tr("server.tax_rate") + TreasuryManager.getTaxPercent() + "%"));
        box.setItem(12, MenuUtil.named(Items.LIME_CONCRETE, LanguageManager.tr("server.tax_plus")));
        box.setItem(13, stateItem(AdminSettingsManager.autoTaxEnabled(), LanguageManager.tr("server.auto_tax")));

        box.setItem(14, MenuUtil.named(Items.RED_CONCRETE, LanguageManager.tr("server.overdue_minus")));
        box.setItem(15, MenuUtil.named(Items.PAPER, LanguageManager.tr("server.overdue_penalty") + TreasuryManager.getOverduePenaltyPercent() + "%"));
        box.setItem(16, MenuUtil.named(Items.LIME_CONCRETE, LanguageManager.tr("server.overdue_plus")));

        box.setItem(28, MenuUtil.named(Items.RED_CONCRETE, LanguageManager.tr("server.cancel_minus")));
        box.setItem(29, MenuUtil.named(Items.PAPER, LanguageManager.tr("server.cancel_penalty") + TreasuryManager.getCancelPenaltyPercent() + "%"));
        box.setItem(30, MenuUtil.named(Items.LIME_CONCRETE, LanguageManager.tr("server.cancel_plus")));

        box.setItem(32, MenuUtil.named(Items.RED_CONCRETE, LanguageManager.tr("server.days_minus")));
        box.setItem(33, MenuUtil.named(Items.CLOCK, LanguageManager.tr("server.max_overdue_days") + TreasuryManager.getMaxOverdueDays()));
        box.setItem(34, MenuUtil.named(Items.LIME_CONCRETE, LanguageManager.tr("server.days_plus")));

        box.setItem(37, MenuUtil.named(Items.RED_CONCRETE, LanguageManager.tr("server.start_hour_minus")));
        box.setItem(38, MenuUtil.named(Items.CLOCK, LanguageManager.tr("server.job_open_hour") + AdminSettingsManager.jobAcceptHour() + ":00"));
        box.setItem(39, MenuUtil.named(Items.LIME_CONCRETE, LanguageManager.tr("server.start_hour_plus")));

        box.setItem(40, MenuUtil.named(Items.EMERALD, LanguageManager.tr("server.create_job")));
        box.setItem(42, MenuUtil.named(Items.BOOK, LanguageManager.tr("server.open_jobs")));
        box.setItem(44, MenuUtil.named(Items.GOLD_BLOCK, LanguageManager.tr("server.treasury_balance") + TreasuryManager.getTreasury()));

        box.setItem(45, MenuUtil.playerInfoHead(viewer));
        box.setItem(49, MenuUtil.named(Items.BARRIER, LanguageManager.tr("common.back")));
        box.setItem(53, MenuUtil.named(Items.CLOCK, LanguageManager.tr("history.treasury")));

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId == 49) {
            PlotzMainMenu.open(sp);
            return;
        }

        if (slotId == 10 && !AdminSettingsManager.autoTaxEnabled()) TreasuryManager.setTaxPercent(TreasuryManager.getManualTaxPercent() - 1);
        if (slotId == 12 && !AdminSettingsManager.autoTaxEnabled()) TreasuryManager.setTaxPercent(TreasuryManager.getManualTaxPercent() + 1);
        if (slotId == 13) AdminSettingsManager.setAutoTaxEnabled(!AdminSettingsManager.autoTaxEnabled());

        if (slotId == 14) TreasuryManager.setOverduePenaltyPercent(TreasuryManager.getOverduePenaltyPercent() - 1);
        if (slotId == 16) TreasuryManager.setOverduePenaltyPercent(TreasuryManager.getOverduePenaltyPercent() + 1);

        if (slotId == 28) TreasuryManager.setCancelPenaltyPercent(TreasuryManager.getCancelPenaltyPercent() - 1);
        if (slotId == 30) TreasuryManager.setCancelPenaltyPercent(TreasuryManager.getCancelPenaltyPercent() + 1);

        if (slotId == 32) TreasuryManager.setMaxOverdueDays(TreasuryManager.getMaxOverdueDays() - 1);
        if (slotId == 34) TreasuryManager.setMaxOverdueDays(TreasuryManager.getMaxOverdueDays() + 1);

        if (slotId == 37) AdminSettingsManager.setJobAcceptHour(AdminSettingsManager.jobAcceptHour() - 1);
        if (slotId == 39) AdminSettingsManager.setJobAcceptHour(AdminSettingsManager.jobAcceptHour() + 1);

        if (slotId == 40) {
            JobsInputManager.startServerJob(sp);
            return;
        }

        if (slotId == 42) {
            PlotzJobsMenu.open(sp, 0, false, true);
            return;
        }

        if (slotId == 53) {
            PlotzHistoryMenu.open(sp, true);
            return;
        }

        refresh();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}