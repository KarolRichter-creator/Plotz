package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.service.JobManager;
import de.karl_der_iii.economymc.service.JobsInputManager;
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
import java.util.stream.Collectors;

public class PlotzJobsMenu extends ChestMenu {
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
            Component.literal(serverOnly ? "Server Jobs" : "Jobs")
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

    private String statusText(JobManager.JobEntry job) {
        return switch (job.status()) {
            case OPEN -> "§aOpen";
            case IN_PROGRESS -> "§6In Progress: " + job.workerName();
            case COMPLETED -> "§bCompleted";
            case CONFIRMED -> "§aConfirmed";
            case CANCELLED -> "§cCancelled";
            case FAILED -> "§4Failed";
        };
    }

    private void refresh() {
        jobIdsBySlot.clear();

        for (int i = 0; i < box.getContainerSize(); i++) {
            box.setItem(i, MenuUtil.named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }

        List<JobManager.JobEntry> jobs = JobManager.getVisibleJobs();
        if (serverOnly) {
            jobs = jobs.stream().filter(JobManager.JobEntry::serverJob).collect(Collectors.toList());
        }

        int start = page * 45;
        int end = Math.min(start + 45, jobs.size());

        int slot = 0;
        for (int i = start; i < end; i++) {
            JobManager.JobEntry job = jobs.get(i);
            box.setItem(slot, MenuUtil.named(
                job.serverJob() ? Items.GOLD_BLOCK : Items.PAPER,
                (job.serverJob() ? "§6" : "§e") + job.title() + " §7| $" + job.reward() + " | " + statusText(job)
            ));
            jobIdsBySlot.put(slot, job.id());
            slot++;
        }

        box.setItem(45, MenuUtil.playerInfoHead(viewer));
        box.setItem(49, MenuUtil.named(Items.BARRIER, "§cClose"));
        box.setItem(50, MenuUtil.named(Items.ARROW, "§7Previous Page"));
        box.setItem(51, MenuUtil.named(Items.PAPER, "§7Page " + (page + 1)));
        box.setItem(52, MenuUtil.named(Items.ARROW, "§7Next Page"));

        if (allowCreate) {
            box.setItem(53, MenuUtil.named(Items.EMERALD, "§aAdd Job"));
        } else {
            box.setItem(53, MenuUtil.named(Items.BOOK, "§7Create jobs in the correct menu"));
        }

        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        if (slotId == 49) {
            sp.closeContainer();
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