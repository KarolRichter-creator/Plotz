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
        this(containerId, inventory, viewer, new SimpleContainer(27));
    }

    private PlotzAdminModeMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x3, containerId, inventory, box, 3);
        this.viewer = viewer;
        this.box = box;
        refresh();
    }

    private ItemStack toggleItem(boolean enabled, String title) {
        return MenuUtil.named(enabled ? Items.LIME_DYE : Items.GRAY_DYE, (enabled ? "§a" : "§7") + title + ": " + (enabled ? "ON" : "OFF"));
    }

    private void refresh() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        box.setItem(10, toggleItem(AdminSettingsManager.jobsEnabled(), "Jobs"));
        box.setItem(11, toggleItem(AdminSettingsManager.checksEnabled(), "Checks"));
        box.setItem(12, toggleItem(AdminSettingsManager.shopEnabled(), "Shop"));
        box.setItem(13, toggleItem(AdminSettingsManager.plotMarketEnabled(), "Plot Market"));
        box.setItem(14, toggleItem(AdminSettingsManager.serverModeEnabled(), "Server Mode"));

        box.setItem(16, MenuUtil.named(Items.PAPER, "§7Min Tax: " + AdminSettingsManager.minTaxPercent() + "%"));
        box.setItem(17, MenuUtil.named(Items.PAPER, "§7Min Overdue: " + AdminSettingsManager.minOverduePercent() + "%"));
        box.setItem(18, MenuUtil.named(Items.PAPER, "§7Min Cancel: " + AdminSettingsManager.minCancelPercent() + "%"));

        box.setItem(20, MenuUtil.named(Items.GLOBE_BANNER_PATTERN, LanguageManager.currentLanguageLabel()));
        box.setItem(21, MenuUtil.named(Items.WRITABLE_BOOK, LanguageManager.tr("admin.language.toggle")));

        MenuUtil.putPlayerInfoHead(box, viewer, 22);
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

        if (slotId == 16) AdminSettingsManager.setMinTaxPercent((AdminSettingsManager.minTaxPercent() + 1) % 6);
        if (slotId == 17) AdminSettingsManager.setMinOverduePercent((AdminSettingsManager.minOverduePercent() + 1) % 6);
        if (slotId == 18) AdminSettingsManager.setMinCancelPercent((AdminSettingsManager.minCancelPercent() + 1) % 6);

        if (slotId == 20 || slotId == 21) {
            AdminSettingsManager.setLanguage(AdminSettingsManager.nextLanguage());
        }

        refresh();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}