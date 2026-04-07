package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.service.AdminSettingsManager;
import de.karl_der_iii.economymc.service.LanguageManager;
import de.karl_der_iii.economymc.service.ServerShopManager;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlotzServerShopMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private final Map<Integer, ServerShopManager.Category> categoriesBySlot = new HashMap<>();

    private static final int[] CATEGORY_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        31
    };

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzServerShopMenu(containerId, inventory, player),
            Component.literal(LanguageManager.tr("server.shop.menu.title"))
        ));
    }

    public PlotzServerShopMenu(int containerId, Inventory inventory, ServerPlayer viewer) {
        this(containerId, inventory, viewer, new SimpleContainer(54));
    }

    private PlotzServerShopMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x6, containerId, inventory, box, 6);
        this.viewer = viewer;
        this.box = box;
        refresh();
    }

    private void refresh() {
        categoriesBySlot.clear();

        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        box.setItem(4, MenuUtil.named(Items.GOLD_BLOCK, LanguageManager.tr("server.shop.menu.title")));

        List<ServerShopManager.Category> categories = ServerShopManager.categories();
        for (int i = 0; i < categories.size() && i < CATEGORY_SLOTS.length; i++) {
            ServerShopManager.Category category = categories.get(i);
            int slot = CATEGORY_SLOTS[i];
            box.setItem(slot, MenuUtil.named(category.icon(), LanguageManager.tr(category.translationKey())));
            categoriesBySlot.put(slot, category);
        }

        box.setItem(45, MenuUtil.playerInfoHead(viewer));
        box.setItem(49, MenuUtil.named(Items.BARRIER, LanguageManager.tr("common.back")));
        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (!AdminSettingsManager.serverShopEnabled()) {
            sp.sendSystemMessage(Component.literal(LanguageManager.tr("msg.server_shop_disabled")));
            PlotzMainMenu.open(sp);
            return;
        }

        if (slotId == 49) {
            PlotzMainMenu.open(sp);
            return;
        }

        ServerShopManager.Category category = categoriesBySlot.get(slotId);
        if (category != null) {
            PlotzServerShopCategoryMenu.open(sp, category, 0);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}