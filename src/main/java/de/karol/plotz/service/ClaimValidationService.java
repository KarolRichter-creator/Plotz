package de.karol.plotz.service;

import de.karol.plotz.config.PlotzConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class ClaimValidationService {
    private ClaimValidationService() {}

    public static boolean isCapitalChunk(BlockPos pos) {
        return pos.getX() >= PlotzConfig.CAPITAL_MIN_X
            && pos.getX() <= PlotzConfig.CAPITAL_MAX_X
            && pos.getZ() >= PlotzConfig.CAPITAL_MIN_Z
            && pos.getZ() <= PlotzConfig.CAPITAL_MAX_Z;
    }

    public static boolean isServerClaimBlocked(ServerPlayer player) {
        // TODO:
        // Hier später OPAC-Server-Claims prüfen.
        // Alles, was Server-Claim ist, soll blockiert werden.
        return false;
    }

    public static boolean isMinDistanceValid(ServerPlayer player) {
        // TODO:
        // Hier später Mindestabstand 700 Blöcke prüfen.
        return true;
    }
}
