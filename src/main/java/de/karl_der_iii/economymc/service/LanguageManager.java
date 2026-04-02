package de.karl_der_iii.economymc.service;

import java.util.HashMap;
import java.util.Map;

public final class LanguageManager {
    private static final Map<String, Map<String, String>> LANG = new HashMap<>();

    static {
        Map<String, String> en = new HashMap<>();

        en.put("help.header", "§6EconomyMC Commands");
        en.put("help.plots", "§e/ec plots §7- opens the plots menu");
        en.put("help.shop", "§e/ec shop §7- opens the shop menu");
        en.put("help.jobs", "§e/ec jobs §7- opens the jobs menu");
        en.put("help.checks", "§e/ec checks §7- opens the checks menu");
        en.put("help.history", "§e/ec history §7- opens the payment history");
        en.put("help.bank", "§e/ec bank §7- opens the bank menu");
        en.put("help.daily", "§e/ec daily §7- claims the daily reward");
        en.put("help.pay", "§e/ec pay <player/treasury> <amount> §7- sends money");
        en.put("help.servermode", "§e/ec servermode §7- opens server mode");
        en.put("help.adminmode", "§e/ec adminmode §7- opens admin mode");
        en.put("help.admin", "§e/ec admin ... §7- capital and account management");
        en.put("help.language", "§7Languages: German, English, Polish, French, Spanish, Portuguese, Russian, Turkish, Chinese, Japanese");

        en.put("cmd.only_players", "Only players can use this command.");
        en.put("msg.shop_disabled", "§cShop is disabled by admin.");
        en.put("msg.jobs_disabled", "§cJobs are disabled by admin.");
        en.put("msg.checks_disabled", "§cChecks are disabled by admin.");
        en.put("msg.servermode_disabled", "§cServer mode is disabled by admin.");

        en.put("common.close", "§cClose");
        en.put("common.back", "§cBack");
        en.put("common.next", "§7Next Page");
        en.put("common.previous", "§7Previous Page");
        en.put("common.confirm", "§aConfirm");
        en.put("common.cancel", "§cCancel");
        en.put("common.create", "§aCreate");
        en.put("common.edit", "§eEdit");
        en.put("common.price", "§6Price");
        en.put("common.description", "§7Description");
        en.put("common.status", "§7Status");
        en.put("common.page", "§7Page ");
        en.put("common.seller", "§7Seller");
        en.put("common.amount", "§7Amount");
        en.put("common.click_to_view", "§7Click to view");
        en.put("common.publish", "§bPublish");
        en.put("common.sell", "§aSell");
        en.put("common.treasury", "Treasury");
        en.put("common.location", "§7Location");
        en.put("common.chunks", "§7Chunks");

        en.put("menu.player.balance", "§6Balance: $%d");
        en.put("menu.player.normal", "§7Normal Credits: %d");
        en.put("menu.player.capital", "§7Capital Credits: %d");

        en.put("main.menu.title", "EconomyMC");
        en.put("main.plots", "§aPlots");
        en.put("main.shop", "§eShop");
        en.put("main.jobs", "§bJobs");
        en.put("main.checks", "§dChecks");
        en.put("main.bank", "§6Bank");
        en.put("main.history", "§7History");
        en.put("main.market", "§3Market");
        en.put("main.myplots", "§2My Plots");
        en.put("main.mysales", "§5My Sales");
        en.put("main.daily", "§aDaily Reward");
        en.put("main.servermode", "§cServer Mode");
        en.put("main.servermode.disabled", "§8Server Mode Disabled");
        en.put("main.adminmode", "§4Admin Mode");

        en.put("plots.menu.title", "EconomyMC Plots");
        en.put("plots.position.capital", "§6Current Position: Capital Zone");
        en.put("plots.position.normal", "§7Current Position: Normal Zone");
        en.put("plots.buy.normal", "§eBuy Normal Claim Credits");
        en.put("plots.buy.capital", "§6Buy Capital Claim Credits");
        en.put("plots.create.sale", "§aCreate Sale Listing");
        en.put("plots.market", "§3Market Listings");
        en.put("plots.mine", "§bMy Plots");
        en.put("plots.sales", "§dMy Sales");
        en.put("plots.history", "§aOpen History");
        en.put("plots.buy.normal.fail", "§cYou do not have enough money for a normal claim credit.");
        en.put("plots.buy.normal.ok", "§aBought 1 normal claim credit.");
        en.put("plots.buy.capital.fail", "§cYou do not have enough money for a capital claim credit.");
        en.put("plots.buy.capital.ok", "§aBought 1 capital claim credit.");

        en.put("daily.already", "§cDaily already claimed for today. Come back in %dh %dm.");
        en.put("daily.claimed", "§aYou claimed your daily $100.");

        en.put("pay.known_or_treasury", "§cOnly known players or Treasury can receive money.");
        en.put("pay.self", "§cYou cannot pay yourself.");
        en.put("pay.not_enough", "§cYou do not have enough money.");
        en.put("pay.sent", "§aYou paid $%d to %s.");
        en.put("pay.received", "§aYou received $%d from %s.");

        en.put("bank.target.server", "Server");
        en.put("bank.invalid_target", "§cUnknown target player.");
        en.put("bank.not_enough_money", "§cYou do not have enough money.");
        en.put("bank.not_found", "§cLoan not found.");
        en.put("bank.request.created", "§aLoan request created.");
        en.put("bank.offer.created", "§aLoan offer created.");
        en.put("bank.accepted", "§aLoan accepted.");
        en.put("bank.repaid", "§aLoan repaid.");
        en.put("bank.command.list", "§e/ec bank list §7- shows open loan requests and offers");
        en.put("bank.command.request", "§e/ec bank request ...");
        en.put("bank.command.offer", "§e/ec bank offer <loanId> <interest>");
        en.put("bank.command.accept", "§e/ec bank accept <loanId>");
        en.put("bank.command.repay", "§e/ec bank repay <loanId>");

        en.put("history.empty", "§7No entries yet.");
        en.put("history.daily", "§eDaily reward: $%d");
        en.put("history.pay.sent", "§aPay to %s: $%d");
        en.put("history.pay.received", "§aPay from %s: $%d");

        en.put("admin.pos1_set", "§aCapital pos1 set to: %d, %d, %d");
        en.put("admin.pos2_set", "§aCapital pos2 set to: %d, %d, %d");
        en.put("admin.pos_missing", "§cSet /ec admin pos1 and /ec admin pos2 first.");
        en.put("admin.capital_saved", "§aCapital area saved.");
        en.put("admin.capital_cleared", "§aCapital area cleared.");

        LANG.put("en_us", en);

        Map<String, String> de = new HashMap<>(en);
        de.put("help.header", "§6EconomyMC Befehle");
        de.put("help.plots", "§e/ec plots §7- öffnet das Grundstücksmenü");
        de.put("help.shop", "§e/ec shop §7- öffnet das Shop-Menü");
        de.put("help.jobs", "§e/ec jobs §7- öffnet das Jobs-Menü");
        de.put("help.checks", "§e/ec checks §7- öffnet das Checks-Menü");
        de.put("help.history", "§e/ec history §7- öffnet den Zahlungsverlauf");
        de.put("help.bank", "§e/ec bank §7- öffnet das Bank-Menü");
        de.put("help.daily", "§e/ec daily §7- holt die tägliche Belohnung");
        de.put("help.pay", "§e/ec pay <Spieler/Treasury> <Betrag> §7- sendet Geld");
        de.put("help.servermode", "§e/ec servermode §7- öffnet den Servermodus");
        de.put("help.adminmode", "§e/ec adminmode §7- öffnet den Adminmodus");
        de.put("help.admin", "§e/ec admin ... §7- Hauptstadt und Kontoverwaltung");
        de.put("help.language", "§7Sprachen: Deutsch, Englisch, Polnisch, Französisch, Spanisch, Portugiesisch, Russisch, Türkisch, Chinesisch, Japanisch");

        de.put("cmd.only_players", "Nur Spieler können diesen Befehl benutzen.");
        de.put("msg.shop_disabled", "§cShop ist vom Admin deaktiviert.");
        de.put("msg.jobs_disabled", "§cJobs sind vom Admin deaktiviert.");
        de.put("msg.checks_disabled", "§cChecks sind vom Admin deaktiviert.");
        de.put("msg.servermode_disabled", "§cServer-Modus ist vom Admin deaktiviert.");

        de.put("common.close", "§cSchließen");
        de.put("common.back", "§cZurück");

        de.put("menu.player.balance", "§6Kontostand: $%d");
        de.put("menu.player.normal", "§7Normale Credits: %d");
        de.put("menu.player.capital", "§7Hauptstadt-Credits: %d");

        de.put("main.menu.title", "EconomyMC");
        de.put("main.plots", "§aGrundstücke");
        de.put("main.shop", "§eShop");
        de.put("main.jobs", "§bJobs");
        de.put("main.checks", "§dChecks");
        de.put("main.bank", "§6Bank");
        de.put("main.history", "§7Verlauf");
        de.put("main.market", "§3Markt");
        de.put("main.myplots", "§2Meine Grundstücke");
        de.put("main.mysales", "§5Meine Verkäufe");
        de.put("main.daily", "§aTägliche Belohnung");
        de.put("main.servermode", "§cServermodus");
        de.put("main.servermode.disabled", "§8Servermodus deaktiviert");
        de.put("main.adminmode", "§4Adminmodus");

        de.put("plots.menu.title", "EconomyMC Grundstücke");
        de.put("plots.position.capital", "§6Aktuelle Position: Hauptstadt-Zone");
        de.put("plots.position.normal", "§7Aktuelle Position: Normale Zone");
        de.put("plots.buy.normal", "§eNormale Claim-Credits kaufen");
        de.put("plots.buy.capital", "§6Hauptstadt-Claim-Credits kaufen");
        de.put("plots.create.sale", "§aVerkaufsangebot erstellen");
        de.put("plots.market", "§3Marktangebote");
        de.put("plots.mine", "§bMeine Grundstücke");
        de.put("plots.sales", "§dMeine Verkäufe");
        de.put("plots.history", "§aVerlauf öffnen");
        de.put("plots.buy.normal.fail", "§cDu hast nicht genug Geld für einen normalen Claim-Credit.");
        de.put("plots.buy.normal.ok", "§a1 normaler Claim-Credit gekauft.");
        de.put("plots.buy.capital.fail", "§cDu hast nicht genug Geld für einen Hauptstadt-Claim-Credit.");
        de.put("plots.buy.capital.ok", "§a1 Hauptstadt-Claim-Credit gekauft.");

        de.put("daily.already", "§cDaily für heute schon abgeholt. Komm in %dh %dm zurück.");
        de.put("daily.claimed", "§aDu hast dein tägliches $100 abgeholt.");

        de.put("pay.known_or_treasury", "§cNur bekannte Spieler oder das Treasury können Geld erhalten.");
        de.put("pay.self", "§cDu kannst dir nicht selbst Geld senden.");
        de.put("pay.not_enough", "§cDu hast nicht genug Geld.");
        de.put("pay.sent", "§aDu hast $%d an %s bezahlt.");
        de.put("pay.received", "§aDu hast $%d von %s erhalten.");

        de.put("bank.target.server", "Server");
        de.put("bank.invalid_target", "§cUnbekannter Zielspieler.");
        de.put("bank.not_enough_money", "§cDu hast nicht genug Geld.");
        de.put("bank.not_found", "§cKredit nicht gefunden.");
        de.put("bank.request.created", "§aKreditanfrage erstellt.");
        de.put("bank.offer.created", "§aKreditangebot erstellt.");
        de.put("bank.accepted", "§aKredit angenommen.");
        de.put("bank.repaid", "§aKredit zurückgezahlt.");
        de.put("bank.command.list", "§e/ec bank list §7- zeigt offene Kreditanfragen und Angebote");
        de.put("bank.command.request", "§e/ec bank request ...");
        de.put("bank.command.offer", "§e/ec bank offer <loanId> <Zins>");
        de.put("bank.command.accept", "§e/ec bank accept <loanId>");
        de.put("bank.command.repay", "§e/ec bank repay <loanId>");

        de.put("history.empty", "§7Noch keine Einträge vorhanden.");
        de.put("history.daily", "§eTägliche Belohnung: $%d");
        de.put("history.pay.sent", "§aÜberweisung an %s: $%d");
        de.put("history.pay.received", "§aÜberweisung von %s: $%d");

        de.put("admin.pos1_set", "§aCapital pos1 gesetzt auf: %d, %d, %d");
        de.put("admin.pos2_set", "§aCapital pos2 gesetzt auf: %d, %d, %d");
        de.put("admin.pos_missing", "§cSetze zuerst /ec admin pos1 und /ec admin pos2.");
        de.put("admin.capital_saved", "§aHauptstadtbereich gespeichert.");
        de.put("admin.capital_cleared", "§aHauptstadtbereich gelöscht.");

        LANG.put("de_de", de);

        Map<String, String> pl = new HashMap<>(en);
        pl.put("main.menu.title", "EconomyMC");
        pl.put("main.plots", "§aDziałki");
        pl.put("main.shop", "§eSklep");
        pl.put("main.jobs", "§bZadania");
        pl.put("main.checks", "§dCzeki");
        pl.put("main.bank", "§6Bank");
        pl.put("main.history", "§7Historia");
        pl.put("main.market", "§3Rynek");
        pl.put("main.myplots", "§2Moje działki");
        pl.put("main.mysales", "§5Moje sprzedaże");
        pl.put("main.daily", "§aNagroda dzienna");
        pl.put("main.servermode", "§cTryb serwera");
        pl.put("main.servermode.disabled", "§8Tryb serwera wyłączony");
        pl.put("main.adminmode", "§4Tryb administratora");
        pl.put("cmd.only_players", "Tylko gracze mogą używać tej komendy.");
        pl.put("msg.shop_disabled", "§cSklep został wyłączony przez administratora.");
        pl.put("msg.jobs_disabled", "§cZadania zostały wyłączone przez administratora.");
        pl.put("msg.checks_disabled", "§cCzeki zostały wyłączone przez administratora.");
        pl.put("msg.servermode_disabled", "§cTryb serwera został wyłączony przez administratora.");
        pl.put("common.close", "§cZamknij");
        pl.put("common.back", "§cPowrót");
        pl.put("plots.menu.title", "EconomyMC Działki");
        pl.put("plots.position.capital", "§6Aktualna pozycja: Strefa stolicy");
        pl.put("plots.position.normal", "§7Aktualna pozycja: Zwykła strefa");
        pl.put("plots.buy.normal", "§eKup zwykłe kredyty claimów");
        pl.put("plots.buy.capital", "§6Kup kredyty claimów stolicy");
        pl.put("plots.create.sale", "§aUtwórz ofertę sprzedaży");
        pl.put("plots.market", "§3Oferty rynku");
        pl.put("plots.mine", "§bMoje działki");
        pl.put("plots.sales", "§dMoje sprzedaże");
        pl.put("plots.buy.normal.fail", "§cNie masz wystarczająco pieniędzy na zwykły kredyt claimu.");
        pl.put("plots.buy.normal.ok", "§aKupiono 1 zwykły kredyt claimu.");
        pl.put("plots.buy.capital.fail", "§cNie masz wystarczająco pieniędzy na kredyt claimu stolicy.");
        pl.put("plots.buy.capital.ok", "§aKupiono 1 kredyt claimu stolicy.");
        LANG.put("pl_pl", pl);

        Map<String, String> fr = new HashMap<>(en);
        fr.put("main.menu.title", "EconomyMC");
        fr.put("main.plots", "§aParcelles");
        fr.put("main.shop", "§eBoutique");
        fr.put("main.jobs", "§bJobs");
        fr.put("main.checks", "§dChèques");
        fr.put("main.bank", "§6Banque");
        fr.put("main.history", "§7Historique");
        fr.put("main.market", "§3Marché");
        fr.put("main.myplots", "§2Mes parcelles");
        fr.put("main.mysales", "§5Mes ventes");
        fr.put("main.daily", "§aRécompense quotidienne");
        fr.put("main.servermode", "§cMode serveur");
        fr.put("main.servermode.disabled", "§8Mode serveur désactivé");
        fr.put("main.adminmode", "§4Mode admin");
        fr.put("common.close", "§cFermer");
        fr.put("common.back", "§cRetour");
        LANG.put("fr_fr", fr);

        Map<String, String> es = new HashMap<>(en);
        es.put("main.menu.title", "EconomyMC");
        es.put("main.plots", "§aParcelas");
        es.put("main.shop", "§eTienda");
        es.put("main.jobs", "§bTrabajos");
        es.put("main.checks", "§dCheques");
        es.put("main.bank", "§6Banco");
        es.put("main.history", "§7Historial");
        es.put("main.market", "§3Mercado");
        es.put("main.myplots", "§2Mis parcelas");
        es.put("main.mysales", "§5Mis ventas");
        es.put("main.daily", "§aRecompensa diaria");
        es.put("main.servermode", "§cModo servidor");
        es.put("main.servermode.disabled", "§8Modo servidor desactivado");
        es.put("main.adminmode", "§4Modo admin");
        fr.put("common.close", "§cCerrar");
        fr.put("common.back", "§cAtrás");
        LANG.put("es_es", es);

        Map<String, String> pt = new HashMap<>(en);
        pt.put("main.menu.title", "EconomyMC");
        pt.put("main.plots", "§aTerrenos");
        pt.put("main.shop", "§eLoja");
        pt.put("main.jobs", "§bTrabalhos");
        pt.put("main.checks", "§dCheques");
        pt.put("main.bank", "§6Banco");
        pt.put("main.history", "§7Histórico");
        pt.put("main.market", "§3Mercado");
        pt.put("main.myplots", "§2Meus terrenos");
        pt.put("main.mysales", "§5Minhas vendas");
        pt.put("main.daily", "§aRecompensa diária");
        pt.put("main.servermode", "§cModo servidor");
        pt.put("main.servermode.disabled", "§8Modo servidor desativado");
        pt.put("main.adminmode", "§4Modo admin");
        pt.put("common.close", "§cFechar");
        pt.put("common.back", "§cVoltar");
        LANG.put("pt_br", pt);

        Map<String, String> ru = new HashMap<>(en);
        ru.put("main.menu.title", "EconomyMC");
        ru.put("main.plots", "§aУчастки");
        ru.put("main.shop", "§eМагазин");
        ru.put("main.jobs", "§bЗадания");
        ru.put("main.checks", "§dЧеки");
        ru.put("main.bank", "§6Банк");
        ru.put("main.history", "§7История");
        ru.put("main.market", "§3Рынок");
        ru.put("main.myplots", "§2Мои участки");
        ru.put("main.mysales", "§5Мои продажи");
        ru.put("main.daily", "§aЕжедневная награда");
        ru.put("main.servermode", "§cРежим сервера");
        ru.put("main.servermode.disabled", "§8Режим сервера отключён");
        ru.put("main.adminmode", "§4Режим администратора");
        ru.put("common.close", "§cЗакрыть");
        ru.put("common.back", "§cНазад");
        LANG.put("ru_ru", ru);

        Map<String, String> tr = new HashMap<>(en);
        tr.put("main.menu.title", "EconomyMC");
        tr.put("main.plots", "§aArsalar");
        tr.put("main.shop", "§eMağaza");
        tr.put("main.jobs", "§bİşler");
        tr.put("main.checks", "§dÇekler");
        tr.put("main.bank", "§6Banka");
        tr.put("main.history", "§7Geçmiş");
        tr.put("main.market", "§3Pazar");
        tr.put("main.myplots", "§2Arsalarım");
        tr.put("main.mysales", "§5Satışlarım");
        tr.put("main.daily", "§aGünlük ödül");
        tr.put("main.servermode", "§cSunucu modu");
        tr.put("main.servermode.disabled", "§8Sunucu modu devre dışı");
        tr.put("main.adminmode", "§4Yönetici modu");
        tr.put("common.close", "§cKapat");
        tr.put("common.back", "§cGeri");
        LANG.put("tr_tr", tr);

        Map<String, String> zh = new HashMap<>(en);
        zh.put("main.menu.title", "EconomyMC");
        zh.put("main.plots", "§a地块");
        zh.put("main.shop", "§e商店");
        zh.put("main.jobs", "§b工作");
        zh.put("main.checks", "§d支票");
        zh.put("main.bank", "§6银行");
        zh.put("main.history", "§7历史");
        zh.put("main.market", "§3市场");
        zh.put("main.myplots", "§2我的地块");
        zh.put("main.mysales", "§5我的销售");
        zh.put("main.daily", "§a每日奖励");
        zh.put("main.servermode", "§c服务器模式");
        zh.put("main.servermode.disabled", "§8服务器模式已禁用");
        zh.put("main.adminmode", "§4管理模式");
        zh.put("common.close", "§c关闭");
        zh.put("common.back", "§c返回");
        LANG.put("zh_cn", zh);

        Map<String, String> ja = new HashMap<>(en);
        ja.put("main.menu.title", "EconomyMC");
        ja.put("main.plots", "§a区画");
        ja.put("main.shop", "§eショップ");
        ja.put("main.jobs", "§bジョブ");
        ja.put("main.checks", "§d小切手");
        ja.put("main.bank", "§6銀行");
        ja.put("main.history", "§7履歴");
        ja.put("main.market", "§3マーケット");
        ja.put("main.myplots", "§2自分の区画");
        ja.put("main.mysales", "§5自分の販売");
        ja.put("main.daily", "§aデイリー報酬");
        ja.put("main.servermode", "§cサーバーモード");
        ja.put("main.servermode.disabled", "§8サーバーモード無効");
        ja.put("main.adminmode", "§4管理モード");
        ja.put("common.close", "§c閉じる");
        ja.put("common.back", "§c戻る");
        LANG.put("ja_jp", ja);
    }

    private LanguageManager() {}

    public static String tr(String key) {
        return tr(AdminSettingsManager.language(), key);
    }

    public static String tr(String lang, String key) {
        Map<String, String> source = LANG.getOrDefault(lang, LANG.get("en_us"));
        return source.getOrDefault(key, LANG.get("en_us").getOrDefault(key, key));
    }

    public static String format(String key, Object... args) {
        return String.format(tr(key), args);
    }

    public static String currentLanguageLabel() {
        return tr("help.language");
    }
}