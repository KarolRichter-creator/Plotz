package de.karol.plotz.menu;

import de.karol.plotz.data.PlotzStore;
import de.karol.plotz.service.BalanceManager;
import de.karol.plotz.service.ScoreboardManager;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlotzShopListingDetailMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private final String listingId;
    private final int returnPage;

    public static void open(ServerPlayer player, String listingId, int returnPage) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzShopListingDetailMenu(containerId, inventory, player, listingId, returnPage),
            Component.literal("Shop Listing")
        ));
    }

    public PlotzShopListingDetailMenu(int containerId, Inventory inventory, ServerPlayer viewer, String listingId, int returnPage) {
        this(containerId, inventory, viewer, new SimpleContainer(27), listingId, returnPage);
    }

    private PlotzShopListingDetailMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box, String listingId, int returnPage) {
        super(MenuType.GENERIC_9x3, containerId, inventory, box, 3);
        this.viewer = viewer;
        this.box = box;
        this.listingId = listingId;
        this.returnPage = returnPage;
        refresh();
    }

    private ItemStack createPreview(PlotzStore.ShopListing listing) {
        if (listing.items().size() == 1) {
            ItemStack stack = listing.items().get(0).copy();
            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal("§6Price: $" + listing.price()));
            lore.add(Component.literal("§7Seller: " + listing.sellerName()));
            lore.add(Component.literal("§7Amount: " + stack.getCount()));
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
        lore.add(Component.literal("§6Price: $" + listing.price()));
        lore.add(Component.literal("§7Seller: " + listing.sellerName()));
        lore.add(Component.literal("§7Stacks inside: " + listing.items().size()));
        lore.add(Component.literal("§7Total items: " + totalCount));

        for (Map.Entry<String, Integer> entry : grouped.entrySet()) {
            lore.add(Component.literal("§f" + entry.getValue() + "x " + entry.getKey()));
        }

        boxItem.set(DataComponents.LORE, new ItemLore(lore));
        return boxItem;
    }

    private void refresh() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        PlotzStore.ShopListing listing = PlotzStore.getShopListingById(listingId);
        if (listing == null) {
            box.setItem(13, MenuUtil.named(Items.BARRIER, "§cListing no longer exists"));
            box.setItem(21, MenuUtil.named(Items.BARRIER, "§cBack"));
            MenuUtil.putPlayerInfoHead(box, viewer, 18);
            broadcastChanges();
            return;
        }

        box.setItem(13, createPreview(listing));
        box.setItem(21, MenuUtil.named(Items.BARRIER, "§cBack"));

        if (listing.sellerId().equals(viewer.getUUID())) {
            box.setItem(23, MenuUtil.named(Items.RED_CONCRETE, "§cWithdraw Listing"));
        } else {
            box.setItem(23, MenuUtil.named(Items.LIME_CONCRETE, "§aBuy"));
        }

        MenuUtil.putPlayerInfoHead(box, viewer, 18);
        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId == 21) {
            PlotzShopMenu.open(sp, returnPage);
            return;
        }

        if (slotId != 23) {
            return;
        }

        PlotzStore.ShopListing listing = PlotzStore.getShopListingById(listingId);
        if (listing == null) {
            sp.sendSystemMessage(Component.literal("§cListing no longer exists."));
            PlotzShopMenu.open(sp, returnPage);
            return;
        }

        if (listing.sellerId().equals(sp.getUUID())) {
            for (ItemStack stack : listing.items()) {
                if (!sp.getInventory().add(stack.copy())) {
                    sp.drop(stack.copy(), false);
                }
            }

            PlotzStore.removeShopListing(listingId);
            sp.sendSystemMessage(Component.literal("§aListing withdrawn."));
            PlotzShopMenu.open(sp, returnPage);
            return;
        }

        if (!BalanceManager.removeBalance(sp.getUUID(), listing.price())) {
            sp.sendSystemMessage(Component.literal("§cYou do not have enough money."));
            return;
        }

        BalanceManager.addBalance(listing.sellerId(), listing.price());
        ScoreboardManager.update(sp.server);

        for (ItemStack stack : listing.items()) {
            if (!sp.getInventory().add(stack.copy())) {
                sp.drop(stack.copy(), false);
            }
        }

        PlotzStore.removeShopListing(listingId);
        sp.sendSystemMessage(Component.literal("§aItem(s) bought for $" + listing.price()));
        PlotzShopMenu.open(sp, returnPage);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}