package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.data.PlotzStore;
import de.karl_der_iii.economymc.service.LanguageManager;
import de.karl_der_iii.economymc.service.ShopInputManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotzShopSellMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private boolean published = false;

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzShopSellMenu(containerId, inventory, player),
            Component.literal(LanguageManager.tr("shop.sell.title"))
        ));
    }

    public PlotzShopSellMenu(int containerId, Inventory inventory, ServerPlayer viewer) {
        this(containerId, inventory, viewer, new SimpleContainer(54));
    }

    private PlotzShopSellMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x6, containerId, inventory, box, 6);
        this.viewer = viewer;
        this.box = box;
        restoreDraftItems();
        refresh();
    }

    private void restoreDraftItems() {
        PlotzStore.ShopDraft draft = PlotzStore.getShopDraft(viewer.getUUID());
        if (draft == null) return;

        int slot = 0;
        for (ItemStack stack : draft.items()) {
            if (slot >= 45) break;
            box.setItem(slot, stack.copy());
            slot++;
        }
    }

    private void refresh() {
        for (int i = 45; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        PlotzStore.ShopDraft draft = PlotzStore.getShopDraft(viewer.getUUID());
        if (draft != null) {
            box.setItem(49, MenuUtil.named(Items.GOLD_INGOT, LanguageManager.tr("shop.sell.set_price") + ": $" + draft.price()));
        } else {
            box.setItem(49, MenuUtil.named(Items.GOLD_INGOT, LanguageManager.tr("shop.sell.set_price")));
        }

        box.setItem(50, MenuUtil.named(Items.WRITABLE_BOOK, LanguageManager.tr("shop.sell.publish")));
        box.setItem(53, MenuUtil.named(Items.BARRIER, LanguageManager.tr("common.back")));
        MenuUtil.putPlayerInfoHead(box, viewer, 45);
        broadcastChanges();
    }

    private List<ItemStack> getSellContents() {
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            ItemStack stack = box.getItem(i);
            if (!stack.isEmpty()) result.add(stack.copy());
        }
        return result;
    }

    private void clearSellArea() {
        for (int i = 0; i < 45; i++) {
            box.setItem(i, ItemStack.EMPTY);
        }
    }

    private void saveDraftFromContainer() {
        List<ItemStack> items = getSellContents();
        if (items.isEmpty()) return;

        PlotzStore.ShopDraft existing = PlotzStore.getShopDraft(viewer.getUUID());
        int price = existing == null ? 100 : existing.price();

        PlotzStore.setShopDraft(new PlotzStore.ShopDraft(
            viewer.getUUID(),
            viewer.getGameProfile().getName(),
            items,
            price
        ));
    }

    private boolean isBlockedAdminTool(ItemStack stack) {
        return stack.is(Items.COMMAND_BLOCK)
            || stack.is(Items.CHAIN_COMMAND_BLOCK)
            || stack.is(Items.REPEATING_COMMAND_BLOCK)
            || stack.is(Items.STRUCTURE_BLOCK)
            || stack.is(Items.STRUCTURE_VOID)
            || stack.is(Items.BARRIER)
            || stack.is(Items.LIGHT)
            || stack.is(Items.DEBUG_STICK)
            || stack.is(Items.JIGSAW);
    }

    private void publish() {
        PlotzStore.ShopDraft draft = PlotzStore.getShopDraft(viewer.getUUID());
        if (draft == null || draft.items().isEmpty()) {
            viewer.sendSystemMessage(Component.literal(LanguageManager.tr("shop.sell.put_items_first")));
            return;
        }

        for (ItemStack stack : draft.items()) {
            if (isBlockedAdminTool(stack)) {
                viewer.sendSystemMessage(Component.literal(LanguageManager.tr("shop.sell.admin_tool_blocked")));
                return;
            }
        }

        for (ItemStack stack : draft.items()) {
            if (isBlockedAdminTool(stack)) {
                viewer.sendSystemMessage(Component.literal(LanguageManager.tr("shop.sell.admin_tool_blocked")));
                return;
            }
        }

        for (ItemStack stack : draft.items()) {
            if (isBlockedAdminTool(stack)) {
                viewer.sendSystemMessage(Component.literal(LanguageManager.tr("shop.sell.admin_tool_blocked")));
                return;
            }
        }

        for (ItemStack stack : draft.items()) {
            if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)
                && stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) != null
                && stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().contains("plotz_admin_tool")) {
                viewer.sendSystemMessage(Component.literal(LanguageManager.tr("shop.sell.admin_tool_blocked")));
                return;
            }
        }

        PlotzStore.addShopListing(new PlotzStore.ShopListing(
            UUID.randomUUID().toString(),
            viewer.getUUID(),
            viewer.getGameProfile().getName(),
            draft.items(),
            draft.price()
        ));

        clearSellArea();
        PlotzStore.clearShopDraft(viewer.getUUID());
        published = true;
        viewer.sendSystemMessage(Component.literal(LanguageManager.tr("shop.sell.published")));
        PlotzShopMenu.open(viewer);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId >= 45 && slotId <= 53) {
            if (slotId == 49) {
                saveDraftFromContainer();
                PlotzStore.ShopDraft draft = PlotzStore.getShopDraft(sp.getUUID());
                if (draft == null || draft.items().isEmpty()) {
                    sp.sendSystemMessage(Component.literal(LanguageManager.tr("shop.sell.put_items_first")));
                    return;
                }

                clearSellArea();
                ShopInputManager.waitForPrice(sp);
                return;
            }

            if (slotId == 50) {
                saveDraftFromContainer();
                publish();
                return;
            }

            if (slotId == 53) {
                PlotzShopMenu.open(sp);
                return;
            }
            return;
        }

        super.clicked(slotId, button, clickType, player);
        saveDraftFromContainer();
        refresh();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (!(player instanceof ServerPlayer sp)) return;
        if (published) return;
        if (ShopInputManager.isTransitioning(sp.getUUID())) return;

        List<ItemStack> toReturn = getSellContents();
        clearSellArea();

        for (ItemStack stack : toReturn) {
            if (!sp.getInventory().add(stack.copy())) {
                sp.drop(stack.copy(), false);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}