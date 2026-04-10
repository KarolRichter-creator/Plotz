package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.data.PlotzStore;
import de.karl_der_iii.economymc.service.LanguageManager;
import de.karl_der_iii.economymc.service.PlotzLogic;
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
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.UUID;

public class PlotzPlotsHubMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzPlotsHubMenu(containerId, inventory, player),
            Component.literal(LanguageManager.tr("plots.menu.title"))
        ));
    }

    public PlotzPlotsHubMenu(int containerId, Inventory inventory, ServerPlayer viewer) {
        this(containerId, inventory, viewer, new SimpleContainer(45));
    }

    private PlotzPlotsHubMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x5, containerId, inventory, box, 5);
        this.viewer = viewer;
        this.box = box;
        refresh();
    }

    private ItemStack connectorItem() {
        boolean connected = ModList.get().isLoaded("openpartiesandclaims");
        return MenuUtil.named(
            connected ? Items.LIME_DYE : Items.RED_DYE,
            LanguageManager.tr("plots.connector"),
            List.of(connected ? "§a●" : "§c●")
        );
    }

    private void refresh() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        UUID id = viewer.getUUID();
        boolean capitalHere = PlotzLogic.isCapital(viewer.blockPosition());

        box.setItem(22, connectorItem());

        box.setItem(13, MenuUtil.named(
            Items.BOOK,
            LanguageManager.tr("plots.buy.normal") + " §7(" + PlotzStore.getNormalCredits(id) + " | " + PlotzLogic.NORMAL_CHUNK_PRICE + "$)"
        ));
        box.setItem(21, MenuUtil.named(
            Items.MAP,
            LanguageManager.tr("plots.mine") + " §7(" + PlotzStore.getOwnedPlots(id).size() + ")"
        ));
        box.setItem(23, MenuUtil.named(
            Items.ENCHANTED_BOOK,
            LanguageManager.tr("plots.buy.capital") + " §7(" + PlotzStore.getCapitalCredits(id) + " | " + PlotzLogic.CAPITAL_CHUNK_PRICE + "$)"
        ));
        box.setItem(31, MenuUtil.named(
            Items.COMPASS,
            capitalHere ? LanguageManager.tr("plots.position.capital") : LanguageManager.tr("plots.position.normal")
        ));

        box.setItem(11, MenuUtil.named(
            Items.CHEST,
            LanguageManager.tr("plots.market") + " §7(" + PlotzStore.getListings().size() + ")"
        ));
        box.setItem(15, MenuUtil.named(
            Items.WRITABLE_BOOK,
            LanguageManager.tr("plots.sales") + " §7(" + PlotzStore.getListingsBySeller(id).size() + ")"
        ));

        box.setItem(29, MenuUtil.named(
            Items.EMERALD,
            LanguageManager.tr("plots.create.sale")
        ));
        box.setItem(33, MenuUtil.playerInfoHead(viewer));

        box.setItem(40, MenuUtil.named(Items.BARRIER, LanguageManager.tr("common.back")));

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }

        if (slotId == 13) {
            if (!PlotzLogic.canBuyNormalCredit(sp)) {
                sp.sendSystemMessage(Component.literal(LanguageManager.tr("plots.buy.normal.fail")));
                return;
            }
            PlotzStore.addNormalCredit(sp.getUUID(), 1);
            sp.sendSystemMessage(Component.literal(LanguageManager.tr("plots.buy.normal.ok")));
            refresh();
            return;
        }

        if (slotId == 23) {
            if (!PlotzLogic.canBuyCapitalCredit(sp)) {
                sp.sendSystemMessage(Component.literal(LanguageManager.tr("plots.buy.capital.fail")));
                return;
            }
            PlotzStore.addCapitalCredit(sp.getUUID(), 1);
            sp.sendSystemMessage(Component.literal(LanguageManager.tr("plots.buy.capital.ok")));
            refresh();
            return;
        }

        if (slotId == 21) {
            PlotzMyPlotsMenu.open(sp);
            return;
        }

        if (slotId == 11) {
            PlotzMarketMenu.open(sp);
            return;
        }

        if (slotId == 15) {
            PlotzMySalesMenu.open(sp);
            return;
        }

        if (slotId == 29) {
            PlotzCreateSaleMenu.open(sp);
            return;
        }

        if (slotId == 40) {
            PlotzMainMenu.open(sp);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}