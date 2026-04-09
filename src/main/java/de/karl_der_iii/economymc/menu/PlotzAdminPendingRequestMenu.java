cat > src/main/java/de/karl_der_iii/economymc/menu/PlotzAdminPendingRequestMenu.java <<'EOF'
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

import java.util.ArrayList;
import java.util.List;

public class PlotzAdminPendingRequestMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzAdminPendingRequestMenu(containerId, inventory, player),
            Component.literal(LanguageManager.tr("admin.pending.title"))
        ));
    }

    public PlotzAdminPendingRequestMenu(int containerId, Inventory inventory, ServerPlayer viewer) {
        this(containerId, inventory, viewer, new SimpleContainer(27));
    }

    private PlotzAdminPendingRequestMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x3, containerId, inventory, box, 3);
        this.viewer = viewer;
        this.box = box;
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        List<String> lore = new ArrayList<>();
        String title;

        if (AdminSettingsManager.hasPendingBudgetChange()) {
            title = LanguageManager.tr("admin.pending.budget");
            lore.add(LanguageManager.tr("admin.pending.from") + AdminSettingsManager.pendingBudgetChangeRequester());
            lore.add(LanguageManager.tr("server.target_budget") + AdminSettingsManager.pendingBudgetValue());
        } else if (AdminSettingsManager.hasPendingAutoTaxDisableRequest()) {
            title = LanguageManager.tr("admin.pending.auto_tax");
            lore.add(LanguageManager.tr("admin.pending.from") + AdminSettingsManager.pendingAutoTaxDisableRequester());
        } else {
            title = LanguageManager.tr("admin.pending.none");
            lore.add(LanguageManager.tr("admin.pending.none.desc"));
        }

        box.setItem(13, MenuUtil.named(Items.ORANGE_CONCRETE, title, lore));
        box.setItem(11, MenuUtil.named(Items.LIME_CONCRETE, LanguageManager.tr("admin.approve")));
        box.setItem(15, MenuUtil.named(Items.RED_CONCRETE, LanguageManager.tr("admin.deny")));
        box.setItem(22, MenuUtil.named(Items.BARRIER, LanguageManager.tr("common.back")));
        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId == 11) {
            if (AdminSettingsManager.hasPendingBudgetChange()) {
                AdminSettingsManager.approvePendingBudgetChange();
            } else if (AdminSettingsManager.hasPendingAutoTaxDisableRequest()) {
                AdminSettingsManager.approvePendingAutoTaxDisableRequest();
            }
            PlotzAdminModeMenu.open(sp);
            return;
        }

        if (slotId == 15) {
            if (AdminSettingsManager.hasPendingBudgetChange()) {
                AdminSettingsManager.denyPendingBudgetChange();
            } else if (AdminSettingsManager.hasPendingAutoTaxDisableRequest()) {
                AdminSettingsManager.denyPendingAutoTaxDisableRequest();
            }
            PlotzAdminModeMenu.open(sp);
            return;
        }

        if (slotId == 22) {
            PlotzAdminModeMenu.open(sp);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
EOF