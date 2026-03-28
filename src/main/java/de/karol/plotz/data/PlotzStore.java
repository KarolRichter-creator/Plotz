package de.karol.plotz.data;

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

    static {
        LISTINGS.add(new Listing(
            UUID.randomUUID().toString(),
            UUID.randomUUID(),
            "System",
            "Small Riverside Plot",
            4500,
            false,
            3,
            "Near river, outside capital",
            "Nice location with enough space for a house and garden",
            "Good price because of location",
            "Small house and garden",
            false
        ));

        LISTINGS.add(new Listing(
            UUID.randomUUID().toString(),
            UUID.randomUUID(),
            "System",
            "Capital Market Plot",
            12000,
            true,
            2,
            "Directly inside the capital",
            "Perfect for a shop or city villa",
            "Top location in the city center",
            "Empty building plot",
            true
        ));
    }

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

    public static void addOwnedPlot(PlotEntry plot) {
        OWNED_PLOTS.add(plot);
    }

    public static List<PlotEntry> getOwnedPlots(UUID ownerId) {
        List<PlotEntry> result = new ArrayList<>();
        for (PlotEntry plot : OWNED_PLOTS) {
            if (plot.ownerId().equals(ownerId)) {
                result.add(plot);
            }
        }
        return result;
    }

    public static List<Listing> getListings() {
        return new ArrayList<>(LISTINGS);
    }

    public static List<Listing> getListingsBySeller(UUID sellerId) {
        List<Listing> result = new ArrayList<>();
        for (Listing listing : LISTINGS) {
            if (listing.sellerId().equals(sellerId)) {
                result.add(listing);
            }
        }
        return result;
    }

    public static Listing getListingById(String listingId) {
        for (Listing listing : LISTINGS) {
            if (listing.listingId().equals(listingId)) {
                return listing;
            }
        }
        return null;
    }

    public static void addListing(Listing listing) {
        LISTINGS.add(listing);
    }

    public static void removeListing(String listingId) {
        LISTINGS.removeIf(l -> l.listingId().equals(listingId));
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
}