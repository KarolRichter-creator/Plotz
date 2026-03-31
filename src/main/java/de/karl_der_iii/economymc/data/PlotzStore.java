package de.karl_der_iii.economymc.data;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PlotzStore {
    private static final ConcurrentHashMap<UUID, Integer> NORMAL_CREDITS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> CAPITAL_CREDITS = new ConcurrentHashMap<>();

    private static final CopyOnWriteArrayList<PlotEntry> OWNED_PLOTS = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Listing> LISTINGS = new CopyOnWriteArrayList<>();
    private static final ConcurrentHashMap<UUID, SaleDraft> SALE_DRAFTS = new ConcurrentHashMap<>();

    private static final CopyOnWriteArrayList<ShopListing> SHOP_LISTINGS = new CopyOnWriteArrayList<>();
    private static final ConcurrentHashMap<UUID, ShopDraft> SHOP_DRAFTS = new ConcurrentHashMap<>();

    private PlotzStore() {}

    public static int getNormalCredits(UUID playerId) {
        return NORMAL_CREDITS.getOrDefault(playerId, 0);
    }

    public static int getCapitalCredits(UUID playerId) {
        return CAPITAL_CREDITS.getOrDefault(playerId, 0);
    }

    public static void addNormalCredit(UUID playerId, int amount) {
        NORMAL_CREDITS.put(playerId, getNormalCredits(playerId) + amount);
    }

    public static void addCapitalCredit(UUID playerId, int amount) {
        CAPITAL_CREDITS.put(playerId, getCapitalCredits(playerId) + amount);
    }

    public static boolean spendNormalCredit(UUID playerId) {
        int current = getNormalCredits(playerId);
        if (current <= 0) return false;
        NORMAL_CREDITS.put(playerId, current - 1);
        return true;
    }

    public static boolean spendCapitalCredit(UUID playerId) {
        int current = getCapitalCredits(playerId);
        if (current <= 0) return false;
        CAPITAL_CREDITS.put(playerId, current - 1);
        return true;
    }

    public static void addOwnedPlot(PlotEntry plot) {
        OWNED_PLOTS.add(plot);
    }

    public static void upsertOwnedGroupedClaim(PlotEntry plot) {
        OWNED_PLOTS.removeIf(p ->
            p.ownerId().equals(plot.ownerId()) &&
            "Synced from Open Parties and Claims".equals(p.description()) &&
            p.location().equals(plot.location())
        );
        OWNED_PLOTS.add(plot);
    }

    public static void clearOwnedClaimsFor(UUID ownerId) {
        OWNED_PLOTS.removeIf(p ->
            p.ownerId().equals(ownerId) &&
            "Synced from Open Parties and Claims".equals(p.description())
        );
    }

    public static List<PlotEntry> getOwnedPlots(UUID ownerId) {
        List<PlotEntry> result = new ArrayList<>();
        for (PlotEntry plot : OWNED_PLOTS) {
            if (plot.ownerId().equals(ownerId)) result.add(plot);
        }
        return result;
    }

    public static PlotEntry getOwnedPlot(UUID ownerId, String location) {
        for (PlotEntry plot : OWNED_PLOTS) {
            if (plot.ownerId().equals(ownerId) && plot.location().equals(location)) return plot;
        }
        return null;
    }

    public static List<PlotEntry> getAllOwnedPlots() {
        return new ArrayList<>(OWNED_PLOTS);
    }

    public static List<Listing> getListings() {
        return new ArrayList<>(LISTINGS);
    }

    public static List<Listing> getListingsBySeller(UUID sellerId) {
        List<Listing> result = new ArrayList<>();
        for (Listing listing : LISTINGS) {
            if (listing.sellerId().equals(sellerId)) result.add(listing);
        }
        return result;
    }

    public static Listing getListingById(String listingId) {
        for (Listing listing : LISTINGS) {
            if (listing.listingId().equals(listingId)) return listing;
        }
        return null;
    }

    public static void addListing(Listing listing) {
        LISTINGS.add(listing);
    }

    public static void removeListing(String listingId) {
        LISTINGS.removeIf(l -> l.listingId().equals(listingId));
    }

    public static boolean hasListingForLocation(String location) {
        for (Listing listing : LISTINGS) {
            if (listing.location().equals(location)) return true;
        }
        return false;
    }

    public static void setDraft(SaleDraft draft) {
        SALE_DRAFTS.put(draft.ownerId(), draft);
    }

    public static SaleDraft getDraft(UUID playerId) {
        return SALE_DRAFTS.get(playerId);
    }

    public static void clearDraft(UUID playerId) {
        SALE_DRAFTS.remove(playerId);
    }

    public static boolean hasAnyDraftForLocation(String location) {
        for (SaleDraft draft : SALE_DRAFTS.values()) {
            if (draft.location().equals(location)) return true;
        }
        return false;
    }

    public static void updateDraftPrice(UUID playerId, int price) {
        SaleDraft draft = SALE_DRAFTS.get(playerId);
        if (draft == null) return;
        SALE_DRAFTS.put(playerId, new SaleDraft(
            draft.ownerId(), draft.ownerName(), draft.title(), draft.capital(), draft.chunkCount(),
            draft.location(), price, draft.description(), draft.builtOnPlot(), draft.justification(), draft.negotiable()
        ));
    }

    public static void updateDraftDescription(UUID playerId, String description) {
        SaleDraft draft = SALE_DRAFTS.get(playerId);
        if (draft == null) return;
        SALE_DRAFTS.put(playerId, new SaleDraft(
            draft.ownerId(), draft.ownerName(), draft.title(), draft.capital(), draft.chunkCount(),
            draft.location(), draft.price(), description, draft.builtOnPlot(), draft.justification(), draft.negotiable()
        ));
    }

    public static void updateDraftBuilt(UUID playerId, String builtOnPlot) {
        SaleDraft draft = SALE_DRAFTS.get(playerId);
        if (draft == null) return;
        SALE_DRAFTS.put(playerId, new SaleDraft(
            draft.ownerId(), draft.ownerName(), draft.title(), draft.capital(), draft.chunkCount(),
            draft.location(), draft.price(), draft.description(), builtOnPlot, draft.justification(), draft.negotiable()
        ));
    }

    public static void updateDraftJustification(UUID playerId, String justification) {
        SaleDraft draft = SALE_DRAFTS.get(playerId);
        if (draft == null) return;
        SALE_DRAFTS.put(playerId, new SaleDraft(
            draft.ownerId(), draft.ownerName(), draft.title(), draft.capital(), draft.chunkCount(),
            draft.location(), draft.price(), draft.description(), draft.builtOnPlot(), justification, draft.negotiable()
        ));
    }

    public static void updateDraftNegotiable(UUID playerId, boolean negotiable) {
        SaleDraft draft = SALE_DRAFTS.get(playerId);
        if (draft == null) return;
        SALE_DRAFTS.put(playerId, new SaleDraft(
            draft.ownerId(), draft.ownerName(), draft.title(), draft.capital(), draft.chunkCount(),
            draft.location(), draft.price(), draft.description(), draft.builtOnPlot(), draft.justification(), negotiable
        ));
    }

    public static List<ShopListing> getShopListings() {
        return new ArrayList<>(SHOP_LISTINGS);
    }

    public static void addShopListing(ShopListing listing) {
        SHOP_LISTINGS.add(listing);
    }

    public static ShopListing getShopListingById(String listingId) {
        for (ShopListing listing : SHOP_LISTINGS) {
            if (listing.listingId().equals(listingId)) return listing;
        }
        return null;
    }

    public static void removeShopListing(String listingId) {
        SHOP_LISTINGS.removeIf(l -> l.listingId().equals(listingId));
    }

    public static void setShopDraft(ShopDraft draft) {
        SHOP_DRAFTS.put(draft.ownerId(), draft);
    }

    public static ShopDraft getShopDraft(UUID playerId) {
        return SHOP_DRAFTS.get(playerId);
    }

    public static void clearShopDraft(UUID playerId) {
        SHOP_DRAFTS.remove(playerId);
    }

    public static void updateShopDraftPrice(UUID playerId, int price) {
        ShopDraft draft = SHOP_DRAFTS.get(playerId);
        if (draft == null) return;
        SHOP_DRAFTS.put(playerId, new ShopDraft(
            draft.ownerId(),
            draft.ownerName(),
            copyStacks(draft.items()),
            price
        ));
    }

    public static void updateShopDraftItems(UUID playerId, List<ItemStack> items) {
        ShopDraft draft = SHOP_DRAFTS.get(playerId);
        int price = draft == null ? 100 : draft.price();
        String ownerName = draft == null ? "Unknown" : draft.ownerName();
        SHOP_DRAFTS.put(playerId, new ShopDraft(
            playerId,
            ownerName,
            copyStacks(items),
            price
        ));
    }

    private static List<ItemStack> copyStacks(List<ItemStack> items) {
        List<ItemStack> copied = new ArrayList<>();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) copied.add(stack.copy());
        }
        return copied;
    }

    public record PlotEntry(
        UUID ownerId,
        String ownerName,
        String title,
        boolean capital,
        int chunkCount,
        String location,
        String description
    ) {}

    public record Listing(
        String listingId,
        UUID sellerId,
        String sellerName,
        String title,
        int price,
        boolean capital,
        int chunkCount,
        String location,
        String description,
        String justification,
        String builtOnPlot,
        boolean negotiable
    ) {}

    public record SaleDraft(
        UUID ownerId,
        String ownerName,
        String title,
        boolean capital,
        int chunkCount,
        String location,
        int price,
        String description,
        String builtOnPlot,
        String justification,
        boolean negotiable
    ) {}

    public record ShopListing(
        String listingId,
        UUID sellerId,
        String sellerName,
        List<ItemStack> items,
        int price
    ) {}

    public record ShopDraft(
        UUID ownerId,
        String ownerName,
        List<ItemStack> items,
        int price
    ) {}
}