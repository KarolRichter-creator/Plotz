package de.karol.plotz.service;

import net.minecraft.server.level.ServerPlayer;

public class EconomyService {
    private EconomyService() {}

    public static boolean tryCharge(ServerPlayer player, int amount) {
        // TODO:
        // Hier später EconomyCraft sauber einbauen.
        // Zum Beispiel:
        // 1. Kontostand prüfen
        // 2. Geld mit EconomyCraft abziehen
        return true;
    }

    public static void paySeller(String sellerName, int amount) {
        // TODO:
        // Hier später Verkäufer über EconomyCraft auszahlen.
    }
}
