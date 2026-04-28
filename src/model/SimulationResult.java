package model;

import java.util.List;

public class SimulationResult {
    public final String          algoName;
    public final int             totalRefs;
    public final int             pageFaults;
    public final int             pageHits;
    public final double          hitRatio;
    public final List<TraceStep> trace;

    // ── Summary statistics (set externally by BatchPanel) ──
    public double avgFaults;
    public double stdFaults;
    public int    minFaults;
    public int    maxFaults;

    // ── Hit ratio over time ────────────────────────────────
    public final double[] hitRatioOverTime;
    public final int[]    snapshotSteps;
    private static final int SNAPSHOT_EVERY = 5;

    public SimulationResult(String algoName, int totalRefs,
                            int pageFaults, List<TraceStep> trace) {
        this.algoName   = algoName;
        this.totalRefs  = totalRefs;
        this.pageFaults = pageFaults;
        this.pageHits   = totalRefs - pageFaults;
        this.hitRatio   = totalRefs > 0 ? (double) this.pageHits / totalRefs * 100 : 0;
        this.trace      = trace;

        // Build hit ratio over time from trace
        int snapCount         = trace.size() / SNAPSHOT_EVERY;
        this.hitRatioOverTime = new double[snapCount];
        this.snapshotSteps    = new int[snapCount];

        for (int i = 0; i < snapCount; i++) {
            int stepIdx    = (i + 1) * SNAPSHOT_EVERY - 1;
            TraceStep snap = trace.get(stepIdx);
            int hitsAtStep = snap.step - snap.totalFaults;
            this.hitRatioOverTime[i] = snap.step > 0
                ? (double) hitsAtStep / snap.step * 100 : 0;
            this.snapshotSteps[i] = snap.step;
        }
    }
}
