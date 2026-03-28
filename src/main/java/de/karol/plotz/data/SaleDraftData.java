package de.karol.plotz.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SaleDraftData {
    public record SaleDraft(
        UUID owner,
        String title,
        int price,
        String location,
        String description
    ) {}

    private static final List<SaleDraft> DRAFTS = new ArrayList<>();

    private SaleDraftData() {}

    public static synchronized void addDraft(SaleDraft draft) {
        DRAFTS.add(draft);
    }

    public static synchronized List<SaleDraft> getDraftsOf(UUID owner) {
        return DRAFTS.stream().filter(d -> d.owner().equals(owner)).toList();
    }
}
