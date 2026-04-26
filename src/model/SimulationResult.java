package model;

import java.util.List;

public class SimulationResult {
    public final String        algoName;
    public final int           totalRefs;
    public final int           pageFaults;
    public final int           pageHits;
    public final double        hitRatio;
    public final List<TraceStep> trace;

    public SimulationResult(String algoName, int totalRefs,
                            int pageFaults, List<TraceStep> trace) {
        this.algoName   = algoName;
        this.totalRefs  = totalRefs;
        this.pageFaults = pageFaults;
        this.pageHits   = totalRefs - pageFaults;
        this.hitRatio   = (double) this.pageHits / totalRefs * 100;
        this.trace      = trace;
    }
}
