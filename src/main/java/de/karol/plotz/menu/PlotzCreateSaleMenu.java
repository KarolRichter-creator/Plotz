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

public class PlotzCreateSaleMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private long lastClickMs = 0L;

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzCreateSaleMenu(containerId, inventory, player),
            Component.literal("Create Sale Listing")
        ));
    }

    public PlotzCreateSaleMenu(int containerId, Inventory inventory, ServerPlayer viewer) {
        this(containerId, inventory, viewer, new SimpleContainer(27));
    }

    private PlotzCreateSaleMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
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

        boolean capital = PlotzLogic.isCapital(viewer.blockPosition());

        box.setItem(4, MenuUtil.named(
            Items.COMPASS,
            capital ? "§6Capital Plot" : "§7Normal Plot"
        ));

        box.setItem(10, MenuUtil.named(
            Items.GOLD_INGOT,
            "§eFixed Price - 5,000$"
        ));

        box.setItem(12, MenuUtil.named(
            Items.GOLD_BLOCK,
            "§6Fixed Price - 10,000$"
        ));

        box.setItem(14, MenuUtil.named(
            Items.EMERALD,
            "§aNegotiable - 8,000$"
        ));

        box.setItem(16, MenuUtil.named(
            Items.EMERALD_BLOCK,
            "§2Negotiable - 15,000$"
        ));

        box.setItem(22, MenuUtil.named(
            Items.BARRIER,
            "§cBack"
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

    private void createListing(ServerPlayer sp, int price, boolean negotiable) {
        boolean capital = PlotzLogic.isCapital(sp.blockPosition());

        PlotzStore.addListing(new PlotzStore.Listing(
            java.util.UUID.randomUUID().toString(),
            sp.getUUID(),
            sp.getGameProfile().getName(),
            "Plot by " + sp.getGameProfile().getName(),
            price,
            capital,
            1,
            "X=" + sp.blockPosition().getX() + " Z=" + sp.blockPosition().getZ(),
            capital ? "Plot inside the capital" : "Plot outside the capital",
            negotiable ? "Negotiable offer" : "Fixed price offer",
            "No detailed building value yet",
            negotiable
        ));

        sp.sendSystemMessage(Component.literal(
            negotiable
                ? "§aCreated negotiable listing for " + price + "$."
                : "§aCreated fixed price listing for " + price + "$."
        ));

        PlotzMainMenu.open(sp);
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
            createListing(sp, 5000, false);
            return;
        }

        if (slotId == 12) {
            createListing(sp, 10000, false);
            return;
        }

        if (slotId == 14) {
            createListing(sp, 8000, true);
            return;
        }

        if (slotId == 16) {
            createListing(sp, 15000, true);
            return;
        }

        if (slotId == 22) {
            PlotzMainMenu.open(sp);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}