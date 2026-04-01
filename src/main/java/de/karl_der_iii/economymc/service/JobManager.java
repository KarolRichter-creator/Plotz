package de.karl_der_iii.economymc.service;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public final class JobManager {
    public enum JobStatus {
        OPEN,
        IN_PROGRESS,
        COMPLETED,
        CONFIRMED,
        CANCELLED,
        FAILED
    }

    public record JobEntry(
        String id,
        UUID creatorId,
        String creatorName,
        String title,
        String description,
        int reward,
        int dueDays,
        long createdAt,
        long dueAt,
        long availableAt,
        boolean serverJob,
        JobStatus status,
        UUID workerId,
        String workerName,
        long acceptedAt,
        long completedAt
    ) {}

    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("economymc-jobs.properties");
    private static final Properties PROPS = new Properties();
    private static boolean loaded = false;

    private JobManager() {}

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        if (!Files.exists(FILE)) {
            PROPS.setProperty("nextId", "1");
            save();
            return;
        }

        try (InputStream in = Files.newInputStream(FILE)) {
            PROPS.load(in);
        } catch (IOException ignored) {
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                PROPS.store(out, "EconomyMC jobs");
            }
        } catch (IOException ignored) {
        }
    }

    private static String nextId() {
        ensureLoaded();
        int next = Integer.parseInt(PROPS.getProperty("nextId", "1"));
        PROPS.setProperty("nextId", Integer.toString(next + 1));
        save();
        return Integer.toString(next);
    }

    private static long computeAvailableAt() {
        ZonedDateTime next = ZonedDateTime.now(ZoneId.systemDefault())
            .plusDays(1)
            .withHour(AdminSettingsManager.jobAcceptHour())
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        return next.toInstant().toEpochMilli();
    }

    public static JobEntry createJob(UUID creatorId, String creatorName, String title, String description, int reward, int dueDays, boolean serverJob) {
        ensureLoaded();

        String id = nextId();
        long now = System.currentTimeMillis();
        long dueAt = now + dueDays * 24L * 60L * 60L * 1000L;
        long availableAt = computeAvailableAt();

        String base = "job." + id + ".";
        PROPS.setProperty(base + "creatorId", creatorId == null ? "" : creatorId.toString());
        PROPS.setProperty(base + "creatorName", safe(creatorName));
        PROPS.setProperty(base + "title", safe(title));
        PROPS.setProperty(base + "description", safe(description));
        PROPS.setProperty(base + "reward", Integer.toString(reward));
        PROPS.setProperty(base + "dueDays", Integer.toString(dueDays));
        PROPS.setProperty(base + "createdAt", Long.toString(now));
        PROPS.setProperty(base + "dueAt", Long.toString(dueAt));
        PROPS.setProperty(base + "availableAt", Long.toString(availableAt));
        PROPS.setProperty(base + "serverJob", Boolean.toString(serverJob));
        PROPS.setProperty(base + "status", JobStatus.OPEN.name());
        PROPS.setProperty(base + "workerId", "");
        PROPS.setProperty(base + "workerName", "");
        PROPS.setProperty(base + "acceptedAt", "0");
        PROPS.setProperty(base + "completedAt", "0");
        save();

        return getJob(id);
    }

    public static JobEntry getJob(String id) {
        ensureLoaded();
        String base = "job." + id + ".";
        if (!PROPS.containsKey(base + "title")) return null;

        long createdAt = parseLong(PROPS.getProperty(base + "createdAt", "0"), 0L);

        return new JobEntry(
            id,
            parseUuid(PROPS.getProperty(base + "creatorId", "")),
            PROPS.getProperty(base + "creatorName", "Unknown"),
            PROPS.getProperty(base + "title", "Untitled"),
            PROPS.getProperty(base + "description", ""),
            parseInt(PROPS.getProperty(base + "reward", "0"), 0),
            parseInt(PROPS.getProperty(base + "dueDays", "1"), 1),
            createdAt,
            parseLong(PROPS.getProperty(base + "dueAt", "0"), 0L),
            parseLong(PROPS.getProperty(base + "availableAt", Long.toString(createdAt)), createdAt),
            Boolean.parseBoolean(PROPS.getProperty(base + "serverJob", "false")),
            parseStatus(PROPS.getProperty(base + "status", "OPEN")),
            parseUuid(PROPS.getProperty(base + "workerId", "")),
            PROPS.getProperty(base + "workerName", ""),
            parseLong(PROPS.getProperty(base + "acceptedAt", "0"), 0L),
            parseLong(PROPS.getProperty(base + "completedAt", "0"), 0L)
        );
    }

    public static boolean canAcceptNow(JobEntry job) {
        return System.currentTimeMillis() >= job.availableAt();
    }

    public static List<JobEntry> getVisibleJobs() {
        ensureLoaded();
        processExpiredJobs();

        List<JobEntry> result = new ArrayList<>();
        for (String key : PROPS.stringPropertyNames()) {
            if (key.startsWith("job.") && key.endsWith(".title")) {
                String id = key.substring(4, key.length() - 6);
                JobEntry entry = getJob(id);
                if (entry != null && entry.status() != JobStatus.CONFIRMED && entry.status() != JobStatus.CANCELLED && entry.status() != JobStatus.FAILED) {
                    result.add(entry);
                }
            }
        }

        result.sort(Comparator.comparingInt(a -> -Integer.parseInt(a.id())));
        return result;
    }

    public static boolean acceptJob(String id, UUID workerId, String workerName) {
        JobEntry job = getJob(id);
        if (job == null || job.status() != JobStatus.OPEN || !canAcceptNow(job)) return false;

        String base = "job." + id + ".";
        PROPS.setProperty(base + "status", JobStatus.IN_PROGRESS.name());
        PROPS.setProperty(base + "workerId", workerId.toString());
        PROPS.setProperty(base + "workerName", safe(workerName));
        PROPS.setProperty(base + "acceptedAt", Long.toString(System.currentTimeMillis()));
        save();
        return true;
    }

    public static boolean markCompleted(String id, UUID workerId) {
        JobEntry job = getJob(id);
        if (job == null || job.status() != JobStatus.IN_PROGRESS || job.workerId() == null || !job.workerId().equals(workerId)) return false;

        String base = "job." + id + ".";
        PROPS.setProperty(base + "status", JobStatus.COMPLETED.name());
        PROPS.setProperty(base + "completedAt", Long.toString(System.currentTimeMillis()));
        save();
        return true;
    }

    public static boolean confirmJob(String id) {
        JobEntry job = getJob(id);
        if (job == null || job.status() != JobStatus.COMPLETED) return false;

        int finalReward = calculateCurrentReward(job);
        if (job.workerId() != null && finalReward != 0) {
            BalanceManager.addBalanceAllowNegative(job.workerId(), finalReward);
            TransactionHistoryManager.add(job.workerId(), LanguageManager.format("history.job.reward", finalReward));
        }

        int remainder = job.reward() - Math.max(finalReward, 0);
        if (remainder > 0) {
            refundCreator(job, remainder);
        }

        setStatus(id, JobStatus.CONFIRMED);
        return true;
    }

    public static boolean cancelByWorker(String id) {
        JobEntry job = getJob(id);
        if (job == null || job.status() != JobStatus.IN_PROGRESS || job.workerId() == null) return false;

        int penalty = TreasuryManager.calculateCancelPenalty(job.reward());
        BalanceManager.addBalanceAllowNegative(job.workerId(), -penalty);
        refundCreator(job, job.reward());
        setStatus(id, JobStatus.CANCELLED);
        return true;
    }

    public static boolean withdrawByCreator(String id) {
        JobEntry job = getJob(id);
        if (job == null || job.status() != JobStatus.OPEN) return false;

        refundCreator(job, job.reward());
        setStatus(id, JobStatus.CANCELLED);
        return true;
    }

    public static int calculateCurrentReward(JobEntry job) {
        long overdueDays = getOverdueDays(job);
        int reduction = TreasuryManager.calculateOverdueReduction(job.reward(), overdueDays);
        return job.reward() - reduction;
    }

    public static long getOverdueDays(JobEntry job) {
        long now = System.currentTimeMillis();
        if (now <= job.dueAt()) return 0L;
        return (now - job.dueAt()) / (24L * 60L * 60L * 1000L);
    }

    public static void processExpiredJobs() {
        ensureLoaded();

        for (JobEntry job : getAllJobsRaw()) {
            if (job.status() == JobStatus.IN_PROGRESS) {
                long overdueDays = getOverdueDays(job);
                if (overdueDays >= TreasuryManager.getMaxOverdueDays()) {
                    if (job.workerId() != null) {
                        int finalReward = calculateCurrentReward(job);
                        if (finalReward < 0) {
                            BalanceManager.addBalanceAllowNegative(job.workerId(), finalReward);
                        }
                    }

                    refundCreator(job, job.reward());
                    setStatus(job.id(), JobStatus.FAILED);
                }
            }
        }
    }

    private static void refundCreator(JobEntry job, int amount) {
        if (amount <= 0) return;

        if (job.serverJob()) {
            TreasuryManager.addTreasury(amount);
            TransactionHistoryManager.addTreasury(LanguageManager.format("history.job.refund", amount));
        } else if (job.creatorId() != null) {
            BalanceManager.addBalance(job.creatorId(), amount);
            TransactionHistoryManager.add(job.creatorId(), LanguageManager.format("history.job.refund", amount));
        }
    }

    private static void setStatus(String id, JobStatus status) {
        String base = "job." + id + ".";
        PROPS.setProperty(base + "status", status.name());
        save();
    }

    private static List<JobEntry> getAllJobsRaw() {
        List<JobEntry> result = new ArrayList<>();
        for (String key : PROPS.stringPropertyNames()) {
            if (key.startsWith("job.") && key.endsWith(".title")) {
                String id = key.substring(4, key.length() - 6);
                JobEntry entry = getJob(id);
                if (entry != null) result.add(entry);
            }
        }
        return result;
    }

    private static UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static long parseLong(String raw, long fallback) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static JobStatus parseStatus(String raw) {
        try {
            return JobStatus.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return JobStatus.OPEN;
        }
    }

    private static String safe(String text) {
        return text == null ? "" : text.replace("\n", " ").replace("\r", " ");
    }
}