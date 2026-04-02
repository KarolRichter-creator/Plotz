package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.service.AdminSettingsManager;
import de.karl_der_iii.economymc.service.LanguageManager;
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

public class PlotzAdminModeMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzAdminModeMenu(containerId, inventory, player),
            Component.literal(LanguageManager.tr("admin.mode.title"))
        ));
    }

    public PlotzAdminModeMenu(int containerId, Inventory inventory, ServerPlayer viewer) {
        this(containerId, inventory, viewer, new SimpleContainer(36));
    }

    private PlotzAdminModeMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x4, containerId, inventory, box, 4);
        this.viewer = viewer;
        this.box = box;
        refresh();
    }

    private ItemStack toggleItem(boolean enabled, String title) {
        return MenuUtil.named(
            enabled ? Items.LIME_DYE : Items.GRAY_DYE,
            (enabled ? "§a" : "§7") + title + ": " + (enabled ? LanguageManager.tr("admin.on") : LanguageManager.tr("admin.off"))
        );
    }

    private void refresh() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        box.setItem(4, MenuUtil.named(Items.REDSTONE_TORCH, LanguageManager.tr("admin.mode.title")));

        box.setItem(10, toggleItem(AdminSettingsManager.jobsEnabled(), LanguageManager.tr("admin.jobs")));
        box.setItem(11, toggleItem(AdminSettingsManager.checksEnabled(), LanguageManager.tr("admin.checks")));
        box.setItem(12, toggleItem(AdminSettingsManager.shopEnabled(), LanguageManager.tr("admin.shop")));
        box.setItem(13, toggleItem(AdminSettingsManager.plotMarketEnabled(), LanguageManager.tr("admin.plot_market")));
        box.setItem(14, toggleItem(AdminSettingsManager.serverModeEnabled(), LanguageManager.tr("admin.server_mode")));

        box.setItem(19, MenuUtil.named(
            Items.PAPER,
            LanguageManager.tr("admin.min_tax") + ": " + AdminSettingsManager.minTaxPercent() + "%"
        ));
        box.setItem(20, MenuUtil.named(
            Items.PAPER,
            LanguageManager.tr("admin.min_overdue") + ": " + AdminSettingsManager.minOverduePercent() + "%"
        ));
        box.setItem(21, MenuUtil.named(
            Items.PAPER,
            LanguageManager.tr("admin.min_cancel") + ": " + AdminSettingsManager.minCancelPercent() + "%"
        ));

        box.setItem(23, MenuUtil.named(Items.GLOBE_BANNER_PATTERN, LanguageManager.currentLanguageLabel()));
        box.setItem(24, MenuUtil.named(Items.WRITABLE_BOOK, LanguageManager.tr("admin.language.toggle")));

        box.setItem(31, MenuUtil.playerInfoHead(viewer));
        box.setItem(35, MenuUtil.named(Items.BARRIER, LanguageManager.tr("common.back")));

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId == 10) AdminSettingsManager.setJobsEnabled(!AdminSettingsManager.jobsEnabled());
        if (slotId == 11) AdminSettingsManager.setChecksEnabled(!AdminSettingsManager.checksEnabled());
        if (slotId == 12) AdminSettingsManager.setShopEnabled(!AdminSettingsManager.shopEnabled());
        if (slotId == 13) AdminSettingsManager.setPlotMarketEnabled(!AdminSettingsManager.plotMarketEnabled());
        if (slotId == 14) AdminSettingsManager.setServerModeEnabled(!AdminSettingsManager.serverModeEnabled());

        if (slotId == 19) AdminSettingsManager.setMinTaxPercent(AdminSettingsManager.minTaxPercent() + 1);
        if (slotId == 20) AdminSettingsManager.setMinOverduePercent(AdminSettingsManager.minOverduePercent() + 1);
        if (slotId == 21) AdminSettingsManager.setMinCancelPercent(AdminSettingsManager.minCancelPercent() + 1);

        if (slotId == 23 || slotId == 24) {
            AdminSettingsManager.setLanguage(AdminSettingsManager.nextLanguage());
        }

        if (slotId == 35) {
            PlotzMainMenu.open(sp);
            return;
        }

        refresh();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}