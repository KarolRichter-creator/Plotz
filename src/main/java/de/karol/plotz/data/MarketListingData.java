package de.karol.plotz.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MarketListingData {
    public record Listing(
        UUID sellerId,
        String sellerName,
        String title,
        int price,
        boolean capital,
        int chunkCount,
        String location,
        String description
    ) {}

    private static final List<Listing> LISTINGS = new ArrayList<>();

    static {
        LISTINGS.add(new Listing(
            UUID.randomUUID(),
            "System",
            "Kleines Grundstück am Fluss",
            4500,
            false,
            3,
            "Nahe Fluss, außerhalb Hauptstadt",
            "Schöne Lage mit Platz für Haus und Garten"
        ));
        LISTINGS.add(new Listing(
            UUID.randomUUID(),
            "System",
            "Hauptstadt-Grundstück am Markt",
            12000,
            true,
            2,
            "Direkt in der Hauptstadt",
            "Perfekt für Shop oder Stadtvilla"
        ));
    }

    private MarketListingData() {}

    public static synchronized List<Listing> getListings() {
        return List.copyOf(LISTINGS);
    }

    public static synchronized Listing getByIndex(int index) {
        if (index < 0 || index >= LISTINGS.size()) return null;
        return LISTINGS.get(index);
    }

    public static synchronized void addListing(Listing listing) {
        LISTINGS.add(listing);
    }

    public static synchronized void removeByIndex(int index) {
        if (index >= 0 && index < LISTINGS.size()) {
            LISTINGS.remove(index);
        }
    }

    public static synchronized List<Listing> getBySeller(UUID sellerId) {
        return LISTINGS.stream().filter(l -> l.sellerId().equals(sellerId)).toList();
    }
}
