package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.data.PlotzStore;
import de.karl_der_iii.economymc.service.OpacBridge;
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

public class PlotzMyPlotsMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private final Map<Integer, PlotzStore.PlotEntry> plotBySlot = new HashMap<>();
    private long lastClickMs = 0L;

    public static void open(ServerPlayer player) {
        OpacBridge.syncOwnedClaims(player);

        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzMyPlotsMenu(containerId, inventory, player),
            Component.literal("My Plots")
        ));
    }

    public PlotzMyPlotsMenu(int containerId, Inventory inventory, ServerPlayer viewer) {
        this(containerId, inventory, viewer, new SimpleContainer(54));
    }

    private PlotzMyPlotsMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x6, containerId, inventory, box, 6);
        this.viewer = viewer;
        this.box = box;
        refresh();
    }

    private boolean clickAllowed() {
        long now = System.currentTimeMillis();
        if (now - lastClickMs < 250L) {
            return false;
        }
        lastClickMs = now;
        return true;
    }

    private void refresh() {
        plotBySlot.clear();

        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        List<PlotzStore.PlotEntry> plots = PlotzStore.getOwnedPlots(viewer.getUUID());
        int slot = 10;

        for (PlotzStore.PlotEntry plot : plots) {
            if (slot % 9 == 8) {
                slot++;
            }
            if (slot >= 44) {
                break;
            }

            box.setItem(slot, MenuUtil.named(
                plot.capital() ? Items.FILLED_MAP : Items.MAP,
                (plot.capital() ? "§6" : "§b")
                    + plot.title()
                    + " §7| " + plot.chunkCount() + " Chunks | " + plot.location()
            ));

            plotBySlot.put(slot, plot);
            slot++;
        }

        box.setItem(47, MenuUtil.named(Items.PAPER, "§7Click a plot to create a sale draft"));
        box.setItem(49, MenuUtil.named(Items.BARRIER, "§cBack"));
        MenuUtil.putPlayerInfoHead(box, viewer, 45);
        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }

        if (!clickAllowed()) {
            return;
        }

        if (slotId == 49) {
            PlotzMainMenu.open(sp);
            return;
        }

        PlotzStore.PlotEntry plot = plotBySlot.get(slotId);
        if (plot == null) {
            return;
        }

        if (PlotzStore.hasListingForLocation(plot.location())) {
            sp.sendSystemMessage(Component.literal("§cThis plot is already listed in the market."));
            return;
        }

        if (PlotzStore.hasAnyDraftForLocation(plot.location())) {
            sp.sendSystemMessage(Component.literal("§cThis plot already has a sale draft."));
            return;
        }

        PlotzStore.setDraft(new PlotzStore.SaleDraft(
            sp.getUUID(),
            sp.getGameProfile().getName(),
            plot.title(),
            plot.capital(),
            plot.chunkCount(),
            plot.location(),
            plot.capital() ? 10000 : 5000,
            "Edit later",
            "Edit later",
            "Edit later",
            false
        ));

        sp.sendSystemMessage(Component.literal("§aSale draft selected: " + plot.title()));
        PlotzCreateSaleMenu.open(sp);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}