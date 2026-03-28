package de.karol.plotz.menu;

import de.karol.plotz.data.PlotzStore;
import de.karol.plotz.service.PlotzLogic;
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

public class PlotzListingDetailMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private final String listingId;
    private final boolean backToMySales;

    public static void open(ServerPlayer player, String listingId, boolean backToMySales) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzListingDetailMenu(containerId, inventory, player, listingId, backToMySales),
            Component.literal("Grundstücksdetails")
        ));
    }

    public PlotzListingDetailMenu(int containerId, Inventory inventory, ServerPlayer viewer, String listingId, boolean backToMySales) {
        this(containerId, inventory, viewer, new SimpleContainer(27), listingId, backToMySales);
    }

    private PlotzListingDetailMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box, String listingId, boolean backToMySales) {
        super(MenuType.GENERIC_9x3, containerId, inventory, box, 3);
        this.viewer = viewer;
        this.box = box;
        this.listingId = listingId;
        this.backToMySales = backToMySales;
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, ItemStack.EMPTY);
        }

        PlotzStore.Listing listing = PlotzStore.getListingById(listingId);
        if (listing == null) {
            box.setItem(13, MenuUtil.named(Items.BARRIER, "§cAngebot nicht mehr vorhanden"));
            box.setItem(22, MenuUtil.named(Items.BARRIER, "§cZurück"));
            broadcastChanges();
            return;
        }

        box.setItem(10, MenuUtil.named(
            listing.capital() ? Items.ENCHANTED_BOOK : Items.BOOK,
            (listing.capital() ? "§6" : "§e") + listing.title()
        ));
        box.setItem(12, MenuUtil.named(Items.GOLD_INGOT, "§6Preis: " + listing.price() + "$"));
        box.setItem(13, MenuUtil.named(Items.PAPER, "§7Beschreibung: " + listing.description()));
        box.setItem(14, MenuUtil.named(Items.COMPASS, "§bLage: " + listing.location()));
        box.setItem(15, MenuUtil.named(Items.BRICKS, "§7Bebauung: " + listing.builtOnPlot()));
        box.setItem(16, MenuUtil.named(Items.NAME_TAG, "§7Preisbegründung: " + listing.justification()));
        box.setItem(21, MenuUtil.named(Items.MAP, "§bChunks: " + listing.chunkCount()));
        box.setItem(22, MenuUtil.named(Items.BARRIER, "§cZurück"));
        box.setItem(23, MenuUtil.named(Items.LIME_CONCRETE, "§aKaufen"));

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }

        if (slotId == 22) {
            if (backToMySales) {
                PlotzMySalesMenu.open(sp);
            } else {
                PlotzMarketMenu.open(sp);
            }
            return;
        }

        if (slotId != 23) {
            return;
        }

        PlotzStore.Listing listing = PlotzStore.getListingById(listingId);
        if (listing == null) {
            sp.sendSystemMessage(Component.literal("§cAngebot existiert nicht mehr."));
            if (backToMySales) {
                PlotzMySalesMenu.open(sp);
            } else {
                PlotzMarketMenu.open(sp);
            }
            return;
        }

        if (listing.sellerId().equals(sp.getUUID())) {
            sp.sendSystemMessage(Component.literal("§cDu kannst dein eigenes Grundstück nicht kaufen."));
            return;
        }

        if (!PlotzLogic.tryCharge(sp, listing.price())) {
            sp.sendSystemMessage(Component.literal("§cNot enough money."));
            return;
        }

        PlotzLogic.paySeller(sp, listing.sellerName(), listing.price());

        PlotzStore.addOwnedPlot(new PlotzStore.PlotEntry(
            sp.getUUID(),
            sp.getGameProfile().getName(),
            listing.title(),
            listing.capital(),
            listing.chunkCount(),
            listing.location(),
            listing.description()
        ));

        PlotzStore.removeListing(listing.listingId());
        sp.sendSystemMessage(Component.literal("§aDu hast das Grundstück gekauft: " + listing.title()));
        PlotzMyPlotsMenu.open(sp);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}