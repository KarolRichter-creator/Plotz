package de.karol.plotz.service;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public final class PlotzLogic {
    public static final int NORMAL_CHUNK_PRICE = 250;
    public static final int CAPITAL_CHUNK_PRICE = 500;
    public static final int MIN_DISTANCE_BLOCKS = 700;

    public static final int CAPITAL_MIN_X = -2000;
    public static final int CAPITAL_MAX_X = 2000;
    public static final int CAPITAL_MIN_Z = -2000;
    public static final int CAPITAL_MAX_Z = 2000;

    private PlotzLogic() {}

    public static boolean isCapital(BlockPos pos) {
        return pos.getX() >= CAPITAL_MIN_X
            && pos.getX() <= CAPITAL_MAX_X
            && pos.getZ() >= CAPITAL_MIN_Z
            && pos.getZ() <= CAPITAL_MAX_Z;
    }

    public static boolean tryCharge(ServerPlayer player, int amount) {
        return EconomyBridge.removeMoney(player, amount);
    }

    public static void paySeller(ServerPlayer buyerContext, String sellerName, int amount) {
        EconomyBridge.addMoney(buyerContext.server, sellerName, amount);
    }

    public static boolean isServerClaimBlocked(ServerPlayer player) {
        return false;
    }

    public static boolean isMinDistanceValid(ServerPlayer player) {
        return true;
    }
}