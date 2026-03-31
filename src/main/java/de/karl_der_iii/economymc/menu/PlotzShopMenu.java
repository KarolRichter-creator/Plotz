package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.data.PlotzStore;
import de.karl_der_iii.economymc.service.TreasuryManager;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlotzShopMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private final int page;
    private final Map<Integer, String> listingIdsBySlot = new HashMap<>();

    public static void open(ServerPlayer player) {
        open(player, 0);
    }

    public static void open(ServerPlayer player, int page) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzShopMenu(containerId, inventory, player, page),
            Component.literal("Shop")
        ));
    }

    public PlotzShopMenu(int containerId, Inventory inventory, ServerPlayer viewer, int page) {
        this(containerId, inventory, viewer, new SimpleContainer(54), page);
    }

    private PlotzShopMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box, int page) {
        super(MenuType.GENERIC_9x6, containerId, inventory, box, 6);
        this.viewer = viewer;
        this.box = box;
        this.page = page;
        refresh();
    }

    private ItemStack createDisplayItem(PlotzStore.ShopListing listing) {
        int base = listing.price();
        int tax = TreasuryManager.calculateTax(base);
        int total = TreasuryManager.calculateTotalWithTax(base);

        if (listing.items().size() == 1) {
            ItemStack stack = listing.items().get(0).copy();
            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal("§6Base Price: $" + base));
            lore.add(Component.literal("§cTax: $" + tax));
            lore.add(Component.literal("§aTotal: $" + total));
            lore.add(Component.literal("§7Seller: " + listing.sellerName()));
            lore.add(Component.literal("§7Amount: " + stack.getCount()));
            lore.add(Component.literal("§7Click to view"));
            stack.set(DataComponents.LORE, new ItemLore(lore));
            return stack;
        }

        ItemStack boxItem = new ItemStack(Items.SHULKER_BOX);
        boxItem.set(DataComponents.CUSTOM_NAME, Component.literal("§dShop Bundle"));

        Map<String, Integer> grouped = new LinkedHashMap<>();
        int totalCount = 0;

        for (ItemStack item : listing.items()) {
            String name = item.getHoverName().getString();
            grouped.put(name, grouped.getOrDefault(name, 0) + item.getCount());
            totalCount += item.getCount();
        }

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§6Base Price: $" + base));
        lore.add(Component.literal("§cTax: $" + tax));
        lore.add(Component.literal("§aTotal: $" + total));
        lore.add(Component.literal("§7Seller: " + listing.sellerName()));
        lore.add(Component.literal("§7Stacks inside: " + listing.items().size()));
        lore.add(Component.literal("§7Total items: " + totalCount));

        int shown = 0;
        for (Map.Entry<String, Integer> entry : grouped.entrySet()) {
            if (shown >= 5) {
                lore.add(Component.literal("§8...and more"));
                break;
            }
            lore.add(Component.literal("§f" + entry.getValue() + "x " + entry.getKey()));
            shown++;
        }

        lore.add(Component.literal("§7Click to view"));
        boxItem.set(DataComponents.LORE, new ItemLore(lore));
        return boxItem;
    }

    private void refresh() {
        listingIdsBySlot.clear();

        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        List<PlotzStore.ShopListing> listings = PlotzStore.getShopListings();
        int start = page * 45;
        int end = Math.min(start + 45, listings.size());

        int slot = 0;
        for (int i = start; i < end; i++) {
            if (slot >= 45) break;

            PlotzStore.ShopListing listing = listings.get(i);
            box.setItem(slot, createDisplayItem(listing));
            listingIdsBySlot.put(slot, listing.listingId());
            slot++;
        }

        box.setItem(45, MenuUtil.playerInfoHead(viewer));
        box.setItem(49, MenuUtil.named(Items.BARRIER, "§cClose"));
        box.setItem(50, MenuUtil.named(Items.ARROW, "§7Previous Page"));
        box.setItem(51, MenuUtil.named(Items.PAPER, "§7Page " + (page + 1)));
        box.setItem(52, MenuUtil.named(Items.ARROW, "§7Next Page"));
        box.setItem(53, MenuUtil.named(Items.EMERALD, "§aSell"));

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId == 49) {
            sp.closeContainer();
            return;
        }

        if (slotId == 50) {
            if (page > 0) {
                open(sp, page - 1);
            }
            return;
        }

        if (slotId == 52) {
            if ((page + 1) * 45 < PlotzStore.getShopListings().size()) {
                open(sp, page + 1);
            }
            return;
        }

        if (slotId == 53) {
            PlotzShopSellMenu.open(sp);
            return;
        }

        String listingId = listingIdsBySlot.get(slotId);
        if (listingId == null) return;

        PlotzShopListingDetailMenu.open(sp, listingId, page);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}