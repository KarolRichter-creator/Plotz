package de.karl_der_iii.economymc;

import com.mojang.brigadier.CommandDispatcher;
import de.karl_der_iii.economymc.service.ServerModeConfirmManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.karl_der_iii.economymc.menu.PlotzMainMenu;
import de.karl_der_iii.economymc.service.BalanceManager;
import de.karl_der_iii.economymc.service.AdminSettingsManager;
import de.karl_der_iii.economymc.service.BankInputManager;
import de.karl_der_iii.economymc.service.CapitalAreaManager;
import de.karl_der_iii.economymc.service.ChecksInputManager;
import de.karl_der_iii.economymc.service.DraftInputManager;
import de.karl_der_iii.economymc.service.JobManager;
import de.karl_der_iii.economymc.service.JobsInputManager;
import de.karl_der_iii.economymc.service.LanguageManager;
import de.karl_der_iii.economymc.service.OpacBridge;
import de.karl_der_iii.economymc.service.PayInputManager;
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

import java.util.Optional;
import java.util.UUID;

@Mod(PlotzMod.MOD_ID)
public class PlotzMod {
    public static final String MOD_ID = "plotz";

    public PlotzMod(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
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
                        ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                        return 0;
                    }
                    PlotzMainMenu.open(player);
                    return 1;
                })
                .then(
                    Commands.literal("admin")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("pos1").executes(ctx -> {
                            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                                return 0;
                            }
                            BlockPos pos = player.blockPosition();
                            CapitalAreaManager.setPos1(pos);
                            ctx.getSource().sendSuccess(() -> Component.literal("§aCapital pos1 set to: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);
                            return 1;
                        }))
                        .then(Commands.literal("pos2").executes(ctx -> {
                            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                                return 0;
                            }
                            BlockPos pos = player.blockPosition();
                            CapitalAreaManager.setPos2(pos);
                            ctx.getSource().sendSuccess(() -> Component.literal("§aCapital pos2 set to: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);
                            return 1;
                        }))
                        .then(Commands.literal("setcapital").executes(ctx -> {
                            if (!CapitalAreaManager.canCreateArea()) {
                                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("admin.capital.pos_required")));
                                return 0;
                            }
                            CapitalAreaManager.applyArea();
                            ctx.getSource().sendSuccess(() -> Component.literal(LanguageManager.tr("admin.capital.saved")), false);
                            return 1;
                        }))
                        .then(Commands.literal("clearcapital").executes(ctx -> {
                            CapitalAreaManager.clearArea();
                            ctx.getSource().sendSuccess(() -> Component.literal(LanguageManager.tr("admin.capital.cleared")), false);
                            return 1;
                        }))
                        .then(
                            Commands.literal("setmoney")
                                .then(Commands.argument("account", StringArgumentType.word())
                                    .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(BalanceManager.getKnownAccountNames(ctx.getSource().getServer()), builder))
                                    .then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(ctx -> {
                                        String account = StringArgumentType.getString(ctx, "account");
                                        long amount = IntegerArgumentType.getInteger(ctx, "amount");
                                        Optional<UUID> targetOpt = BalanceManager.resolveKnownAccount(ctx.getSource().getServer(), account);
                                        if (targetOpt.isEmpty()) {
                                            ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("admin.money.unknown")));
                                            return 0;
                                        }
                                        UUID target = targetOpt.get();
                                        BalanceManager.setBalance(target, amount);
                                        ScoreboardManager.update(ctx.getSource().getServer());
                                        ctx.getSource().sendSuccess(() -> Component.literal("§aSet balance of " + BalanceManager.resolveDisplayName(ctx.getSource().getServer(), target) + " to $" + amount + "."), false);
                                        return 1;
                                    })))
                        )
                        .then(
                            Commands.literal("addmoney")
                                .then(Commands.argument("account", StringArgumentType.word())
                                    .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(BalanceManager.getKnownAccountNames(ctx.getSource().getServer()), builder))
                                    .then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
                                        String account = StringArgumentType.getString(ctx, "account");
                                        long amount = IntegerArgumentType.getInteger(ctx, "amount");
                                        Optional<UUID> targetOpt = BalanceManager.resolveKnownAccount(ctx.getSource().getServer(), account);
                                        if (targetOpt.isEmpty()) {
                                            ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("admin.money.unknown")));
                                            return 0;
                                        }
                                        UUID target = targetOpt.get();
                                        BalanceManager.addBalance(target, amount);
                                        ScoreboardManager.update(ctx.getSource().getServer());
                                        ctx.getSource().sendSuccess(() -> Component.literal("§aAdded $" + amount + " to " + BalanceManager.resolveDisplayName(ctx.getSource().getServer(), target) + "."), false);
                                        return 1;
                                    })))
                        )
                        .then(
                            Commands.literal("removemoney")
                                .then(Commands.argument("account", StringArgumentType.word())
                                    .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(BalanceManager.getKnownAccountNames(ctx.getSource().getServer()), builder))
                                    .then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
                                        String account = StringArgumentType.getString(ctx, "account");
                                        long amount = IntegerArgumentType.getInteger(ctx, "amount");
                                        Optional<UUID> targetOpt = BalanceManager.resolveKnownAccount(ctx.getSource().getServer(), account);
                                        if (targetOpt.isEmpty()) {
                                            ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("admin.money.unknown")));
                                            return 0;
                                        }
                                        UUID target = targetOpt.get();
                                        if (!BalanceManager.removeBalance(target, amount)) {
                                            ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("admin.money.not_enough_account")));
                                            return 0;
                                        }
                                        ScoreboardManager.update(ctx.getSource().getServer());
                                        ctx.getSource().sendSuccess(() -> Component.literal("§aRemoved $" + amount + " from " + BalanceManager.resolveDisplayName(ctx.getSource().getServer(), target) + "."), false);
                                        return 1;
                                    })))
                        )
                )
        );
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
                return;
            }
            if (PayInputManager.handleChat(player, event.getRawText())) {
                event.setCanceled(true);
                return;
            }
            if (BankInputManager.handleChat(player, event.getRawText())) {
                event.setCanceled(true);
            }
            if (ServerModeConfirmManager.handleChat(player, event.getRawText())) {
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

        if (!OpacBridge.isInstalled()) {
            AdminSettingsManager.setPlotMarketEnabled(false);
        }

        ScoreboardManager.update(event.getServer());
    }
}