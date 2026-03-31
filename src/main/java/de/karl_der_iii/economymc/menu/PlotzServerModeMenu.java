package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.service.AdminSettingsManager;
import de.karl_der_iii.economymc.service.JobsInputManager;
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
            Component.literal("EC Server Mode")
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

    private void fillBackground() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }
    }

    private void refresh() {
        fillBackground();

        box.setItem(4, MenuUtil.named(Items.GOLD_BLOCK, "§6Treasury: $" + TreasuryManager.getTreasury()));

        box.setItem(10, MenuUtil.named(Items.RED_CONCRETE, "§cTax -1%"));
        box.setItem(11, MenuUtil.named(
            AdminSettingsManager.autoTaxEnabled() ? Items.COMPASS : Items.PAPER,
            "§7Tax Rate: " + TreasuryManager.getTaxPercent() + "% " +
                (AdminSettingsManager.autoTaxEnabled() ? "§8(Auto)" : "§8(Manual)")
        ));
        box.setItem(12, MenuUtil.named(Items.LIME_CONCRETE, "§aTax +1%"));
        box.setItem(13, MenuUtil.named(
            AdminSettingsManager.autoTaxEnabled() ? Items.LIME_DYE : Items.GRAY_DYE,
            (AdminSettingsManager.autoTaxEnabled() ? "§a" : "§7") + "Auto Tax: " +
                (AdminSettingsManager.autoTaxEnabled() ? "ON" : "OFF")
        ));

        box.setItem(14, MenuUtil.named(Items.RED_CONCRETE, "§cOverdue -1%"));
        box.setItem(15, MenuUtil.named(Items.PAPER, "§7Overdue Penalty: " + TreasuryManager.getOverduePenaltyPercent() + "%"));
        box.setItem(16, MenuUtil.named(Items.LIME_CONCRETE, "§aOverdue +1%"));

        box.setItem(28, MenuUtil.named(Items.RED_CONCRETE, "§cCancel -1%"));
        box.setItem(29, MenuUtil.named(Items.PAPER, "§7Cancel Penalty: " + TreasuryManager.getCancelPenaltyPercent() + "%"));
        box.setItem(30, MenuUtil.named(Items.LIME_CONCRETE, "§aCancel +1%"));

        box.setItem(32, MenuUtil.named(Items.RED_CONCRETE, "§cDays -1"));
        box.setItem(33, MenuUtil.named(Items.CLOCK, "§7Max Overdue Days: " + TreasuryManager.getMaxOverdueDays()));
        box.setItem(34, MenuUtil.named(Items.LIME_CONCRETE, "§aDays +1"));

        box.setItem(37, MenuUtil.named(Items.RED_CONCRETE, "§cStart Hour -1"));
        box.setItem(38, MenuUtil.named(Items.CLOCK, "§7Job Open Hour: " + AdminSettingsManager.jobAcceptHour() + ":00"));
        box.setItem(39, MenuUtil.named(Items.LIME_CONCRETE, "§aStart Hour +1"));

        box.setItem(40, MenuUtil.named(Items.EMERALD, "§aCreate Server Job"));
        box.setItem(42, MenuUtil.named(Items.BOOK, "§bOpen Server Jobs"));
        box.setItem(44, MenuUtil.named(Items.MAP, "§7Treasury, jobs and taxes"));

        MenuUtil.putPlayerInfoHead(box, viewer, 45);
        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId == 10 && !AdminSettingsManager.autoTaxEnabled()) {
            TreasuryManager.setTaxPercent(TreasuryManager.getManualTaxPercent() - 1);
        }
        if (slotId == 12 && !AdminSettingsManager.autoTaxEnabled()) {
            TreasuryManager.setTaxPercent(TreasuryManager.getManualTaxPercent() + 1);
        }
        if (slotId == 13) {
            AdminSettingsManager.setAutoTaxEnabled(!AdminSettingsManager.autoTaxEnabled());
        }

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

        refresh();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}