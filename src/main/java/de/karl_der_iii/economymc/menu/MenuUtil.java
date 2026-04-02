package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.data.PlotzStore;
import de.karl_der_iii.economymc.service.BalanceManager;
import de.karl_der_iii.economymc.service.LanguageManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.ArrayList;
import java.util.List;

public final class MenuUtil {
    private MenuUtil() {}

    public static ItemStack named(net.minecraft.world.item.Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    public static ItemStack named(net.minecraft.world.item.Item item, String name, List<String> loreLines) {
        ItemStack stack = named(item, name);
        List<Component> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(Component.literal(line));
        }
        stack.set(DataComponents.LORE, new ItemLore(lore));
        return stack;
    }

    public static ItemStack playerInfoHead(ServerPlayer player) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
        head.set(DataComponents.CUSTOM_NAME, Component.literal("§e" + player.getGameProfile().getName()));

        long balance = BalanceManager.getBalance(player.getUUID());
        int normalCredits = PlotzStore.getNormalCredits(player.getUUID());
        int capitalCredits = PlotzStore.getCapitalCredits(player.getUUID());

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(LanguageManager.format("menu.player.balance", balance)));
        lore.add(Component.literal(LanguageManager.format("menu.player.normal", normalCredits)));
        lore.add(Component.literal(LanguageManager.format("menu.player.capital", capitalCredits)));
        head.set(DataComponents.LORE, new ItemLore(lore));

        return head;
    }

    public static void putPlayerInfoHead(SimpleContainer box, ServerPlayer player, int slot) {
        box.setItem(slot, playerInfoHead(player));
    }
}