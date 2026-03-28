package de.karol.plotz.menu;

import de.karol.plotz.config.PlotzConfig;
import de.karol.plotz.data.MarketListingData;
import de.karol.plotz.data.OwnedPlotData;
import de.karol.plotz.data.PlayerCreditsData;
import de.karol.plotz.data.SaleDraftData;
import de.karol.plotz.service.EconomyService;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.SimpleMenuProvider;

public class PlotzMainMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer plotzContainer;

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzMainMenu(containerId, inventory, player),
            Component.literal("Plotz")
        ));
    }

    public PlotzMainMenu(int containerId, Inventory playerInventory, ServerPlayer viewer) {
        this(containerId, playerInventory, viewer, new SimpleContainer(27));
    }

    private PlotzMainMenu(int containerId, Inventory playerInventory, ServerPlayer viewer, SimpleContainer container) {
        super(MenuType.GENERIC_9x3, containerId, playerInventory, container, 3);
        this.viewer = viewer;
        this.plotzContainer = container;
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < plotzContainer.getContainerSize(); i++) {
            plotzContainer.setItem(i, ItemStack.EMPTY);
        }

        plotzContainer.setItem(11, named(Items.BOOK,
            "§eNormale Claim-Chunks kaufen",
            "Normal: " + PlayerCreditsData.getNormalCredits(viewer.getUUID()) + " | Preis: " + PlotzConfig.NORMAL_CHUNK_PRICE + "$"));

        plotzContainer.setItem(13, named(Items.ENCHANTED_BOOK,
            "§6Hauptstadt-Claim-Chunks kaufen",
            "Hauptstadt: " + PlayerCreditsData.getCapitalCredits(viewer.getUUID()) + " | Preis: " + PlotzConfig.CAPITAL_CHUNK_PRICE + "$"));

        plotzContainer.setItem(15, named(Items.EMERALD,
            "§aNeuen Verkauf anlegen",
            "Erstellt aktuell einen einfachen Verkaufsentwurf"));

        plotzContainer.setItem(20, named(Items.MAP,
            "§bMein Besitz",
            "Grundstücke: " + OwnedPlotData.getPlotsOf(viewer.getUUID()).size()));

        plotzContainer.setItem(22, named(Items.WRITABLE_BOOK,
            "§dMeine Verkäufe",
            "Angebote: " + MarketListingData.getBySeller(viewer.getUUID()).size()));

        plotzContainer.setItem(24, named(Items.CHEST,
            "§3Marktangebote",
            "Anzahl: " + MarketListingData.getListings().size()));

        broadcastChanges();
    }

    private ItemStack named(net.minecraft.world.item.Item item, String title, String sub) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(title + " §7(" + sub + ")"));
        return stack;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId == 11) {
            if (EconomyService.tryCharge(sp, PlotzConfig.NORMAL_CHUNK_PRICE)) {
                PlayerCreditsData.addNormalCredits(sp.getUUID(), 1);
                sp.sendSystemMessage(Component.literal("§a1 normaler Claim-Chunk gekauft."));
                refresh();
            } else {
                sp.sendSystemMessage(Component.literal("§cNicht genug Geld."));
            }
            return;
        }

        if (slotId == 13) {
            if (EconomyService.tryCharge(sp, PlotzConfig.CAPITAL_CHUNK_PRICE)) {
                PlayerCreditsData.addCapitalCredits(sp.getUUID(), 1);
                sp.sendSystemMessage(Component.literal("§a1 Hauptstadt-Claim-Chunk gekauft."));
                refresh();
            } else {
                sp.sendSystemMessage(Component.literal("§cNicht genug Geld."));
            }
            return;
        }

        if (slotId == 15) {
            SaleDraftData.addDraft(new SaleDraftData.SaleDraft(
                sp.getUUID(),
                "Grundstück von " + sp.getGameProfile().getName(),
                5000,
                "Aktuelle Position: X=" + sp.blockPosition().getX() + " Z=" + sp.blockPosition().getZ(),
                "Einfacher Verkaufsentwurf"
            ));
            sp.sendSystemMessage(Component.literal("§aEin einfacher Verkaufsentwurf wurde erstellt."));
            return;
        }

        if (slotId == 20) {
            PlotzMyPlotsMenu.open(sp);
            return;
        }

        if (slotId == 22) {
            sp.sendSystemMessage(Component.literal("§dEigene Verkäufe: " + MarketListingData.getBySeller(sp.getUUID()).size()));
            return;
        }

        if (slotId == 24) {
            PlotzMarketMenu.open(sp);
            return;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
