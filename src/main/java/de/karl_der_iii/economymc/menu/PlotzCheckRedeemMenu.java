package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.service.CheckManager;
import de.karl_der_iii.economymc.service.ChecksInputManager;
import de.karl_der_iii.economymc.service.LanguageManager;
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

public class PlotzCheckRedeemMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private final String checkId;
    private final int returnPage;

    public static void open(ServerPlayer player, String checkId, int returnPage) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzCheckRedeemMenu(containerId, inventory, player, checkId, returnPage),
            Component.literal(LanguageManager.tr("check.redeem.title"))
        ));
    }

    public PlotzCheckRedeemMenu(int containerId, Inventory inventory, ServerPlayer viewer, String checkId, int returnPage) {
        this(containerId, inventory, viewer, new SimpleContainer(27), checkId, returnPage);
    }

    private PlotzCheckRedeemMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box, String checkId, int returnPage) {
        super(MenuType.GENERIC_9x3, containerId, inventory, box, 3);
        this.viewer = viewer;
        this.box = box;
        this.checkId = checkId;
        this.returnPage = returnPage;
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        CheckManager.CheckEntry entry = CheckManager.getCheck(checkId);
        if (entry == null) {
            box.setItem(13, MenuUtil.named(Items.BARRIER, "§cCheck not found"));
            return;
        }

        box.setItem(11, MenuUtil.named(Items.PAPER, "§eCreator: " + entry.creatorName()));
        box.setItem(12, MenuUtil.named(Items.GOLD_INGOT, "§6Amount: $" + entry.amount()));
        box.setItem(13, MenuUtil.named(entry.redeemed() ? Items.GRAY_DYE : Items.LIME_DYE, entry.redeemed() ? "§7Already redeemed" : "§aRedeem"));
        box.setItem(21, MenuUtil.named(Items.BARRIER, "§cBack"));
        MenuUtil.putPlayerInfoHead(box, viewer, 18);
        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId == 21) {
            PlotzChecksMenu.open(sp, returnPage);
            return;
        }

        if (slotId == 13) {
            CheckManager.CheckEntry entry = CheckManager.getCheck(checkId);
            if (entry == null || entry.redeemed()) {
                sp.sendSystemMessage(Component.literal("§cThis check can no longer be redeemed."));
                PlotzChecksMenu.open(sp, returnPage);
                return;
            }

            ChecksInputManager.startRedeem(sp, checkId);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}