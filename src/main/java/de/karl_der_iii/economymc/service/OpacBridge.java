package de.karl_der_iii.economymc.service;

import de.karl_der_iii.economymc.data.PlotzStore;
import de.karl_der_iii.economymc.service.LanguageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.fml.ModList;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.player.api.IPlayerClaimPosListAPI;
import xaero.pac.common.claims.player.api.IPlayerDimensionClaimsAPI;
import xaero.pac.common.claims.tracker.api.IClaimsManagerListenerAPI;
import xaero.pac.common.event.api.OPACServerAddonRegisterEvent;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.claims.player.api.IServerPlayerClaimInfoAPI;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class OpacBridge {
    private static final Map<String, UUID> CLAIM_OWNER_CACHE = new ConcurrentHashMap<>();
    private static MinecraftServer server;

    private OpacBridge() {}

    public static boolean isInstalled() {
        return ModList.get().isLoaded("openpartiesandclaims");
    }

    public static String getPartyStatusText(ServerPlayer player) {
        return isInstalled() ? "§aOPAC connected" : "§cOPAC not installed";
    }

    public static void registerClaimsTracker(OPACServerAddonRegisterEvent event) {
        server = event.getServer();

        event.getClaimsManagerTrackerAPI().register(new IClaimsManagerListenerAPI() {
            @Override
            public void onWholeRegionChange(ResourceLocation dimension, int regionX, int regionZ) {
            }

            @Override
            public void onChunkChange(ResourceLocation dimension, int chunkX, int chunkZ, IPlayerChunkClaimAPI claim) {
                handleChunkChange(dimension, chunkX, chunkZ, claim);
            }

            @Override
            public void onDimensionChange(ResourceLocation dimension) {
            }
        });
    }

    public static void syncOwnedClaims(ServerPlayer player) {
        if (!isInstalled()) {
            return;
        }

        try {
            PlotzStore.clearOwnedClaimsFor(player.getUUID());

            IServerPlayerClaimInfoAPI info = OpenPACServerAPI.get(player.server)
                .getServerClaimsManager()
                .getPlayerInfo(player.getUUID());

            if (info == null) {
                return;
            }

            info.getStream().forEach(entry -> {
                ResourceLocation dimension = entry.getKey();
                IPlayerDimensionClaimsAPI dimClaims = entry.getValue();

                Set<ChunkPos> allChunks = new HashSet<>();

                dimClaims.getStream().forEach((IPlayerClaimPosListAPI posList) -> {
                    posList.getStream().forEach(allChunks::add);
                });

                for (Set<ChunkPos> group : groupConnectedChunks(allChunks)) {
                    if (group.isEmpty()) {
                        continue;
                    }

                    int minX = Integer.MAX_VALUE;
                    int maxX = Integer.MIN_VALUE;
                    int minZ = Integer.MAX_VALUE;
                    int maxZ = Integer.MIN_VALUE;

                    for (ChunkPos chunk : group) {
                        minX = Math.min(minX, chunk.x);
                        maxX = Math.max(maxX, chunk.x);
                        minZ = Math.min(minZ, chunk.z);
                        maxZ = Math.max(maxZ, chunk.z);
                    }

                    int centerChunkX = (minX + maxX) / 2;
                    int centerChunkZ = (minZ + maxZ) / 2;
                    boolean capital = PlotzLogic.isCapital(new BlockPos(centerChunkX * 16 + 8, 64, centerChunkZ * 16 + 8));

                    String location = dimension + " | Chunks " + minX + "," + minZ + " -> " + maxX + "," + maxZ;

                    PlotzStore.upsertOwnedGroupedClaim(new PlotzStore.PlotEntry(
                        player.getUUID(),
                        player.getGameProfile().getName(),
                        capital ? "Capital Plot" : "Plot",
                        capital,
                        group.size(),
                        location,
                        "Synced from Open Parties and Claims"
                    ));
                }
            });
        } catch (Exception ignored) {
        }
    }

    private static Set<Set<ChunkPos>> groupConnectedChunks(Set<ChunkPos> chunks) {
        Set<Set<ChunkPos>> groups = new HashSet<>();
        Set<ChunkPos> remaining = new HashSet<>(chunks);

        while (!remaining.isEmpty()) {
            ChunkPos start = remaining.iterator().next();
            remaining.remove(start);

            Set<ChunkPos> group = new HashSet<>();
            ArrayDeque<ChunkPos> queue = new ArrayDeque<>();
            queue.add(start);

            while (!queue.isEmpty()) {
                ChunkPos current = queue.poll();
                if (!group.add(current)) {
                    continue;
                }

                for (ChunkPos neighbor : neighbors(current)) {
                    if (remaining.remove(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }

            groups.add(group);
        }

        return groups;
    }

    private static java.util.List<ChunkPos> neighbors(ChunkPos pos) {
        java.util.List<ChunkPos> list = new ArrayList<>(4);
        list.add(new ChunkPos(pos.x + 1, pos.z));
        list.add(new ChunkPos(pos.x - 1, pos.z));
        list.add(new ChunkPos(pos.x, pos.z + 1));
        list.add(new ChunkPos(pos.x, pos.z - 1));
        return list;
    }

    private static void handleChunkChange(ResourceLocation dimension, int chunkX, int chunkZ, IPlayerChunkClaimAPI claim) {
        if (server == null) {
            return;
        }

        String key = dimension + "|" + chunkX + "|" + chunkZ;
        UUID previousOwner = CLAIM_OWNER_CACHE.get(key);

        if (claim == null) {
            CLAIM_OWNER_CACHE.remove(key);
            if (previousOwner != null) {
                ServerPlayer oldOwner = server.getPlayerList().getPlayer(previousOwner);
                if (oldOwner != null) {
                    syncOwnedClaims(oldOwner);
                }
            }
            return;
        }

        UUID newOwner = claim.getPlayerId();
        if (newOwner == null) {
            return;
        }

        if (newOwner.equals(previousOwner)) {
            return;
        }

        CLAIM_OWNER_CACHE.put(key, newOwner);

        ServerPlayer player = server.getPlayerList().getPlayer(newOwner);
        if (player == null) {
            return;
        }

        BlockPos claimPos = new BlockPos(chunkX * 16 + 8, 64, chunkZ * 16 + 8);
        boolean capital = PlotzLogic.isCapital(claimPos);
        int price = PlotzLogic.getClaimPrice(claimPos);

        if (previousOwner == null) {
            if (!PlotzLogic.isMinDistanceValid(claimPos, newOwner)) {
                try {
                    OpenPACServerAPI.get(server).getServerClaimsManager().unclaim(dimension, chunkX, chunkZ);
                    CLAIM_OWNER_CACHE.remove(key);
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        LanguageManager.format("plots.claim.distance_fail", PlotzLogic.MIN_DISTANCE_BLOCKS)
                    ));
                } catch (Exception e) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        LanguageManager.tr("plots.claim.rollback_fail")
                    ));
                }
                return;
            }

            if (!PlotzLogic.chargeClaimToTreasury(player, claimPos)) {
                try {
                    OpenPACServerAPI.get(server).getServerClaimsManager().unclaim(dimension, chunkX, chunkZ);
                    CLAIM_OWNER_CACHE.remove(key);
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        capital
                            ? "§cClaim reverted: you need $" + price + " for a capital claim."
                            : "§cClaim reverted: you need $" + price + " for a normal claim."
                    ));
                } catch (Exception e) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cClaim detected without enough money, but automatic rollback failed."
                    ));
                }
                return;
            }

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                capital
                    ? "§aCapital claim bought for $" + price + "."
                    : "§aClaim bought for $" + price + "."
            ));
        }

        syncOwnedClaims(player);

        if (previousOwner != null) {
            ServerPlayer oldOwner = server.getPlayerList().getPlayer(previousOwner);
            if (oldOwner != null) {
                syncOwnedClaims(oldOwner);
            }
        }
    }
}