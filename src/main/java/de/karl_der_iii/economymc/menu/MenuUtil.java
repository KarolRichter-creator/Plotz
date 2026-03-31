package de.karl_der_iii.economymc.menu;

import de.karl_der_iii.economymc.data.PlotzStore;
import de.karl_der_iii.economymc.service.EconomyBridge;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.List;

public final class MenuUtil {
    private MenuUtil() {}

    public static ItemStack named(Item item, String text) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(text));
        return stack;
    }

    public static ItemStack playerInfoHead(ServerPlayer player) {
        long balance = EconomyBridge.getBalance(player);
        int normalCredits = PlotzStore.getNormalCredits(player.getUUID());
        int capitalCredits = PlotzStore.getCapitalCredits(player.getUUID());

        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

        stack.set(DataComponents.CUSTOM_NAME, Component.literal("§e" + player.getGameProfile().getName()));
        stack.set(
            DataComponents.LORE,
            new ItemLore(List.of(
                Component.literal("§6Balance: $" + balance),
                Component.literal("§7Normal Credits: " + normalCredits),
                Component.literal("§7Capital Credits: " + capitalCredits)
            ))
        );
        stack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));

        return stack;
    }

    public static void putPlayerInfoHead(SimpleContainer box, ServerPlayer player, int slot) {
        box.setItem(slot, playerInfoHead(player));
    }
}