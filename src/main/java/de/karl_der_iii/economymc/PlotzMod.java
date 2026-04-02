package de.karl_der_iii.economymc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.karl_der_iii.economymc.menu.PlotzAdminModeMenu;
import de.karl_der_iii.economymc.menu.PlotzBankMenu;
import de.karl_der_iii.economymc.menu.PlotzChecksMenu;
import de.karl_der_iii.economymc.menu.PlotzHistoryMenu;
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
import de.karl_der_iii.economymc.service.LanguageManager;
import de.karl_der_iii.economymc.service.LoanManager;
import de.karl_der_iii.economymc.service.OpacBridge;
import de.karl_der_iii.economymc.service.ScoreboardManager;
import de.karl_der_iii.economymc.service.ShopInputManager;
import de.karl_der_iii.economymc.service.TransactionHistoryManager;
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

import java.util.List;
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

    private void sendHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("help.header")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("help.plots")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("help.shop")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("help.jobs")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("help.checks")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("help.history")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("help.bank")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("help.daily")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("help.pay")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("help.servermode")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("help.adminmode")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("help.admin")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("help.language")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("bank.command.list")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("bank.command.request")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("bank.command.offer")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("bank.command.accept")), false);
        source.sendSuccess(() -> Component.literal(LanguageManager.tr("bank.command.repay")), false);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        var ec = Commands.literal("ec")
            .requires(source -> source.hasPermission(0))
            .executes(ctx -> {
                sendHelp(ctx.getSource());
                return 1;
            });

        ec.then(Commands.literal("help").executes(ctx -> {
            sendHelp(ctx.getSource());
            return 1;
        }));

        ec.then(Commands.literal("plots").executes(ctx -> {
            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                return 0;
            }
            PlotzMainMenu.open(player);
            return 1;
        }));

        ec.then(Commands.literal("history").executes(ctx -> {
            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                return 0;
            }
            PlotzHistoryMenu.open(player, false);
            return 1;
        }));

        var bank = Commands.literal("bank")
            .executes(ctx -> {
                if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                    ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                    return 0;
                }
                PlotzBankMenu.open(player);
                return 1;
            });

        bank.then(Commands.literal("list").executes(ctx -> {
            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                return 0;
            }

            List<LoanManager.LoanEntry> loans = LoanManager.getVisibleLoans(player.getUUID(), player.hasPermissions(2));
            if (loans.isEmpty()) {
                player.sendSystemMessage(Component.literal(LanguageManager.tr("history.empty")));
                return 1;
            }

            for (LoanManager.LoanEntry loan : loans) {
                player.sendSystemMessage(Component.literal(
                    "§e#" + loan.id()
                        + " §7| $" + loan.principal()
                        + " | " + loan.status().name()
                        + " | borrower: " + loan.borrowerName()
                        + " | lender: " + (loan.lenderName().isBlank() ? LanguageManager.tr("bank.target.server") : loan.lenderName())
                ));
            }
            return 1;
        }));

        bank.then(
            Commands.literal("request")
                .then(
                    Commands.literal("server")
                        .then(
                            Commands.argument("amount", IntegerArgumentType.integer(1))
                                .then(
                                    Commands.argument("days", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                                                return 0;
                                            }

                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            int days = IntegerArgumentType.getInteger(ctx, "days");
                                            LoanManager.createRequest(
                                                player.getUUID(),
                                                player.getGameProfile().getName(),
                                                LoanManager.LoanTargetType.SERVER,
                                                null,
                                                "",
                                                amount,
                                                days
                                            );
                                            player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.request.created")));
                                            return 1;
                                        })
                                )
                        )
                )
        );

        bank.then(
            Commands.literal("request")
                .then(
                    Commands.literal("all")
                        .then(
                            Commands.argument("amount", IntegerArgumentType.integer(1))
                                .then(
                                    Commands.argument("days", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                                                return 0;
                                            }

                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            int days = IntegerArgumentType.getInteger(ctx, "days");
                                            LoanManager.createRequest(
                                                player.getUUID(),
                                                player.getGameProfile().getName(),
                                                LoanManager.LoanTargetType.ALL_PLAYERS,
                                                null,
                                                "",
                                                amount,
                                                days
                                            );
                                            player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.request.created")));
                                            return 1;
                                        })
                                )
                        )
                )
        );

        bank.then(
            Commands.literal("request")
                .then(
                    Commands.literal("player")
                        .then(
                            Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) ->
                                    SharedSuggestionProvider.suggest(
                                        BalanceManager.getKnownAccountNames(ctx.getSource().getServer()),
                                        builder
                                    )
                                )
                                .then(
                                    Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .then(
                                            Commands.argument("days", IntegerArgumentType.integer(1))
                                                .executes(ctx -> {
                                                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                                        ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                                                        return 0;
                                                    }

                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    Optional<UUID> target = BalanceManager.resolveKnownPlayer(player.server, name);
                                                    if (target.isEmpty()) {
                                                        ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("bank.invalid_target")));
                                                        return 0;
                                                    }

                                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                                    int days = IntegerArgumentType.getInteger(ctx, "days");
                                                    LoanManager.createRequest(
                                                        player.getUUID(),
                                                        player.getGameProfile().getName(),
                                                        LoanManager.LoanTargetType.SPECIFIC_PLAYER,
                                                        target.get(),
                                                        name,
                                                        amount,
                                                        days
                                                    );
                                                    player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.request.created")));
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
        );

        bank.then(
            Commands.literal("offer")
                .then(
                    Commands.argument("loanId", StringArgumentType.word())
                        .then(
                            Commands.argument("interestPercent", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                        ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                                        return 0;
                                    }

                                    String loanId = StringArgumentType.getString(ctx, "loanId");
                                    int interestPercent = IntegerArgumentType.getInteger(ctx, "interestPercent");

                                    boolean ok = LoanManager.makeOffer(
                                        loanId,
                                        player.getUUID(),
                                        player.getGameProfile().getName(),
                                        interestPercent
                                    );
                                    if (!ok) {
                                        ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("bank.not_found")));
                                        return 0;
                                    }

                                    player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.offer.created")));
                                    return 1;
                                })
                        )
                )
        );

        bank.then(
            Commands.literal("accept")
                .then(
                    Commands.argument("loanId", StringArgumentType.word())
                        .executes(ctx -> {
                            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                                return 0;
                            }

                            String loanId = StringArgumentType.getString(ctx, "loanId");
                            boolean ok = LoanManager.acceptOffer(loanId);
                            if (!ok) {
                                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("bank.not_found")));
                                return 0;
                            }

                            player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.accepted")));
                            ScoreboardManager.update(player.server);
                            return 1;
                        })
                )
        );

        bank.then(
            Commands.literal("repay")
                .then(
                    Commands.argument("loanId", StringArgumentType.word())
                        .executes(ctx -> {
                            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                                return 0;
                            }

                            String loanId = StringArgumentType.getString(ctx, "loanId");
                            boolean ok = LoanManager.repayLoan(loanId, player.getUUID());
                            if (!ok) {
                                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("bank.not_enough_money")));
                                return 0;
                            }

                            player.sendSystemMessage(Component.literal(LanguageManager.tr("bank.repaid")));
                            ScoreboardManager.update(player.server);
                            return 1;
                        })
                )
        );

        ec.then(bank);

        ec.then(Commands.literal("shop").executes(ctx -> {
            if (!AdminSettingsManager.shopEnabled()) {
                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("msg.shop_disabled")));
                return 0;
            }
            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                return 0;
            }
            PlotzShopMenu.open(player);
            return 1;
        }));

        ec.then(Commands.literal("jobs").executes(ctx -> {
            if (!AdminSettingsManager.jobsEnabled()) {
                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("msg.jobs_disabled")));
                return 0;
            }
            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                return 0;
            }
            PlotzJobsMenu.open(player, 0, true, false);
            return 1;
        }));

        ec.then(Commands.literal("checks").executes(ctx -> {
            if (!AdminSettingsManager.checksEnabled()) {
                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("msg.checks_disabled")));
                return 0;
            }
            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                return 0;
            }
            PlotzChecksMenu.open(player, 0);
            return 1;
        }));

        ec.then(
            Commands.literal("servermode")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    if (!AdminSettingsManager.serverModeEnabled()) {
                        ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("msg.servermode_disabled")));
                        return 0;
                    }
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                        return 0;
                    }
                    PlotzServerModeMenu.open(player);
                    return 1;
                })
        );

        ec.then(
            Commands.literal("adminmode")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                        return 0;
                    }
                    PlotzAdminModeMenu.open(player);
                    return 1;
                })
        );

        ec.then(Commands.literal("daily").executes(ctx -> {
            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                return 0;
            }

            if (!DailyRewardManager.canClaim(player.getUUID())) {
                long remaining = DailyRewardManager.getRemainingMs(player.getUUID()) / 1000L;
                long hours = remaining / 3600L;
                long minutes = (remaining % 3600L) / 60L;
                player.sendSystemMessage(Component.literal(LanguageManager.format("daily.already", hours, minutes)));
                return 0;
            }

            BalanceManager.addBalance(player.getUUID(), 100);
            TransactionHistoryManager.add(player.getUUID(), LanguageManager.format("history.daily", 100));
            ScoreboardManager.update(player.server);
            DailyRewardManager.markClaimed(player.getUUID());
            player.sendSystemMessage(Component.literal(LanguageManager.tr("daily.claimed")));
            return 1;
        }));

        ec.then(
            Commands.literal("pay")
                .then(
                    Commands.argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) ->
                            SharedSuggestionProvider.suggest(
                                BalanceManager.getKnownAccountNames(ctx.getSource().getServer()),
                                builder
                            )
                        )
                        .then(
                            Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer sender)) {
                                        ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                                        return 0;
                                    }

                                    String name = StringArgumentType.getString(ctx, "player");
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                    Optional<UUID> resolved = BalanceManager.resolveKnownAccount(sender.server, name);
                                    if (resolved.isEmpty()) {
                                        ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("pay.known_or_treasury")));
                                        return 0;
                                    }

                                    UUID targetId = resolved.get();
                                    if (targetId.equals(sender.getUUID())) {
                                        ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("pay.self")));
                                        return 0;
                                    }

                                    if (!BalanceManager.removeBalance(sender.getUUID(), amount)) {
                                        ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("pay.not_enough")));
                                        return 0;
                                    }

                                    BalanceManager.addBalance(targetId, amount);
                                    ScoreboardManager.update(sender.server);

                                    String targetName = BalanceManager.resolveDisplayName(sender.server, targetId);
                                    sender.sendSystemMessage(Component.literal(LanguageManager.format("pay.sent", amount, targetName)));

                                    TransactionHistoryManager.add(
                                        sender.getUUID(),
                                        LanguageManager.format("history.pay.sent", targetName, amount)
                                    );

                                    if (BalanceManager.TREASURY_ACCOUNT_ID.equals(targetId)) {
                                        TransactionHistoryManager.addTreasury(
                                            LanguageManager.format("history.pay.received", sender.getGameProfile().getName(), amount)
                                        );
                                    } else {
                                        TransactionHistoryManager.add(
                                            targetId,
                                            LanguageManager.format("history.pay.received", sender.getGameProfile().getName(), amount)
                                        );
                                    }

                                    ServerPlayer onlineTarget = sender.server.getPlayerList().getPlayer(targetId);
                                    if (onlineTarget != null) {
                                        onlineTarget.sendSystemMessage(Component.literal(
                                            LanguageManager.format("pay.received", amount, sender.getGameProfile().getName())
                                        ));
                                    }

                                    return 1;
                                })
                        )
                )
        );

        ec.then(
            Commands.literal("admin")
                .requires(source -> source.hasPermission(2))
                .then(
                    Commands.literal("pos1")
                        .executes(ctx -> {
                            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                                return 0;
                            }
                            BlockPos pos = player.blockPosition();
                            CapitalAreaManager.setPos1(pos);
                            ctx.getSource().sendSuccess(
                                () -> Component.literal(LanguageManager.format("admin.pos1_set", pos.getX(), pos.getY(), pos.getZ())),
                                false
                            );
                            return 1;
                        })
                )
                .then(
                    Commands.literal("pos2")
                        .executes(ctx -> {
                            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("cmd.only_players")));
                                return 0;
                            }
                            BlockPos pos = player.blockPosition();
                            CapitalAreaManager.setPos2(pos);
                            ctx.getSource().sendSuccess(
                                () -> Component.literal(LanguageManager.format("admin.pos2_set", pos.getX(), pos.getY(), pos.getZ())),
                                false
                            );
                            return 1;
                        })
                )
                .then(
                    Commands.literal("setcapital")
                        .executes(ctx -> {
                            if (!CapitalAreaManager.canCreateArea()) {
                                ctx.getSource().sendFailure(Component.literal(LanguageManager.tr("admin.pos_missing")));
                                return 0;
                            }
                            CapitalAreaManager.applyArea();
                            ctx.getSource().sendSuccess(() -> Component.literal(LanguageManager.tr("admin.capital_saved")), false);
                            return 1;
                        })
                )
                .then(
                    Commands.literal("clearcapital")
                        .executes(ctx -> {
                            CapitalAreaManager.clearArea();
                            ctx.getSource().sendSuccess(() -> Component.literal(LanguageManager.tr("admin.capital_cleared")), false);
                            return 1;
                        })
                )
        );

        dispatcher.register(ec);
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