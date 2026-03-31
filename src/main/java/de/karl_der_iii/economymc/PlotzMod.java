package de.karl_der_iii.economymc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.karl_der_iii.economymc.menu.PlotzAdminModeMenu;
import de.karl_der_iii.economymc.menu.PlotzChecksMenu;
import de.karl_der_iii.economymc.menu.PlotzJobsMenu;
import de.karl_der_iii.economymc.menu.PlotzMainMenu;
import de.karl_der_iii.economymc.menu.PlotzServerModeMenu;
import de.karl_der_iii.economymc.menu.PlotzShopMenu;
import de.karl_der_iii.economymc.service.AdminSettingsManager;
import de.karl_der_iii.economymc.service.BalanceManager;
import de.karl_der_iii.economymc.service.CapitalAreaManager;
import de.karl_der_iii.economymc.service.ChecksInputManager;
import de.karl_der_iii.economymc.service.DailyRewardManager;
import de.karl_der_iii.economymc.service.DraftInputManager;
import de.karl_der_iii.economymc.service.JobManager;
import de.karl_der_iii.economymc.service.JobsInputManager;
import de.karl_der_iii.economymc.service.OpacBridge;
import de.karl_der_iii.economymc.service.ScoreboardManager;
import de.karl_der_iii.economymc.service.ShopInputManager;
import de.karl_der_iii.economymc.service.TreasuryManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import xaero.pac.common.event.api.OPACServerAddonRegisterEvent;

import java.util.Optional;
import java.util.UUID;

@Mod(PlotzMod.MOD_ID)
public class PlotzMod {
    public static final String MOD_ID = "plotz";

    public PlotzMod(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::onOpacAddonRegister);
        NeoForge.EVENT_BUS.addListener(this::onServerChat);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        System.out.println("[EconomyMC] Mod gestartet.");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("ec")
                .requires(source -> source.hasPermission(0))
                .executes(ctx -> {
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal("Only players can use /ec."));
                        return 0;
                    }
                    PlotzMainMenu.open(player);
                    return 1;
                })

                .then(Commands.literal("shop").executes(ctx -> {
                    if (!AdminSettingsManager.shopEnabled()) {
                        ctx.getSource().sendFailure(Component.literal("§cShop is disabled by admin."));
                        return 0;
                    }
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal("Only players can use /ec shop."));
                        return 0;
                    }
                    PlotzShopMenu.open(player);
                    return 1;
                }))

                .then(Commands.literal("jobs").executes(ctx -> {
                    if (!AdminSettingsManager.jobsEnabled()) {
                        ctx.getSource().sendFailure(Component.literal("§cJobs are disabled by admin."));
                        return 0;
                    }
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal("Only players can use /ec jobs."));
                        return 0;
                    }
                    PlotzJobsMenu.open(player, 0, true, false);
                    return 1;
                }))

                .then(Commands.literal("checks").executes(ctx -> {
                    if (!AdminSettingsManager.checksEnabled()) {
                        ctx.getSource().sendFailure(Component.literal("§cChecks are disabled by admin."));
                        return 0;
                    }
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal("Only players can use /ec checks."));
                        return 0;
                    }
                    PlotzChecksMenu.open(player, 0);
                    return 1;
                }))

                .then(Commands.literal("servermode").requires(source -> source.hasPermission(2)).executes(ctx -> {
                    if (!AdminSettingsManager.serverModeEnabled()) {
                        ctx.getSource().sendFailure(Component.literal("§cServer mode is disabled by admin."));
                        return 0;
                    }
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal("Only players can use /ec servermode."));
                        return 0;
                    }
                    PlotzServerModeMenu.open(player);
                    return 1;
                }))

                .then(Commands.literal("adminmode").requires(source -> source.hasPermission(2)).executes(ctx -> {
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal("Only players can use /ec adminmode."));
                        return 0;
                    }
                    PlotzAdminModeMenu.open(player);
                    return 1;
                }))

                .then(Commands.literal("daily").executes(ctx -> {
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal("Only players can use /ec daily."));
                        return 0;
                    }

                    if (!DailyRewardManager.canClaim(player.getUUID())) {
                        long remaining = DailyRewardManager.getRemainingMs(player.getUUID()) / 1000L;
                        long hours = remaining / 3600L;
                        long minutes = (remaining % 3600L) / 60L;
                        player.sendSystemMessage(Component.literal("§cDaily already claimed for today. Come back in " + hours + "h " + minutes + "m."));
                        return 0;
                    }

                    BalanceManager.addBalance(player.getUUID(), 100);
                    ScoreboardManager.update(player.server);
                    DailyRewardManager.markClaimed(player.getUUID());
                    player.sendSystemMessage(Component.literal("§aYou claimed your daily $100."));
                    return 1;
                }))

                .then(Commands.literal("pay")
                    .then(Commands.argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) ->
                            SharedSuggestionProvider.suggest(BalanceManager.getKnownAccountNames(ctx.getSource().getServer()), builder)
                        )
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(ctx -> {
                                if (!(ctx.getSource().getEntity() instanceof ServerPlayer sender)) {
                                    ctx.getSource().sendFailure(Component.literal("Only players can use /ec pay."));
                                    return 0;
                                }

                                String name = StringArgumentType.getString(ctx, "player");
                                int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                Optional<UUID> resolved = BalanceManager.resolveKnownAccount(sender.server, name);
                                if (resolved.isEmpty()) {
                                    ctx.getSource().sendFailure(Component.literal("§cOnly known players or Treasury can receive money."));
                                    return 0;
                                }

                                UUID targetId = resolved.get();
                                if (targetId.equals(sender.getUUID())) {
                                    ctx.getSource().sendFailure(Component.literal("§cYou cannot pay yourself."));
                                    return 0;
                                }

                                if (!BalanceManager.removeBalance(sender.getUUID(), amount)) {
                                    ctx.getSource().sendFailure(Component.literal("§cYou do not have enough money."));
                                    return 0;
                                }

                                BalanceManager.addBalance(targetId, amount);
                                ScoreboardManager.update(sender.server);

                                String targetName = BalanceManager.resolveDisplayName(sender.server, targetId);
                                sender.sendSystemMessage(Component.literal("§aYou paid $" + amount + " to " + targetName + "."));

                                ServerPlayer onlineTarget = sender.server.getPlayerList().getPlayer(targetId);
                                if (onlineTarget != null) {
                                    onlineTarget.sendSystemMessage(Component.literal("§aYou received $" + amount + " from " + sender.getGameProfile().getName() + "."));
                                }

                                return 1;
                            }))))

                .then(Commands.literal("admin").requires(source -> source.hasPermission(2))
                    .then(Commands.literal("pos1").executes(ctx -> {
                        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                            ctx.getSource().sendFailure(Component.literal("Only players can use this command."));
                            return 0;
                        }
                        BlockPos pos = player.blockPosition();
                        CapitalAreaManager.setPos1(pos);
                        ctx.getSource().sendSuccess(() -> Component.literal("§aCapital pos1 set to: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);
                        return 1;
                    }))
                    .then(Commands.literal("pos2").executes(ctx -> {
                        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                            ctx.getSource().sendFailure(Component.literal("Only players can use this command."));
                            return 0;
                        }
                        BlockPos pos = player.blockPosition();
                        CapitalAreaManager.setPos2(pos);
                        ctx.getSource().sendSuccess(() -> Component.literal("§aCapital pos2 set to: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);
                        return 1;
                    }))
                    .then(Commands.literal("setcapital").executes(ctx -> {
                        if (!CapitalAreaManager.canCreateArea()) {
                            ctx.getSource().sendFailure(Component.literal("§cSet /ec admin pos1 and /ec admin pos2 first."));
                            return 0;
                        }
                        CapitalAreaManager.applyArea();
                        ctx.getSource().sendSuccess(() -> Component.literal("§aCapital area saved."), false);
                        return 1;
                    }))
                    .then(Commands.literal("clearcapital").executes(ctx -> {
                        CapitalAreaManager.clearArea();
                        ctx.getSource().sendSuccess(() -> Component.literal("§aCapital area cleared."), false);
                        return 1;
                    }))
                    .then(Commands.literal("setmoney")
                        .then(Commands.argument("account", StringArgumentType.word())
                            .suggests((ctx, builder) ->
                                SharedSuggestionProvider.suggest(BalanceManager.getKnownAccountNames(ctx.getSource().getServer()), builder)
                            )
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    String account = StringArgumentType.getString(ctx, "account");
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                    if (account.equalsIgnoreCase("treasury") || account.equalsIgnoreCase("server")) {
                                        TreasuryManager.setTreasury(amount);
                                        ScoreboardManager.update(ctx.getSource().getServer());
                                        ctx.getSource().sendSuccess(() -> Component.literal("§aSet Treasury to $" + amount), false);
                                        return 1;
                                    }

                                    Optional<UUID> target = BalanceManager.resolveKnownPlayer(ctx.getSource().getServer(), account);
                                    if (target.isEmpty()) {
                                        ctx.getSource().sendFailure(Component.literal("§cOnly known players or Treasury are allowed."));
                                        return 0;
                                    }

                                    BalanceManager.setBalance(target.get(), amount);
                                    ScoreboardManager.update(ctx.getSource().getServer());
                                    ctx.getSource().sendSuccess(() -> Component.literal("§aSet balance of " + account + " to $" + amount), false);
                                    return 1;
                                }))))
                    .then(Commands.literal("addmoney")
                        .then(Commands.argument("account", StringArgumentType.word())
                            .suggests((ctx, builder) ->
                                SharedSuggestionProvider.suggest(BalanceManager.getKnownAccountNames(ctx.getSource().getServer()), builder)
                            )
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    String account = StringArgumentType.getString(ctx, "account");
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                    if (account.equalsIgnoreCase("treasury") || account.equalsIgnoreCase("server")) {
                                        TreasuryManager.addTreasury(amount);
                                        ScoreboardManager.update(ctx.getSource().getServer());
                                        ctx.getSource().sendSuccess(() -> Component.literal("§aAdded $" + amount + " to Treasury"), false);
                                        return 1;
                                    }

                                    Optional<UUID> target = BalanceManager.resolveKnownPlayer(ctx.getSource().getServer(), account);
                                    if (target.isEmpty()) {
                                        ctx.getSource().sendFailure(Component.literal("§cOnly known players or Treasury are allowed."));
                                        return 0;
                                    }

                                    BalanceManager.addBalance(target.get(), amount);
                                    ScoreboardManager.update(ctx.getSource().getServer());
                                    ctx.getSource().sendSuccess(() -> Component.literal("§aAdded $" + amount + " to " + account), false);
                                    return 1;
                                }))))
                    .then(Commands.literal("removemoney")
                        .then(Commands.argument("account", StringArgumentType.word())
                            .suggests((ctx, builder) ->
                                SharedSuggestionProvider.suggest(BalanceManager.getKnownAccountNames(ctx.getSource().getServer()), builder)
                            )
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    String account = StringArgumentType.getString(ctx, "account");
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                    if (account.equalsIgnoreCase("treasury") || account.equalsIgnoreCase("server")) {
                                        if (!TreasuryManager.removeTreasury(amount)) {
                                            ctx.getSource().sendFailure(Component.literal("§cTreasury does not have enough money."));
                                            return 0;
                                        }
                                        ScoreboardManager.update(ctx.getSource().getServer());
                                        ctx.getSource().sendSuccess(() -> Component.literal("§aRemoved $" + amount + " from Treasury"), false);
                                        return 1;
                                    }

                                    Optional<UUID> target = BalanceManager.resolveKnownPlayer(ctx.getSource().getServer(), account);
                                    if (target.isEmpty()) {
                                        ctx.getSource().sendFailure(Component.literal("§cOnly known players or Treasury are allowed."));
                                        return 0;
                                    }

                                    if (!BalanceManager.removeBalance(target.get(), amount)) {
                                        ctx.getSource().sendFailure(Component.literal("§cAccount does not have enough money."));
                                        return 0;
                                    }

                                    ScoreboardManager.update(ctx.getSource().getServer());
                                    ctx.getSource().sendSuccess(() -> Component.literal("§aRemoved $" + amount + " from " + account), false);
                                    return 1;
                                }))))
                )
        );
    }

    private void onOpacAddonRegister(OPACServerAddonRegisterEvent event) {
        OpacBridge.registerClaimsTracker(event);
        System.out.println("[EconomyMC] OPAC tracker registered.");
    }

    private void onServerChat(ServerChatEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            if (ShopInputManager.handleChat(player, event.getRawText())) {
                event.setCanceled(true);
                return;
            }

            if (DraftInputManager.handleChat(player, event.getRawText())) {
                event.setCanceled(true);
                return;
            }

            if (JobsInputManager.handleChat(player, event.getRawText())) {
                event.setCanceled(true);
                return;
            }

            if (ChecksInputManager.handleChat(player, event.getRawText())) {
                event.setCanceled(true);
            }
        }
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BalanceManager.setBalance(player.getUUID(), BalanceManager.getBalance(player.getUUID()));
            JobManager.processExpiredJobs();
            ScoreboardManager.update(player.server);
        }
    }

    private void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ScoreboardManager.update(player.server);
        }
    }

    private void onServerStarted(ServerStartedEvent event) {
        TreasuryManager.getTreasury();
        JobManager.processExpiredJobs();
        ScoreboardManager.update(event.getServer());
    }
}