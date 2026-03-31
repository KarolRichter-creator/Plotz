package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.data.PlotzStore;
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

public class PlotzMarketMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private final Map<Integer, String> listingIdsBySlot = new HashMap<>();

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzMarketMenu(containerId, inventory, player),
            Component.literal("Plotz Market")
        ));
    }

    public PlotzMarketMenu(int containerId, Inventory inventory, ServerPlayer viewer) {
        this(containerId, inventory, viewer, new SimpleContainer(54));
    }

    private PlotzMarketMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x6, containerId, inventory, box, 6);
        this.viewer = viewer;
        this.box = box;
        refresh();
    }

    private void refresh() {
        listingIdsBySlot.clear();

        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        List<PlotzStore.Listing> listings = PlotzStore.getListings();
        int slot = 10;

        for (PlotzStore.Listing listing : listings) {
            if (slot % 9 == 8) {
                slot++;
            }
            if (slot >= 44) {
                break;
            }

            box.setItem(slot, MenuUtil.named(
                listing.capital() ? Items.ENCHANTED_BOOK : Items.BOOK,
                (listing.capital() ? "§6" : "§e")
                    + listing.title()
                    + " §7| " + listing.price() + "$ | " + listing.chunkCount() + " Chunks"
            ));
            listingIdsBySlot.put(slot, listing.listingId());
            slot++;
        }

        box.setItem(49, MenuUtil.named(Items.BARRIER, "§cBack"));
        MenuUtil.putPlayerInfoHead(box, viewer, 45);
        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }

        if (slotId == 49) {
            PlotzMainMenu.open(sp);
            return;
        }

        String listingId = listingIdsBySlot.get(slotId);
        if (listingId != null) {
            PlotzListingDetailMenu.open(sp, listingId, false);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}