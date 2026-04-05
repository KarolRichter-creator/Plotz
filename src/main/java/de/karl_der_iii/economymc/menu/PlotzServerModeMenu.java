package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.service.AdminSettingsManager;
import de.karl_der_iii.economymc.service.LanguageManager;
import de.karl_der_iii.economymc.service.LoanManager;
import de.karl_der_iii.economymc.service.ServerModeConfirmManager;
import de.karl_der_iii.economymc.service.TreasuryManager;
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

public class PlotzServerModeMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final SimpleContainer box;

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzServerModeMenu(containerId, inventory, player),
            Component.literal(LanguageManager.tr("server.mode.title"))
        ));
    }

    public PlotzServerModeMenu(int containerId, Inventory inventory, ServerPlayer viewer) {
        this(containerId, inventory, viewer, new SimpleContainer(54));
    }

    private PlotzServerModeMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x6, containerId, inventory, box, 6);
        this.viewer = viewer;
        this.box = box;
        refresh();
    }

    private ItemStack stateItem(boolean enabled, String title, boolean needsConfirm) {
        List<String> lore = new ArrayList<>();
        lore.add((enabled ? "§a" : "§7") + (enabled ? LanguageManager.tr("admin.on") : LanguageManager.tr("admin.off")));
        if (needsConfirm) {
            lore.add(LanguageManager.tr("server.confirm_needed"));
        }
        return MenuUtil.named(enabled ? Items.LIME_DYE : Items.GRAY_DYE, title, lore);
    }

    private int serverLoanRequestCount() {
        return (int) LoanManager.getVisibleLoans(viewer.getUUID(), true).stream()
            .filter(loan -> loan.targetType() == LoanManager.LoanTargetType.SERVER)
            .filter(loan -> loan.status() == LoanManager.LoanStatus.REQUESTED || loan.status() == LoanManager.LoanStatus.OFFERED)
            .count();
    }

    private void refresh() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        int serverLoanRequests = serverLoanRequestCount();

        box.setItem(4, MenuUtil.named(
            Items.GOLD_BLOCK,
            LanguageManager.tr("common.treasury") + ": $" + TreasuryManager.getTreasury(),
            List.of(
                LanguageManager.tr("server.target_budget") + AdminSettingsManager.treasuryTargetBudget(),
                LanguageManager.tr("server.reaction_strength") + AdminSettingsManager.autoTaxReactionStrength() + "/10",
                LanguageManager.tr("server.min_reaction_strength") + AdminSettingsManager.autoTaxMinReactionStrength()
            )
        ));

        box.setItem(10, MenuUtil.named(Items.RED_CONCRETE, LanguageManager.tr("server.tax_minus")));
        box.setItem(11, MenuUtil.named(Items.PAPER, LanguageManager.tr("server.tax_rate") + TreasuryManager.getTaxPercent() + "%"));
        box.setItem(12, MenuUtil.named(Items.LIME_CONCRETE, LanguageManager.tr("server.tax_plus")));
        box.setItem(13, stateItem(AdminSettingsManager.autoTaxEnabled(), LanguageManager.tr("server.auto_tax"), true));

        box.setItem(19, MenuUtil.named(
            Items.CHEST,
            LanguageManager.tr("server.target_budget") + AdminSettingsManager.treasuryTargetBudget(),
            List.of(
                LanguageManager.tr("server.left_increase_10000"),
                LanguageManager.tr("server.right_decrease_10000"),
                LanguageManager.tr("server.confirm_needed")
            )
        ));

        box.setItem(20, MenuUtil.named(
            Items.COMPARATOR,
            LanguageManager.tr("server.reaction_strength") + AdminSettingsManager.autoTaxReactionStrength(),
            List.of(
                LanguageManager.tr("server.left_increase"),
                LanguageManager.tr("server.right_decrease"),
                LanguageManager.tr("server.min_reaction_strength") + AdminSettingsManager.autoTaxMinReactionStrength()
            )
        ));

        box.setItem(23, MenuUtil.named(Items.RED_CONCRETE, LanguageManager.tr("server.overdue_minus")));
        box.setItem(24, MenuUtil.named(Items.PAPER, LanguageManager.tr("server.overdue_penalty") + TreasuryManager.getOverduePenaltyPercent() + "%"));
        box.setItem(25, MenuUtil.named(Items.LIME_CONCRETE, LanguageManager.tr("server.overdue_plus")));

        box.setItem(28, MenuUtil.named(Items.RED_CONCRETE, LanguageManager.tr("server.cancel_minus")));
        box.setItem(29, MenuUtil.named(Items.PAPER, LanguageManager.tr("server.cancel_penalty") + TreasuryManager.getCancelPenaltyPercent() + "%"));
        box.setItem(30, MenuUtil.named(Items.LIME_CONCRETE, LanguageManager.tr("server.cancel_plus")));

        box.setItem(32, MenuUtil.named(Items.RED_CONCRETE, LanguageManager.tr("server.days_minus")));
        box.setItem(33, MenuUtil.named(Items.CLOCK, LanguageManager.tr("server.max_overdue_days") + TreasuryManager.getMaxOverdueDays()));
        box.setItem(34, MenuUtil.named(Items.LIME_CONCRETE, LanguageManager.tr("server.days_plus")));

        if (AdminSettingsManager.hasPendingAutoTaxDisableRequest()) {
            box.setItem(37, MenuUtil.named(
                Items.REDSTONE_TORCH,
                LanguageManager.tr("server.auto_tax.disable_pending_title"),
                List.of(LanguageManager.tr("server.auto_tax.disable_pending_by") + AdminSettingsManager.pendingAutoTaxDisableRequester())
            ));
        } else if (AdminSettingsManager.hasPendingBudgetChange()) {
            box.setItem(37, MenuUtil.named(
                Items.CHEST,
                LanguageManager.tr("server.budget.pending_title"),
                List.of(
                    LanguageManager.tr("server.auto_tax.disable_pending_by") + AdminSettingsManager.pendingBudgetChangeRequester(),
                    LanguageManager.tr("server.target_budget") + AdminSettingsManager.pendingBudgetValue()
                )
            ));
        } else {
            box.setItem(37, MenuUtil.named(Items.CLOCK, LanguageManager.tr("server.job_open_hour") + AdminSettingsManager.jobAcceptHour() + ":00"));
        }

        box.setItem(39, MenuUtil.named(Items.BOOK, LanguageManager.tr("server.open_jobs")));
        box.setItem(40, MenuUtil.named(
            Items.GOLD_INGOT,
            LanguageManager.tr("bank.title") + " §7(" + serverLoanRequests + ")",
            List.of("§7" + LanguageManager.tr("bank.target.server"))
        ));
        box.setItem(41, MenuUtil.named(Items.CLOCK, LanguageManager.tr("common.treasury")));
        box.setItem(42, MenuUtil.playerInfoHead(viewer));

        box.setItem(49, MenuUtil.named(Items.BARRIER, LanguageManager.tr("common.back")));

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }

        if (slotId == 49) {
            PlotzMainMenu.open(sp);
            return;
        }

        if (slotId == 10 && !AdminSettingsManager.autoTaxEnabled()) {
            TreasuryManager.setTaxPercent(TreasuryManager.getManualTaxPercent() - 1);
        }
        if (slotId == 12 && !AdminSettingsManager.autoTaxEnabled()) {
            TreasuryManager.setTaxPercent(TreasuryManager.getManualTaxPercent() + 1);
        }

        if (slotId == 13) {
            if (AdminSettingsManager.autoTaxEnabled()) {
                ServerModeConfirmManager.requestAutoTaxDisable(sp);
                sp.closeContainer();
                return;
            } else {
                AdminSettingsManager.setAutoTaxEnabled(true);
            }
        }

        if (slotId == 19) {
            long current = AdminSettingsManager.treasuryTargetBudget();
            long newValue = current + (button == 1 ? -10000L : 10000L);
            ServerModeConfirmManager.requestBudgetChange(sp, newValue);
            sp.closeContainer();
            return;
        }

        if (slotId == 20) {
            int min = AdminSettingsManager.autoTaxMinReactionStrength();
            int current = AdminSettingsManager.autoTaxReactionStrength();
            int next = current + (button == 1 ? -1 : 1);
            AdminSettingsManager.setAutoTaxReactionStrength(Math.max(min, next));
        }

        if (slotId == 23) {
            TreasuryManager.setOverduePenaltyPercent(TreasuryManager.getOverduePenaltyPercent() - 1);
        }
        if (slotId == 25) {
            TreasuryManager.setOverduePenaltyPercent(TreasuryManager.getOverduePenaltyPercent() + 1);
        }

        if (slotId == 28) {
            TreasuryManager.setCancelPenaltyPercent(TreasuryManager.getCancelPenaltyPercent() - 1);
        }
        if (slotId == 30) {
            TreasuryManager.setCancelPenaltyPercent(TreasuryManager.getCancelPenaltyPercent() + 1);
        }

        if (slotId == 32) {
            TreasuryManager.setMaxOverdueDays(TreasuryManager.getMaxOverdueDays() - 1);
        }
        if (slotId == 34) {
            TreasuryManager.setMaxOverdueDays(TreasuryManager.getMaxOverdueDays() + 1);
        }

        if (slotId == 37 && !AdminSettingsManager.hasPendingAutoTaxDisableRequest() && !AdminSettingsManager.hasPendingBudgetChange()) {
            AdminSettingsManager.setJobAcceptHour(AdminSettingsManager.jobAcceptHour() + (button == 1 ? -1 : 1));
        }

        if (slotId == 39) {
            PlotzJobsMenu.open(sp, 0, true, true);
            return;
        }

        if (slotId == 40) {
            PlotzBankMenu.open(sp);
            return;
        }

        if (slotId == 41) {
            PlotzHistoryMenu.open(sp, true);
            return;
        }

        refresh();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}