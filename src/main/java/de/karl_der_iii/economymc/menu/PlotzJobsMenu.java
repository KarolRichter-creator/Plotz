package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.service.JobManager;
import de.karl_der_iii.economymc.service.JobsInputManager;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlotzJobsMenu extends ChestMenu {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM HH:mm");
    private final ServerPlayer viewer;
    private final SimpleContainer box;
    private final int page;
    private final boolean allowCreate;
    private final boolean serverOnly;
    private final Map<Integer, String> jobIdsBySlot = new HashMap<>();

    public static void open(ServerPlayer player, int page, boolean allowCreate, boolean serverOnly) {
        JobManager.processExpiredJobs();

        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, p) -> new PlotzJobsMenu(containerId, inventory, player, page, allowCreate, serverOnly),
            Component.literal(LanguageManager.tr(serverOnly ? "jobs.server.title" : "jobs.menu.title"))
        ));
    }

    public PlotzJobsMenu(int containerId, Inventory inventory, ServerPlayer viewer, int page, boolean allowCreate, boolean serverOnly) {
        this(containerId, inventory, viewer, new SimpleContainer(54), page, allowCreate, serverOnly);
    }

    private PlotzJobsMenu(int containerId, Inventory inventory, ServerPlayer viewer, SimpleContainer box, int page, boolean allowCreate, boolean serverOnly) {
        super(MenuType.GENERIC_9x6, containerId, inventory, box, 6);
        this.viewer = viewer;
        this.box = box;
        this.page = page;
        this.allowCreate = allowCreate;
        this.serverOnly = serverOnly;
        refresh();
    }

    private String formatTime(long millis) {
        return FORMATTER.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()));
    }

    private String statusText(JobManager.JobEntry job) {
        return switch (job.status()) {
            case OPEN -> JobManager.canAcceptNow(job)
                ? LanguageManager.tr("jobs.status.open")
                : LanguageManager.tr("jobs.status.opens_at") + formatTime(job.availableAt());
            case IN_PROGRESS -> LanguageManager.tr("jobs.status.in_progress") + job.workerName();
            case COMPLETED -> LanguageManager.tr("jobs.status.completed");
            case CONFIRMED -> LanguageManager.tr("jobs.status.confirmed");
            case CANCELLED -> LanguageManager.tr("jobs.status.cancelled");
            case FAILED -> LanguageManager.tr("jobs.status.failed");
        };
    }

    private void refresh() {
        jobIdsBySlot.clear();

        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        box.setItem(4, MenuUtil.named(
            serverOnly ? Items.GOLD_BLOCK : Items.BOOK,
            LanguageManager.tr(serverOnly ? "jobs.server.title" : "jobs.menu.title")
        ));

        List<JobManager.JobEntry> jobs = JobManager.getVisibleJobs();
        if (serverOnly) {
            jobs = jobs.stream().filter(JobManager.JobEntry::serverJob).collect(Collectors.toList());
        }

        int start = page * 36;
        int end = Math.min(start + 36, jobs.size());

        int slot = 9;
        for (int i = start; i < end; i++) {
            if (slot % 9 == 8) {
                slot += 2;
            }

            if (slot >= 45) {
                break;
            }

            JobManager.JobEntry job = jobs.get(i);
            box.setItem(slot, MenuUtil.named(
                job.serverJob() ? Items.GOLD_BLOCK : Items.PAPER,
                (job.serverJob() ? "§6" : "§e") + job.title() + " §7| $" + job.reward() + " | " + statusText(job)
            ));
            jobIdsBySlot.put(slot, job.id());
            slot++;
        }

        box.setItem(45, MenuUtil.playerInfoHead(viewer));
        box.setItem(49, MenuUtil.named(Items.BARRIER, LanguageManager.tr("common.back")));
        box.setItem(50, MenuUtil.named(Items.ARROW, LanguageManager.tr("common.previous")));
        box.setItem(51, MenuUtil.named(Items.PAPER, LanguageManager.tr("common.page") + (page + 1)));
        box.setItem(52, MenuUtil.named(Items.ARROW, LanguageManager.tr("common.next")));

        if (allowCreate) {
            box.setItem(53, MenuUtil.named(Items.EMERALD, LanguageManager.tr("jobs.add")));
        } else {
            box.setItem(53, MenuUtil.named(Items.BOOK, LanguageManager.tr("jobs.create_hint")));
        }

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId == 49) {
            if (serverOnly) {
                PlotzServerModeMenu.open(sp);
            } else {
                PlotzMainMenu.open(sp);
            }
            return;
        }

        if (slotId == 50) {
            if (page > 0) open(sp, page - 1, allowCreate, serverOnly);
            return;
        }

        if (slotId == 52) {
            open(sp, page + 1, allowCreate, serverOnly);
            return;
        }

        if (slotId == 53) {
            if (allowCreate) {
                JobsInputManager.startPlayerJob(sp);
            }
            return;
        }

        String jobId = jobIdsBySlot.get(slotId);
        if (jobId != null) {
            PlotzJobDetailMenu.open(sp, jobId, page, allowCreate, serverOnly);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}