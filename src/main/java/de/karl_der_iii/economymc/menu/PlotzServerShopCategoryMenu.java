package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.service.AdminSettingsManager;
import de.karl_der_iii.economymc.service.LanguageManager;
import de.karl_der_iii.economymc.service.ServerShopManager;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlotzServerShopCategoryMenu extends ChestMenu {
    private static final int PAGE_SIZE = 28;
    private static final int[] DISPLAY_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private final ServerShopManager.Category category;
    private final int page;
    private final Map<Integer, ServerShopManager.Entry> entriesBySlot = new HashMap<>();

    public static void open(ServerPlayer player, ServerShopManager.Category category, int page) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzServerShopCategoryMenu(containerId, inventory, player, category, page),
            Component.literal(LanguageManager.tr(category.translationKey()))
        ));
    }

    public PlotzServerShopCategoryMenu(int containerId, Inventory inventory, ServerPlayer viewer, ServerShopManager.Category category, int page) {
        this(containerId, inventory, viewer, new SimpleContainer(54), category, page);
    }

    private PlotzServerShopCategoryMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box, ServerShopManager.Category category, int page) {
        super(MenuType.GENERIC_9x6, containerId, inventory, box, 6);
        this.viewer = viewer;
        this.box = box;
        this.category = category;
        this.page = Math.max(0, page);
        refresh();
    }

    private ItemStack entryStack(ServerShopManager.Entry entry) {
        ItemStack stack = entry.item().getDefaultInstance().copy();
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(ServerShopManager.displayName(entry)));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(LanguageManager.tr("server.shop.base_price") + entry.basePrice()));
        lore.add(Component.literal(LanguageManager.tr("server.shop.tax") + ServerShopManager.tax(entry)));
        lore.add(Component.literal(LanguageManager.tr("server.shop.total_price") + ServerShopManager.total(entry)));
        lore.add(Component.literal(LanguageManager.tr("server.shop.click_buy")));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        return stack;
    }

    private void refresh() {
        entriesBySlot.clear();

        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        List<ServerShopManager.Entry> entries = ServerShopManager.getEntries(category);
        int start = page * PAGE_SIZE;
        int end = Math.min(entries.size(), start + PAGE_SIZE);
        int maxPage = Math.max(0, (entries.size() - 1) / PAGE_SIZE);

        box.setItem(4, MenuUtil.named(category.icon(), LanguageManager.tr(category.translationKey())));
        box.setItem(45, MenuUtil.playerInfoHead(viewer));
        box.setItem(48, MenuUtil.named(Items.ARROW, LanguageManager.tr("common.previous")));
        box.setItem(49, MenuUtil.named(Items.BARRIER, LanguageManager.tr("common.back")));
        box.setItem(50, MenuUtil.named(Items.ARROW, LanguageManager.tr("common.next")));
        box.setItem(53, MenuUtil.named(
            Items.PAPER,
            LanguageManager.tr("server.shop.page") + " " + (page + 1) + "/" + (maxPage + 1)
        ));

        int idx = 0;
        for (int i = start; i < end && idx < DISPLAY_SLOTS.length; i++, idx++) {
            int slot = DISPLAY_SLOTS[idx];
            ServerShopManager.Entry entry = entries.get(i);
            box.setItem(slot, entryStack(entry));
            entriesBySlot.put(slot, entry);
        }

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

        List<ServerShopManager.Entry> entries = ServerShopManager.getEntries(category);
        int maxPage = Math.max(0, (entries.size() - 1) / PAGE_SIZE);

        if (slotId == 48 && page > 0) {
            open(sp, category, page - 1);
            return;
        }

        if (slotId == 49) {
            PlotzServerShopMenu.open(sp);
            return;
        }

        if (slotId == 50 && page < maxPage) {
            open(sp, category, page + 1);
            return;
        }

        ServerShopManager.Entry entry = entriesBySlot.get(slotId);
        if (entry == null) return;

        if (!ServerShopManager.buy(sp, entry)) {
            sp.sendSystemMessage(Component.literal(LanguageManager.tr("server.shop.not_enough_money")));
            return;
        }

        sp.sendSystemMessage(Component.literal(LanguageManager.format(
            "server.shop.bought",
            ServerShopManager.displayName(entry),
            ServerShopManager.total(entry)
        )));
        refresh();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}