package de.karol.plotz.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OwnedPlotData {
    public record PlotEntry(
        UUID owner,
        String title,
        boolean capital,
        int chunkCount,
        String location,
        String description
    ) {}

    private static final List<PlotEntry> PLOTS = new ArrayList<>();

    private OwnedPlotData() {}

    public static synchronized void addPlot(PlotEntry entry) {
        PLOTS.add(entry);
    }

    public static synchronized List<PlotEntry> getPlotsOf(UUID owner) {
        return PLOTS.stream().filter(p -> p.owner().equals(owner)).toList();
    }
}
