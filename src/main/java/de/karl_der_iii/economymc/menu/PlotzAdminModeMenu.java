package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.service.AdminSettingsManager;
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

import java.util.List;

public class PlotzAdminModeMenu extends ChestMenu {
    private static final String[] LANG_ORDER = {
        "de_de", "en_us", "pl_pl", "fr_fr", "es_es", "pt_br", "ru_ru", "tr_tr", "zh_cn", "ja_jp"
    };

    private final ServerPlayer viewer;
    private final SimpleContainer box;

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzAdminModeMenu(containerId, inventory, player),
            Component.literal(LanguageManager.tr("admin.mode.title"))
        ));
    }

    public PlotzAdminModeMenu(int containerId, Inventory inventory, ServerPlayer viewer) {
        this(containerId, inventory, viewer, new SimpleContainer(54));
    }

    private PlotzAdminModeMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box) {
        super(MenuType.GENERIC_9x6, containerId, inventory, box, 6);
        this.viewer = viewer;
        this.box = box;
        refresh();
    }

    private ItemStack toggleItem(boolean enabled, String title) {
        return MenuUtil.named(
            enabled ? Items.LIME_DYE : Items.GRAY_DYE,
            (enabled ? "§a" : "§7") + title + " §7- " +
                (enabled ? LanguageManager.tr("admin.on") : LanguageManager.tr("admin.off"))
        );
    }

    private String currentLangCode() {
        return AdminSettingsManager.language();
    }

    private int currentLangIndex() {
        String current = currentLangCode();
        for (int i = 0; i < LANG_ORDER.length; i++) {
            if (LANG_ORDER[i].equalsIgnoreCase(current)) {
                return i;
            }
        }
        return 0;
    }

    private void setLangByIndex(int index) {
        if (index < 0) index = LANG_ORDER.length - 1;
        if (index >= LANG_ORDER.length) index = 0;
        AdminSettingsManager.setLanguage(LANG_ORDER[index]);
    }

    private void refresh() {
        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        box.setItem(4, MenuUtil.named(Items.REDSTONE_TORCH, LanguageManager.tr("admin.mode.title")));

        box.setItem(10, toggleItem(AdminSettingsManager.jobsEnabled(), LanguageManager.tr("admin.jobs")));
        box.setItem(11, toggleItem(AdminSettingsManager.checksEnabled(), LanguageManager.tr("admin.checks")));
        box.setItem(12, toggleItem(AdminSettingsManager.shopEnabled(), LanguageManager.tr("admin.shop")));
        box.setItem(13, toggleItem(AdminSettingsManager.plotMarketEnabled(), LanguageManager.tr("admin.plot_market")));
        box.setItem(14, toggleItem(AdminSettingsManager.serverModeEnabled(), LanguageManager.tr("admin.server_mode")));
        box.setItem(15, toggleItem(AdminSettingsManager.serverShopEnabled(), LanguageManager.tr("admin.server_shop")));
        box.setItem(16, toggleItem(AdminSettingsManager.dailyEnabled(), LanguageManager.tr("main.daily")));

        box.setItem(19, MenuUtil.named(
            Items.PAPER,
            LanguageManager.tr("admin.min_tax") + ": " + AdminSettingsManager.minTaxPercent() + "%"
        ));
        box.setItem(20, MenuUtil.named(
            Items.PAPER,
            LanguageManager.tr("admin.min_overdue") + ": " + AdminSettingsManager.minOverduePercent() + "%"
        ));
        box.setItem(21, MenuUtil.named(
            Items.PAPER,
            LanguageManager.tr("admin.min_cancel") + ": " + AdminSettingsManager.minCancelPercent() + "%"
        ));
        box.setItem(22, MenuUtil.named(
            Items.PAPER,
            LanguageManager.tr("server.min_reaction_strength") + AdminSettingsManager.autoTaxMinReactionStrength()
        ));

        box.setItem(23, MenuUtil.named(Items.ARROW, LanguageManager.tr("admin.language.previous")));
        box.setItem(24, MenuUtil.named(
            Items.GLOBE_BANNER_PATTERN,
            LanguageManager.tr("admin.language") + ": " + LanguageManager.languageName(currentLangCode())
        ));
        box.setItem(25, MenuUtil.named(Items.ARROW, LanguageManager.tr("admin.language.next")));

        box.setItem(28, MenuUtil.named(
            Items.EMERALD,
            LanguageManager.tr("admin.daily.base") + AdminSettingsManager.dailyBaseReward()
        ));
        box.setItem(29, MenuUtil.named(
            Items.GOLD_NUGGET,
            LanguageManager.tr("admin.daily.rate") + AdminSettingsManager.dailyIncreasePercent() + "%"
        ));
        box.setItem(30, MenuUtil.named(
            Items.GOLD_BLOCK,
            LanguageManager.tr("admin.daily.max") + AdminSettingsManager.dailyMaxReward()
        ));

        if (AdminSettingsManager.hasPendingBudgetChange()) {
            box.setItem(32, MenuUtil.named(
                Items.ORANGE_CONCRETE,
                LanguageManager.tr("admin.pending.budget"),
                List.of(
                    LanguageManager.tr("admin.pending.from") + AdminSettingsManager.pendingBudgetChangeRequester(),
                    LanguageManager.tr("server.target_budget") + AdminSettingsManager.pendingBudgetValue()
                )
            ));
            box.setItem(33, MenuUtil.named(Items.LIME_CONCRETE, LanguageManager.tr("admin.approve")));
            box.setItem(34, MenuUtil.named(Items.RED_CONCRETE, LanguageManager.tr("admin.deny")));
        } else if (AdminSettingsManager.hasPendingAutoTaxDisableRequest()) {
            box.setItem(32, MenuUtil.named(
                Items.ORANGE_CONCRETE,
                LanguageManager.tr("admin.pending.auto_tax"),
                List.of(
                    LanguageManager.tr("admin.pending.from") + AdminSettingsManager.pendingAutoTaxDisableRequester()
                )
            ));
            box.setItem(33, MenuUtil.named(Items.LIME_CONCRETE, LanguageManager.tr("admin.approve")));
            box.setItem(34, MenuUtil.named(Items.RED_CONCRETE, LanguageManager.tr("admin.deny")));
        }

        box.setItem(31, MenuUtil.playerInfoHead(viewer));
        box.setItem(40, MenuUtil.named(Items.BARRIER, LanguageManager.tr("common.back")));

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }

        switch (slotId) {
            case 10 -> AdminSettingsManager.setJobsEnabled(!AdminSettingsManager.jobsEnabled());
            case 11 -> AdminSettingsManager.setChecksEnabled(!AdminSettingsManager.checksEnabled());
            case 12 -> AdminSettingsManager.setShopEnabled(!AdminSettingsManager.shopEnabled());
            case 13 -> AdminSettingsManager.setPlotMarketEnabled(!AdminSettingsManager.plotMarketEnabled());
            case 14 -> AdminSettingsManager.setServerModeEnabled(!AdminSettingsManager.serverModeEnabled());
            case 15 -> AdminSettingsManager.setServerShopEnabled(!AdminSettingsManager.serverShopEnabled());
            case 16 -> AdminSettingsManager.setDailyEnabled(!AdminSettingsManager.dailyEnabled());

            case 19 -> AdminSettingsManager.setMinTaxPercent(AdminSettingsManager.minTaxPercent() + (button == 1 ? -1 : 1));
            case 20 -> AdminSettingsManager.setMinOverduePercent(AdminSettingsManager.minOverduePercent() + (button == 1 ? -1 : 1));
            case 21 -> AdminSettingsManager.setMinCancelPercent(AdminSettingsManager.minCancelPercent() + (button == 1 ? -1 : 1));
            case 22 -> AdminSettingsManager.setAutoTaxMinReactionStrength(AdminSettingsManager.autoTaxMinReactionStrength() + (button == 1 ? -1 : 1));

            case 23 -> setLangByIndex(currentLangIndex() - 1);
            case 24, 25 -> setLangByIndex(currentLangIndex() + 1);

            case 28 -> AdminSettingsManager.setDailyBaseReward(AdminSettingsManager.dailyBaseReward() + (button == 1 ? -10 : 10));
            case 29 -> AdminSettingsManager.setDailyIncreasePercent(AdminSettingsManager.dailyIncreasePercent() + (button == 1 ? -1 : 1));
            case 30 -> AdminSettingsManager.setDailyMaxReward(AdminSettingsManager.dailyMaxReward() + (button == 1 ? -10 : 10));

            case 33 -> {
                if (AdminSettingsManager.hasPendingBudgetChange()) {
                    AdminSettingsManager.approvePendingBudgetChange();
                } else if (AdminSettingsManager.hasPendingAutoTaxDisableRequest()) {
                    AdminSettingsManager.approvePendingAutoTaxDisableRequest();
                }
            }
            case 34 -> {
                if (AdminSettingsManager.hasPendingBudgetChange()) {
                    AdminSettingsManager.denyPendingBudgetChange();
                } else if (AdminSettingsManager.hasPendingAutoTaxDisableRequest()) {
                    AdminSettingsManager.denyPendingAutoTaxDisableRequest();
                }
            }

            case 40 -> {
                PlotzMainMenu.open(sp);
                return;
            }
            default -> {
            }
        }

        refresh();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}