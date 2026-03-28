package de.karol.plotz.menu;

import de.karol.plotz.data.PlotzStore;
import de.karol.plotz.service.OpacBridge;
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

import java.util.UUID;

public class PlotzMainMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private long lastClickMs = 0L;

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzMainMenu(containerId, inventory, player),
            Component.literal("Plotz")
        ));
    }

    public PlotzMainMenu(int containerId, Inventory inventory, ServerPlayer viewer) {
        this(containerId, inventory, viewer, new SimpleContainer(27));
    }

    private PlotzMainMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x3, containerId, inventory, box, 3);
        this.viewer = viewer;
        this.box = box;
        refresh();
    }

    private void fillBackground() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }
    }

    private void refresh() {
        fillBackground();

        UUID id = viewer.getUUID();
        boolean capitalHere = PlotzLogic.isCapital(viewer.blockPosition());

        box.setItem(4, MenuUtil.named(
            Items.COMPASS,
            capitalHere ? "§6Current Position: Capital Zone" : "§7Current Position: Normal Zone"
        ));

        box.setItem(10, MenuUtil.named(
            Items.BOOK,
            "§eBuy Normal Claim Credits §7(" + PlotzStore.getNormalCredits(id) + " | " + PlotzLogic.NORMAL_CHUNK_PRICE + "$)"
        ));

        box.setItem(12, MenuUtil.named(
            Items.ENCHANTED_BOOK,
            "§6Buy Capital Claim Credits §7(" + PlotzStore.getCapitalCredits(id) + " | " + PlotzLogic.CAPITAL_CHUNK_PRICE + "$)"
        ));

        box.setItem(13, MenuUtil.named(
            Items.NAME_TAG,
            OpacBridge.getPartyStatusText(viewer)
        ));

        box.setItem(14, MenuUtil.named(
            Items.EMERALD,
            "§aCreate Sale Listing"
        ));

        box.setItem(16, MenuUtil.named(
            Items.CHEST,
            "§3Market Listings §7(" + PlotzStore.getListings().size() + ")"
        ));

        box.setItem(19, MenuUtil.named(
            Items.MAP,
            "§bMy Plots §7(" + PlotzStore.getOwnedPlots(id).size() + ")"
        ));

        box.setItem(22, MenuUtil.named(
            Items.WRITABLE_BOOK,
            "§dMy Sales §7(" + PlotzStore.getListingsBySeller(id).size() + ")"
        ));

        box.setItem(25, MenuUtil.named(
            Items.PAPER,
            "§7Claim credits currently require an OPAC party"
        ));

        broadcastChanges();
    }

    private boolean clickAllowed() {
        long now = System.currentTimeMillis();
        if (now - lastClickMs < 250L) {
            return false;
        }
        lastClickMs = now;
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }

        if (!clickAllowed()) {
            return;
        }

        if (slotId == 10) {
            if (!OpacBridge.isInstalled()) {
                sp.sendSystemMessage(Component.literal("§cOpen Parties and Claims is not installed."));
                return;
            }

            if (!PlotzLogic.hasRequiredParty(sp)) {
                sp.sendSystemMessage(Component.literal("§cYou need to create or join an Open Parties and Claims party first."));
                return;
            }

            boolean charged = PlotzLogic.canBuyNormalCredit(sp);
            if (!charged) {
                sp.sendSystemMessage(Component.literal("§cYou do not have enough money for a normal claim credit."));
                return;
            }

            PlotzStore.addNormalCredit(sp.getUUID(), 1);
            sp.sendSystemMessage(Component.literal("§aBought 1 normal claim credit."));
            refresh();
            return;
        }

        if (slotId == 12) {
            if (!OpacBridge.isInstalled()) {
                sp.sendSystemMessage(Component.literal("§cOpen Parties and Claims is not installed."));
                return;
            }

            if (!PlotzLogic.hasRequiredParty(sp)) {
                sp.sendSystemMessage(Component.literal("§cYou need to create or join an Open Parties and Claims party first."));
                return;
            }

            boolean charged = PlotzLogic.canBuyCapitalCredit(sp);
            if (!charged) {
                sp.sendSystemMessage(Component.literal("§cYou do not have enough money for a capital claim credit."));
                return;
            }

            PlotzStore.addCapitalCredit(sp.getUUID(), 1);
            sp.sendSystemMessage(Component.literal("§aBought 1 capital claim credit."));
            refresh();
            return;
        }

        if (slotId == 14) {
            PlotzCreateSaleMenu.open(sp);
            return;
        }

        if (slotId == 16) {
            PlotzMarketMenu.open(sp);
            return;
        }

        if (slotId == 19) {
            PlotzMyPlotsMenu.open(sp);
            return;
        }

        if (slotId == 22) {
            PlotzMySalesMenu.open(sp);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}