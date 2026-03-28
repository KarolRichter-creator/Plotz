package de.karol.plotz.menu;

import de.karol.plotz.data.OwnedPlotData;
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

public class PlotzMyPlotsMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer plotsContainer;

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzMyPlotsMenu(containerId, inventory, player),
            Component.literal("Mein Besitz")
        ));
    }

    public PlotzMyPlotsMenu(int containerId, Inventory playerInventory, ServerPlayer viewer) {
        this(containerId, playerInventory, viewer, new SimpleContainer(54));
    }

    private PlotzMyPlotsMenu(int containerId, Inventory playerInventory, ServerPlayer viewer, SimpleContainer container) {
        super(MenuType.GENERIC_9x6, containerId, playerInventory, container, 6);
        this.viewer = viewer;
        this.plotsContainer = container;
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < plotsContainer.getContainerSize(); i++) {
            plotsContainer.setItem(i, ItemStack.EMPTY);
        }

        List<OwnedPlotData.PlotEntry> plots = OwnedPlotData.getPlotsOf(viewer.getUUID());
        int slot = 10;
        for (OwnedPlotData.PlotEntry plot : plots) {
            if (slot % 9 == 8) slot++;
            if (slot >= 44) break;

            ItemStack map = new ItemStack(plot.capital() ? Items.FILLED_MAP : Items.MAP);
            map.set(DataComponents.CUSTOM_NAME, Component.literal(
                (plot.capital() ? "§6" : "§b") + plot.title()
                + " §7| " + plot.chunkCount() + " Chunks | " + plot.location()
            ));
            plotsContainer.setItem(slot, map);
            slot++;
        }

        ItemStack back = new ItemStack(Items.BARRIER);
        back.set(DataComponents.CUSTOM_NAME, Component.literal("§cZurück"));
        plotsContainer.setItem(49, back);

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == 49 && player instanceof ServerPlayer sp) {
            PlotzMainMenu.open(sp);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
