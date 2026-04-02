package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.service.BalanceManager;
import de.karl_der_iii.economymc.service.LanguageManager;
import de.karl_der_iii.economymc.service.TransactionHistoryManager;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class PlotzHistoryMenu extends ChestMenu {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private final boolean treasuryView;

    public static void open(ServerPlayer player, boolean treasuryView) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzHistoryMenu(containerId, inventory, player, treasuryView),
            Component.literal(LanguageManager.tr("history.title"))
        ));
    }

    public PlotzHistoryMenu(int containerId, Inventory inventory, ServerPlayer viewer, boolean treasuryView) {
        this(containerId, inventory, viewer, new SimpleContainer(54), treasuryView);
    }

    private PlotzHistoryMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box, boolean treasuryView) {
        super(MenuType.GENERIC_9x6, containerId, inventory, box, 6);
        this.viewer = viewer;
        this.box = box;
        this.treasuryView = treasuryView;
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        UUID owner = treasuryView ? BalanceManager.TREASURY_ACCOUNT_ID : viewer.getUUID();
        List<TransactionHistoryManager.Entry> entries = TransactionHistoryManager.getEntries(owner, 45);

        box.setItem(4, MenuUtil.named(
            Items.CLOCK,
            treasuryView ? LanguageManager.tr("history.treasury") : LanguageManager.tr("history.mine")
        ));

        if (entries.isEmpty()) {
            box.setItem(22, MenuUtil.named(Items.BOOK, LanguageManager.tr("history.empty")));
        } else {
            int slot = 0;
            for (TransactionHistoryManager.Entry entry : entries) {
                if (slot >= 45) break;
                String time = FORMAT.format(Instant.ofEpochMilli(entry.timestamp()).atZone(ZoneId.systemDefault()));
                box.setItem(slot, MenuUtil.named(
                    Items.PAPER,
                    "§7[" + time + "] " + entry.text()
                ));
                slot++;
            }
        }

        box.setItem(45, MenuUtil.playerInfoHead(viewer));
        box.setItem(49, MenuUtil.named(Items.BARRIER, LanguageManager.tr("common.back")));
        box.setItem(53, MenuUtil.named(
            Items.PAPER,
            treasuryView ? LanguageManager.tr("history.treasury") : LanguageManager.tr("history.mine")
        ));

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId == 49) {
            if (treasuryView) {
                PlotzServerModeMenu.open(sp);
            } else {
                PlotzMainMenu.open(sp);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}