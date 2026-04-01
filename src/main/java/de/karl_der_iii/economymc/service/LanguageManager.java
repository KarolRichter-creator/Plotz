package de.karl_der_iii.economymc.service;

import java.util.HashMap;
import java.util.Map;

public final class LanguageManager {
    private static final Map<String, Map<String, String>> LANG = new HashMap<>();

    static {
        Map<String, String> de = new HashMap<>();
        de.put("help.header", "§6EconomyMC Befehle");
        de.put("help.plots", "§e/ec plots §7- öffnet das Grundstücksmenü");
        de.put("help.shop", "§e/ec shop §7- öffnet das Shop-Menü");
        de.put("help.jobs", "§e/ec jobs §7- öffnet das Jobs-Menü");
        de.put("help.checks", "§e/ec checks §7- öffnet das Checks-Menü");
        de.put("help.history", "§e/ec history §7- öffnet den Zahlungsverlauf");
        de.put("help.daily", "§e/ec daily §7- holt die tägliche Belohnung");
        de.put("help.pay", "§e/ec pay <Spieler/Treasury> <Betrag> §7- sendet Geld");
        de.put("help.servermode", "§e/ec servermode §7- öffnet den Servermodus");
        de.put("help.adminmode", "§e/ec adminmode §7- öffnet den Adminmodus");
        de.put("help.admin", "§e/ec admin ... §7- Hauptstadt und Kontoverwaltung");
        de.put("help.language", "§7Sprachen: Deutsch, Englisch, Polnisch, Französisch, Spanisch");
        de.put("cmd.only_players", "Only players can use this command.");
        de.put("msg.shop_disabled", "§cShop ist vom Admin deaktiviert.");
        de.put("msg.jobs_disabled", "§cJobs sind vom Admin deaktiviert.");
        de.put("msg.checks_disabled", "§cChecks sind vom Admin deaktiviert.");
        de.put("msg.servermode_disabled", "§cServer-Modus ist vom Admin deaktiviert.");
        de.put("admin.language", "§7Sprache: Deutsch");
        de.put("admin.language.toggle", "§bSprache wechseln");
        de.put("plots.menu.title", "EconomyMC Grundstücke");
        de.put("jobs.menu.title", "EconomyMC Jobs");
        de.put("jobs.server.title", "EconomyMC Server-Jobs");
        de.put("job.detail.title", "EconomyMC Jobdetails");
        de.put("checks.menu.title", "EconomyMC Checks");
        de.put("check.redeem.title", "EconomyMC Check einlösen");
        de.put("server.mode.title", "EconomyMC Servermodus");
        de.put("admin.mode.title", "EconomyMC Adminmodus");
        de.put("history.title", "EconomyMC Verlauf");
        de.put("history.empty", "§7Noch keine Einträge vorhanden.");
        de.put("history.mine", "§bMein Verlauf");
        de.put("history.treasury", "§6Treasury-Verlauf");
        de.put("history.open", "§aVerlauf öffnen");
        de.put("history.pay.sent", "§aPay an %s: $%d");
        de.put("history.pay.received", "§aPay von %s: $%d");
        de.put("history.daily", "§eDaily Belohnung: $%d");
        de.put("history.check.create", "§dCheck erstellt: $%d");
        de.put("history.check.redeem", "§dCheck eingelöst: $%d");
        de.put("history.job.reward", "§bJob ausgezahlt: $%d");
        de.put("history.job.refund", "§6Job-Rückzahlung: $%d");
        de.put("history.treasury.tax", "§6Steuer erhalten: $%d");
        de.put("history.admin.set", "§cAdmin setzte Guthaben: $%d");
        LANG.put("de_de", de);

        Map<String, String> en = new HashMap<>();
        en.put("help.header", "§6EconomyMC Commands");
        en.put("help.plots", "§e/ec plots §7- opens the plots menu");
        en.put("help.shop", "§e/ec shop §7- opens the shop menu");
        en.put("help.jobs", "§e/ec jobs §7- opens the jobs menu");
        en.put("help.checks", "§e/ec checks §7- opens the checks menu");
        en.put("help.history", "§e/ec history §7- opens the payment history");
        en.put("help.daily", "§e/ec daily §7- claims the daily reward");
        en.put("help.pay", "§e/ec pay <player/treasury> <amount> §7- sends money");
        en.put("help.servermode", "§e/ec servermode §7- opens server mode");
        en.put("help.adminmode", "§e/ec adminmode §7- opens admin mode");
        en.put("help.admin", "§e/ec admin ... §7- capital and account management");
        en.put("help.language", "§7Languages: German, English, Polish, French, Spanish");
        en.put("cmd.only_players", "Only players can use this command.");
        en.put("msg.shop_disabled", "§cShop is disabled by admin.");
        en.put("msg.jobs_disabled", "§cJobs are disabled by admin.");
        en.put("msg.checks_disabled", "§cChecks are disabled by admin.");
        en.put("msg.servermode_disabled", "§cServer mode is disabled by admin.");
        en.put("admin.language", "§7Language: English");
        en.put("admin.language.toggle", "§bSwitch language");
        en.put("plots.menu.title", "EconomyMC Plots");
        en.put("jobs.menu.title", "EconomyMC Jobs");
        en.put("jobs.server.title", "EconomyMC Server Jobs");
        en.put("job.detail.title", "EconomyMC Job Details");
        en.put("checks.menu.title", "EconomyMC Checks");
        en.put("check.redeem.title", "EconomyMC Redeem Check");
        en.put("server.mode.title", "EconomyMC Server Mode");
        en.put("admin.mode.title", "EconomyMC Admin Mode");
        en.put("history.title", "EconomyMC History");
        en.put("history.empty", "§7No entries yet.");
        en.put("history.mine", "§bMy History");
        en.put("history.treasury", "§6Treasury History");
        en.put("history.open", "§aOpen History");
        en.put("history.pay.sent", "§aPay to %s: $%d");
        en.put("history.pay.received", "§aPay from %s: $%d");
        en.put("history.daily", "§eDaily reward: $%d");
        en.put("history.check.create", "§dCreated check: $%d");
        en.put("history.check.redeem", "§dRedeemed check: $%d");
        en.put("history.job.reward", "§bJob payout: $%d");
        en.put("history.job.refund", "§6Job refund: $%d");
        en.put("history.treasury.tax", "§6Tax received: $%d");
        en.put("history.admin.set", "§cAdmin set balance: $%d");
        LANG.put("en_us", en);

        Map<String, String> pl = new HashMap<>(en);
        pl.put("admin.language", "§7Język: Polski");
        LANG.put("pl_pl", pl);

        Map<String, String> fr = new HashMap<>(en);
        fr.put("admin.language", "§7Langue : Français");
        LANG.put("fr_fr", fr);

        Map<String, String> es = new HashMap<>(en);
        es.put("admin.language", "§7Idioma: Español");
        LANG.put("es_es", es);
    }

    private LanguageManager() {}

    public static String tr(String key) {
        return tr(AdminSettingsManager.language(), key);
    }

    public static String tr(String lang, String key) {
        Map<String, String> source = LANG.getOrDefault(lang, LANG.get("en_us"));
        return source.getOrDefault(key, key);
    }

    public static String format(String key, Object... args) {
        return String.format(tr(key), args);
    }

    public static String currentLanguageLabel() {
        return tr("admin.language");
    }
}