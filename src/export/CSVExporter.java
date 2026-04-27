package export;

import model.SimulationResult;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CSVExporter {

    // ─────────────────────────────────────────────────────────
    //   RAW RUNS  (one row per algo per run)
    // ─────────────────────────────────────────────────────────
    public static void append(String filePath,
                              SimulationResult[] results,
                              int frames, int pageRange,
                              int hotPages, int refLength,
                              double hotRatio, int runNumber) throws IOException {

        boolean newFile = !new java.io.File(filePath).exists();
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, true))) {
            if (newFile)
                pw.println("Run,Algorithm,Frames,PageRange,HotPages,RefLength,HotRatio,PageFaults,PageHits,HitRatio");
            for (SimulationResult r : results) {
                pw.printf("%d,%s,%d,%d,%d,%d,%.0f%%,%d,%d,%.2f%n",
                    runNumber, r.algoName, frames, pageRange,
                    hotPages, refLength, hotRatio * 100,
                    r.pageFaults, r.pageHits, r.hitRatio);
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //   SUMMARY STATS  (avg, std, min, max across N runs)
    // ─────────────────────────────────────────────────────────
    public static void writeSummary(String filePath,
                                    SimulationResult[] summaries,
                                    int frames, int pageRange,
                                    int hotPages, int refLength,
                                    double hotRatio, int totalRuns) throws IOException {

        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, false))) {
            pw.println("Algorithm,Frames,PageRange,HotPages,RefLength,HotRatio,Runs,"
                + "AvgFaults,StdFaults,MinFaults,MaxFaults,AvgHitRatio");
            for (SimulationResult r : summaries) {
                double avgHitRatio = (refLength - r.avgFaults) / refLength * 100;
                pw.printf("%s,%d,%d,%d,%d,%.0f%%,%d,%.2f,%.2f,%d,%d,%.2f%n",
                    r.algoName, frames, pageRange, hotPages, refLength,
                    hotRatio * 100, totalRuns,
                    r.avgFaults, r.stdFaults, r.minFaults, r.maxFaults,
                    avgHitRatio);
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //   HIT RATIO OVER TIME  (one column per algo, rows = steps)
    // ─────────────────────────────────────────────────────────
    public static void writeHitRatioOverTime(String filePath,
                                             SimulationResult[] results) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, false))) {
            // Header: Step, LIRS, CLOCK-Pro, ARC, OPT ...
            pw.print("Step");
            for (SimulationResult r : results) pw.print("," + r.algoName);
            pw.println();

            // Find shortest snapshot array to stay in sync
            int snapCount = Integer.MAX_VALUE;
            for (SimulationResult r : results)
                snapCount = Math.min(snapCount, r.hitRatioOverTime.length);

            for (int i = 0; i < snapCount; i++) {
                pw.print(results[0].snapshotSteps[i]);
                for (SimulationResult r : results)
                    pw.printf(",%.2f", r.hitRatioOverTime[i]);
                pw.println();
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //   FRAME SWEEP  (avg faults per frame count)
    // ─────────────────────────────────────────────────────────
    public static void writeBatch(String filePath,
                                  int[] frameValues,
                                  SimulationResult[][] resultsByFrame,
                                  int pageRange, int hotPages,
                                  int refLength, double hotRatio) throws IOException {

        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, false))) {
            pw.println("Frames,Algorithm,PageRange,HotPages,RefLength,HotRatio,"
                + "AvgFaults,AvgHits,AvgHitRatio,StdFaults,MinFaults,MaxFaults");
            for (int f = 0; f < frameValues.length; f++) {
                for (SimulationResult r : resultsByFrame[f]) {
                    double avgHits  = refLength - r.avgFaults;
                    double hitRatio = avgHits / refLength * 100;
                    pw.printf("%d,%s,%d,%d,%d,%.0f%%,%.2f,%.2f,%.2f,%.2f,%d,%d%n",
                        frameValues[f], r.algoName, pageRange, hotPages, refLength,
                        hotRatio * 100,
                        r.avgFaults, avgHits, hitRatio,
                        r.stdFaults, r.minFaults, r.maxFaults);
                }
            }
        }
    }

    /** Returns a timestamped filename like: results_2025-06-01_14-30.csv */
    public static String timestampedName(String prefix) {
        String ts = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        return prefix + "_" + ts + ".csv";
    }
}
