package de.karl_der_iii.economymc.service;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ServerShopManager {
    public enum Category {
        TOOLS("server.shop.category.tools", Items.IRON_PICKAXE),
        WEAPONS("server.shop.category.weapons", Items.IRON_SWORD),
        ARMOR("server.shop.category.armor", Items.IRON_CHESTPLATE),
        BLOCKS("server.shop.category.blocks", Items.STONE),
        WOOD("server.shop.category.wood", Items.OAK_PLANKS),
        STONE("server.shop.category.stone", Items.COBBLESTONE),
        ORES("server.shop.category.ores", Items.IRON_INGOT),
        FOOD("server.shop.category.food", Items.BREAD),
        FARMING("server.shop.category.farming", Items.WHEAT),
        REDSTONE("server.shop.category.redstone", Items.REDSTONE),
        DECORATION("server.shop.category.decoration", Items.LANTERN),
        NETHER("server.shop.category.nether", Items.NETHERRACK),
        END("server.shop.category.end", Items.END_STONE),
        MOB_DROPS("server.shop.category.mobdrops", Items.STRING),
        MISC("server.shop.category.misc", Items.CHEST);

        private final String translationKey;
        private final Item icon;

        Category(String translationKey, Item icon) {
            this.translationKey = translationKey;
            this.icon = icon;
        }

        public String translationKey() {
            return translationKey;
        }

        public Item icon() {
            return icon;
        }
    }

    public record Entry(Item item, Category category, int basePrice) {
        public String itemId() {
            return BuiltInRegistries.ITEM.getKey(item).toString();
        }
    }

    private ServerShopManager() {
    }

    public static List<Category> categories() {
        return List.of(Category.values());
    }

    public static List<Entry> getEntries(Category category) {
        List<Entry> result = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;

            Category found = classify(item);
            if (found != category) continue;

            result.add(new Entry(item, found, computeBasePrice(item, found)));
        }

        result.sort(Comparator.comparing(entry -> displayName(entry).toLowerCase(Locale.ROOT)));
        return result;
    }

    public static String displayName(Entry entry) {
        return entry.item().getDefaultInstance().getHoverName().getString();
    }

    public static int tax(Entry entry) {
        return TreasuryManager.calculateTax(entry.basePrice());
    }

    public static int total(Entry entry) {
        return TreasuryManager.calculateTotalWithTax(entry.basePrice());
    }

    public static boolean buy(ServerPlayer player, Entry entry) {
        int total = total(entry);
        if (!BalanceManager.removeBalance(player.getUUID(), total)) {
            return false;
        }

        ItemStack stack = entry.item().getDefaultInstance().copy();
        if (!player.getInventory().add(stack.copy())) {
            player.drop(stack.copy(), false);
        }

        TreasuryManager.addTreasury(total);
        TransactionHistoryManager.add(
            player.getUUID(),
            LanguageManager.format("history.server_shop.buy", displayName(entry), total)
        );
        return true;
    }

    private static Category classify(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();

        if (matches(path, "sword", "bow", "crossbow", "trident", "mace", "arrow", "spectral_arrow", "tipped_arrow")) {
            return Category.WEAPONS;
        }

        if (matches(path, "helmet", "chestplate", "leggings", "boots", "shield", "elytra", "horse_armor", "turtle_helmet")) {
            return Category.ARMOR;
        }

        if (matches(path, "pickaxe", "axe", "shovel", "hoe", "shears", "fishing_rod", "flint_and_steel", "compass", "clock", "spyglass", "brush", "lead", "name_tag", "bucket")) {
            return Category.TOOLS;
        }

        if (isFood(item, path)) {
            return Category.FOOD;
        }

        if (matches(path, "seed", "wheat", "carrot", "potato", "beetroot", "melon", "pumpkin", "sugar_cane", "cocoa_beans", "nether_wart", "bone_meal", "egg")) {
            return Category.FARMING;
        }

        if (matches(path, "redstone", "repeater", "comparator", "observer", "piston", "hopper", "lever", "button", "pressure_plate", "tripwire", "daylight_detector", "target", "rail", "noteblock", "dispenser", "dropper", "crafter")) {
            return Category.REDSTONE;
        }

        if (matches(path, "ender", "chorus", "purpur", "shulker", "dragon_breath", "end_crystal", "end_stone")) {
            return Category.END;
        }

        if (matches(path, "netherrack", "nether", "basalt", "blackstone", "quartz", "glowstone", "soul_", "crimson", "warped", "blaze", "ghast", "magma")) {
            return Category.NETHER;
        }

        if (matches(path, "string", "bone", "gunpowder", "rotten_flesh", "spider_eye", "ender_pearl", "slime", "feather", "leather", "rabbit_foot", "phantom_membrane", "prismarine", "ink_sac", "nautilus_shell", "scute", "snowball", "honeycomb")) {
            return Category.MOB_DROPS;
        }

        if (matches(path, "ingot", "nugget", "diamond", "emerald", "lapis", "amethyst", "coal", "raw_", "netherite", "copper")) {
            return Category.ORES;
        }

        if (item instanceof BlockItem) {
            if (isWood(path)) return Category.WOOD;
            if (isStone(path)) return Category.STONE;
            if (isDecoration(path)) return Category.DECORATION;
            return Category.BLOCKS;
        }

        return Category.MISC;
    }

    private static boolean isFood(Item item, String path) {
        if (item.components().has(DataComponents.FOOD)) {
            return true;
        }
        return matches(path,
            "bread", "beef", "chicken", "mutton", "porkchop", "rabbit", "cod", "salmon",
            "potato", "carrot", "apple", "cookie", "cake", "pie", "stew", "soup",
            "melon_slice", "berries", "kelp", "golden_carrot", "golden_apple", "honey_bottle"
        );
    }

    private static boolean isWood(String path) {
        return matches(path,
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove",
            "cherry", "bamboo", "crimson", "warped"
        );
    }

    private static boolean isStone(String path) {
        return matches(path,
            "stone", "cobble", "deepslate", "tuff", "andesite", "granite",
            "diorite", "calcite", "dripstone", "brick", "slate"
        );
    }

    private static boolean isDecoration(String path) {
        return matches(path,
            "lantern", "candle", "banner", "carpet", "flower_pot", "painting", "item_frame",
            "bookshelf", "bed", "sign", "skull", "head", "coral", "terracotta",
            "concrete", "wool", "stained_glass", "glass_pane", "torch", "chain"
        );
    }

    private static boolean matches(String path, String... parts) {
        for (String part : parts) {
            if (path.contains(part)) return true;
        }
        return false;
    }

    private static int computeBasePrice(Item item, Category category) {
        int base;

        int maxStack = item.getDefaultMaxStackSize();
        if (maxStack <= 1) {
            base = 220;
        } else if (maxStack <= 16) {
            base = 90;
        } else {
            base = 35;
        }

        base += switch (category) {
            case WEAPONS -> 90;
            case ARMOR -> 110;
            case TOOLS -> 70;
            case ORES -> 80;
            case REDSTONE -> 40;
            case NETHER -> 60;
            case END -> 100;
            case MOB_DROPS -> 35;
            case FOOD -> 20;
            case FARMING -> 15;
            case DECORATION -> 30;
            case WOOD -> 18;
            case STONE -> 22;
            case BLOCKS -> 25;
            case MISC -> 28;
        };

        Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(item);
        if (holder.is(ItemTags.PIGLIN_LOVED)) {
            base += 30;
        }

        Rarity rarity = item.getDefaultInstance().getRarity();
        base += switch (rarity) {
            case COMMON -> 0;
            case UNCOMMON -> 25;
            case RARE -> 60;
            case EPIC -> 120;
        };

        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        if (matches(path, "diamond", "netherite", "elytra", "shulker", "totem")) {
            base += 180;
        }

        return Math.max(5, base);
    }
}