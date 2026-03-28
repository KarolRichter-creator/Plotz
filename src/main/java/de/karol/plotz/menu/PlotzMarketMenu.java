package de.karol.plotz.menu;

import de.karol.plotz.data.MarketListingData;
import de.karol.plotz.data.OwnedPlotData;
import de.karol.plotz.service.EconomyService;
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

import java.util.List;

public class PlotzMarketMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer marketContainer;
    private final int[] listingIndexBySlot = new int[54];

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzMarketMenu(containerId, inventory, player),
            Component.literal("Plotz Markt")
        ));
    }

    public PlotzMarketMenu(int containerId, Inventory playerInventory, ServerPlayer viewer) {
        this(containerId, playerInventory, viewer, new SimpleContainer(54));
    }

    private PlotzMarketMenu(int containerId, Inventory playerInventory, ServerPlayer viewer, SimpleContainer container) {
        super(MenuType.GENERIC_9x6, containerId, playerInventory, container, 6);
        this.viewer = viewer;
        this.marketContainer = container;
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < marketContainer.getContainerSize(); i++) {
            marketContainer.setItem(i, ItemStack.EMPTY);
            listingIndexBySlot[i] = -1;
        }

        List<MarketListingData.Listing> listings = MarketListingData.getListings();
        int slot = 10;
        for (int i = 0; i < listings.size() && slot < 44; i++) {
            if (slot % 9 == 8) slot++;
            if (slot >= 44) break;

            MarketListingData.Listing listing = listings.get(i);
            ItemStack book = new ItemStack(listing.capital() ? Items.ENCHANTED_BOOK : Items.BOOK);
            book.set(DataComponents.CUSTOM_NAME, Component.literal(
                (listing.capital() ? "§6" : "§e") + listing.title()
                + " §7| " + listing.price() + "$ | " + listing.chunkCount() + " Chunks"
            ));
            marketContainer.setItem(slot, book);
            listingIndexBySlot[slot] = i;
            slot++;
        }

        ItemStack back = new ItemStack(Items.BARRIER);
        back.set(DataComponents.CUSTOM_NAME, Component.literal("§cZurück"));
        marketContainer.setItem(49, back);

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId == 49) {
            PlotzMainMenu.open(sp);
            return;
        }

        int listingIndex = (slotId >= 0 && slotId < listingIndexBySlot.length) ? listingIndexBySlot[slotId] : -1;
        if (listingIndex >= 0) {
            MarketListingData.Listing listing = MarketListingData.getByIndex(listingIndex);
            if (listing == null) {
                refresh();
                return;
            }

            if (EconomyService.tryCharge(sp, listing.price())) {
                EconomyService.paySeller(listing.sellerName(), listing.price());

                OwnedPlotData.addPlot(new OwnedPlotData.PlotEntry(
                    sp.getUUID(),
                    listing.title(),
                    listing.capital(),
                    listing.chunkCount(),
                    listing.location(),
                    listing.description()
                ));

                MarketListingData.removeByIndex(listingIndex);
                sp.sendSystemMessage(Component.literal("§aDu hast das Grundstück gekauft: " + listing.title()));
                refresh();
            } else {
                sp.sendSystemMessage(Component.literal("§cNicht genug Geld."));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
