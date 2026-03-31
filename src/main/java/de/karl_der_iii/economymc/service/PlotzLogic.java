package de.karl_der_iii.economymc.service;

import de.karl_der_iii.economymc.data.PlotzStore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public final class PlotzLogic {
    public static final int NORMAL_CHUNK_PRICE = 250;
    public static final int CAPITAL_CHUNK_PRICE = 500;
    public static final int MIN_DISTANCE_BLOCKS = 700;

    private PlotzLogic() {}

    public static boolean isCapital(BlockPos pos) {
        return CapitalAreaManager.isInside(pos);
    }

    public static boolean tryCharge(ServerPlayer player, int amount) {
        if (!EconomyBridge.hasEnough(player, amount)) {
            return false;
        }
        return EconomyBridge.removeMoney(player, amount);
    }

    public static boolean paySeller(ServerPlayer buyerContext, String sellerName, int amount) {
        return EconomyBridge.addMoney(buyerContext.server, sellerName, amount);
    }

    public static boolean isServerClaimBlocked(ServerPlayer player) {
        return false;
    }

    public static boolean isMinDistanceValid(BlockPos pos, java.util.UUID ownerId) {
        for (PlotzStore.PlotEntry plot : PlotzStore.getAllOwnedPlots()) {
            if (plot.ownerId().equals(ownerId)) {
                continue;
            }

            int[] bounds = parseBounds(plot.location());
            if (bounds == null) {
                continue;
            }

            int minChunkX = bounds[0];
            int minChunkZ = bounds[1];
            int maxChunkX = bounds[2];
            int maxChunkZ = bounds[3];

            int minBlockX = minChunkX * 16;
            int maxBlockX = maxChunkX * 16 + 15;
            int minBlockZ = minChunkZ * 16;
            int maxBlockZ = maxChunkZ * 16 + 15;

            int nearestX = clamp(pos.getX(), minBlockX, maxBlockX);
            int nearestZ = clamp(pos.getZ(), minBlockZ, maxBlockZ);

            double dx = pos.getX() - nearestX;
            double dz = pos.getZ() - nearestZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist < MIN_DISTANCE_BLOCKS) {
                return false;
            }
        }

        return true;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int[] parseBounds(String location) {
        try {
            int arrow = location.indexOf("->");
            if (arrow < 0) {
                return null;
            }

            String left = location.substring(location.indexOf("Chunks") + 6, arrow).trim();
            String right = location.substring(arrow + 2).trim();

            String[] leftParts = left.split(",");
            String[] rightParts = right.split(",");

            int minChunkX = Integer.parseInt(leftParts[0].trim());
            int minChunkZ = Integer.parseInt(leftParts[1].trim());
            int maxChunkX = Integer.parseInt(rightParts[0].trim());
            int maxChunkZ = Integer.parseInt(rightParts[1].trim());

            return new int[]{minChunkX, minChunkZ, maxChunkX, maxChunkZ};
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean canBuyNormalCredit(ServerPlayer player) {
        return tryCharge(player, NORMAL_CHUNK_PRICE);
    }

    public static boolean canBuyCapitalCredit(ServerPlayer player) {
        return tryCharge(player, CAPITAL_CHUNK_PRICE);
    }
}