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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlotzChecksMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private final int page;
    private final Map<Integer, String> checkIds = new HashMap<>();

    public static void open(ServerPlayer player, int page) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzChecksMenu(containerId, inventory, player, page),
            Component.literal(LanguageManager.tr("checks.menu.title"))
        ));
    }

    public PlotzChecksMenu(int containerId, Inventory inventory, ServerPlayer viewer, int page) {
        this(containerId, inventory, viewer, new SimpleContainer(54), page);
    }

    private PlotzChecksMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box, int page) {
        super(MenuType.GENERIC_9x6, containerId, inventory, box, 6);
        this.viewer = viewer;
        this.box = box;
        this.page = page;
        refresh();
    }

    private void refresh() {
        checkIds.clear();

        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        box.setItem(4, MenuUtil.named(Items.PAPER, LanguageManager.tr("checks.menu.title")));

        List<CheckManager.CheckEntry> checks = CheckManager.getAllChecks();
        int start = page * 45;
        int end = Math.min(start + 45, checks.size());

        int slot = 0;
        for (int i = start; i < end; i++) {
            CheckManager.CheckEntry entry = checks.get(i);
            box.setItem(slot, MenuUtil.named(
                entry.redeemed() ? Items.GRAY_DYE : Items.PAPER,
                LanguageManager.format("checks.entry", entry.redeemed() ? "§7" : "§e", entry.amount(), entry.creatorName())
            ));
            checkIds.put(slot, entry.id());
            slot++;
        }

        box.setItem(45, MenuUtil.playerInfoHead(viewer));
        box.setItem(49, MenuUtil.named(Items.BARRIER, LanguageManager.tr("common.back")));
        box.setItem(50, MenuUtil.named(Items.ARROW, LanguageManager.tr("common.previous")));
        box.setItem(51, MenuUtil.named(Items.PAPER, LanguageManager.tr("common.page") + (page + 1)));
        box.setItem(52, MenuUtil.named(Items.ARROW, LanguageManager.tr("common.next")));
        box.setItem(53, MenuUtil.named(Items.EMERALD, LanguageManager.tr("checks.create")));

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId == 49) {
            PlotzMainMenu.open(sp);
            return;
        }

        if (slotId == 50) {
            if (page > 0) open(sp, page - 1);
            return;
        }

        if (slotId == 52) {
            open(sp, page + 1);
            return;
        }

        if (slotId == 53) {
            ChecksInputManager.startCreate(sp);
            return;
        }

        String id = checkIds.get(slotId);
        if (id != null) {
            PlotzCheckRedeemMenu.open(sp, id, page);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}