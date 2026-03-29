package de.karol.plotz;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.karol.plotz.menu.PlotzJobsMenu;
import de.karol.plotz.menu.PlotzMainMenu;
import de.karol.plotz.menu.PlotzServerModeMenu;
import de.karol.plotz.menu.PlotzShopMenu;
import de.karol.plotz.service.BalanceManager;
import de.karol.plotz.service.CapitalAreaManager;
import de.karol.plotz.service.DailyRewardManager;
import de.karol.plotz.service.DraftInputManager;
import de.karol.plotz.service.JobManager;
import de.karol.plotz.service.JobsInputManager;
import de.karol.plotz.service.OpacBridge;
import de.karol.plotz.service.ScoreboardManager;
import de.karol.plotz.service.ShopInputManager;
import de.karol.plotz.service.TreasuryManager;
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
        System.out.println("[Plotz] Mod gestartet.");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("plotz")
                .requires(source -> source.hasPermission(0))
                .executes(ctx -> {
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal("Only players can use /plotz."));
                        return 0;
                    }

                    PlotzMainMenu.open(player);
                    return 1;
                })
        );

        dispatcher.register(
            Commands.literal("shop")
                .requires(source -> source.hasPermission(0))
                .executes(ctx -> {
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal("Only players can use /shop."));
                        return 0;
                    }

                    PlotzShopMenu.open(player);
                    return 1;
                })
        );

        dispatcher.register(
            Commands.literal("jobs")
                .requires(source -> source.hasPermission(0))
                .executes(ctx -> {
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal("Only players can use /jobs."));
                        return 0;
                    }

                    PlotzJobsMenu.open(player, 0, true, false);
                    return 1;
                })
        );

        dispatcher.register(
            Commands.literal("plotzservermode")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal("Only players can use /plotzservermode."));
                        return 0;
                    }

                    PlotzServerModeMenu.open(player);
                    return 1;
                })
        );

        dispatcher.register(
            Commands.literal("daily")
                .requires(source -> source.hasPermission(0))
                .executes(ctx -> {
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal("Only players can use /daily."));
                        return 0;
                    }

                    if (!DailyRewardManager.canClaim(player.getUUID())) {
                        long remaining = DailyRewardManager.getRemainingMs(player.getUUID()) / 1000L;
                        long hours = remaining / 3600L;
                        long minutes = (remaining % 3600L) / 60L;
                        player.sendSystemMessage(Component.literal("§cDaily already claimed. Come back in " + hours + "h " + minutes + "m."));
                        return 0;
                    }

                    BalanceManager.addBalance(player.getUUID(), 100);
                    ScoreboardManager.update(player.server);
                    DailyRewardManager.markClaimed(player.getUUID());
                    player.sendSystemMessage(Component.literal("§aYou claimed your daily $100."));
                    return 1;
                })
        );

        dispatcher.register(
            Commands.literal("pay")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("player", StringArgumentType.word())
                    .suggests((ctx, builder) ->
                        SharedSuggestionProvider.suggest(BalanceManager.getKnownAccountNames(ctx.getSource().getServer()), builder)
                    )
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            if (!(ctx.getSource().getEntity() instanceof ServerPlayer sender)) {
                                ctx.getSource().sendFailure(Component.literal("Only players can use /pay."));
                                return 0;
                            }

                            String name = StringArgumentType.getString(ctx, "player");
                            int amount = IntegerArgumentType.getInteger(ctx, "amount");

                            Optional<UUID> resolved = BalanceManager.resolveKnownPlayer(sender.server, name);
                            if (resolved.isEmpty()) {
                                ctx.getSource().sendFailure(Component.literal("§cOnly players who already joined this world can receive money."));
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

                            sender.sendSystemMessage(Component.literal("§aYou paid $" + amount + " to " + name + "."));
                            ServerPlayer onlineTarget = sender.server.getPlayerList().getPlayer(targetId);
                            if (onlineTarget != null) {
                                onlineTarget.sendSystemMessage(Component.literal("§aYou received $" + amount + " from " + sender.getGameProfile().getName() + "."));
                            }

                            return 1;
                        })))
        );

        dispatcher.register(
            Commands.literal("plotzadmin")
                .requires(source -> source.hasPermission(2))
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
                        ctx.getSource().sendFailure(Component.literal("§cSet /plotzadmin pos1 and /plotzadmin pos2 first."));
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

                                if (account.equalsIgnoreCase("server")) {
                                    TreasuryManager.setTreasury(amount);
                                    ScoreboardManager.update(ctx.getSource().getServer());
                                    ctx.getSource().sendSuccess(() -> Component.literal("§aSet Treasury to $" + amount), false);
                                    return 1;
                                }

                                Optional<UUID> target = BalanceManager.resolveKnownPlayer(ctx.getSource().getServer(), account);
                                if (target.isEmpty()) {
                                    ctx.getSource().sendFailure(Component.literal("§cOnly known players or Server are allowed."));
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

                                if (account.equalsIgnoreCase("server")) {
                                    TreasuryManager.addTreasury(amount);
                                    ScoreboardManager.update(ctx.getSource().getServer());
                                    ctx.getSource().sendSuccess(() -> Component.literal("§aAdded $" + amount + " to Treasury"), false);
                                    return 1;
                                }

                                Optional<UUID> target = BalanceManager.resolveKnownPlayer(ctx.getSource().getServer(), account);
                                if (target.isEmpty()) {
                                    ctx.getSource().sendFailure(Component.literal("§cOnly known players or Server are allowed."));
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

                                if (account.equalsIgnoreCase("server")) {
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
                                    ctx.getSource().sendFailure(Component.literal("§cOnly known players or Server are allowed."));
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
        );
    }

    private void onOpacAddonRegister(OPACServerAddonRegisterEvent event) {
        OpacBridge.registerClaimsTracker(event);
        System.out.println("[Plotz] OPAC tracker registered.");
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