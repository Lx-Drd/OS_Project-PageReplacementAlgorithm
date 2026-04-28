package ui.panels;

import algorithms.PageReplacementAlgo;
import export.CSVExporter;
import model.SimConfig;
import model.SimulationResult;
import model.TraceStep;
import ui.Renderer;
import workload.WorkloadGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BatchPanel {

    private final PageReplacementAlgo[] algos;
    private final String[]              colors;
    private final SimConfig             cfg;

    public BatchPanel(PageReplacementAlgo[] algos, String[] colors, SimConfig cfg) {
        this.algos  = algos;
        this.colors = colors;
        this.cfg    = cfg;
    }

    public void show(Scanner sc) {
        Renderer.clearScreen();
        Renderer.sectionHeader("◈  Batch Runner & CSV Export", Renderer.GREEN);

        Renderer.ln(Renderer.DIM + "  Active config: " + Renderer.RESET
            + Renderer.CYAN + cfg.summary() + Renderer.RESET);
        Renderer.ln();
        Renderer.ln("  Select batch mode:");
        Renderer.ln("    " + Renderer.YELLOW + "[1]" + Renderer.RESET
            + "  Repeated runs  — N tests on current config, saves 3 CSV files");
        Renderer.ln("    " + Renderer.CYAN   + "[2]" + Renderer.RESET
            + "  Frame sweep    — sweep frame counts min→max, save CSV");
        Renderer.ln("    " + Renderer.RED    + "[0]" + Renderer.RESET + "  Cancel");
        Renderer.ln();
        Renderer.pr(Renderer.CYAN + "  ❯ " + Renderer.RESET + "Choice: ");

        String choice = sc.nextLine().trim();
        switch (choice) {
            case "1" -> repeatedRuns(sc);
            case "2" -> frameSweep(sc);
            default  -> { }
        }
    }

    // ─────────────────────────────────────────────────────────
    //   MODE 1 — repeated runs
    // ─────────────────────────────────────────────────────────
    private void repeatedRuns(Scanner sc) {
        Renderer.ln();
        Renderer.pr("  How many runs? (e.g. 50): ");
        int n = readInt(sc, 1, 9999);
        if (n < 0) return;

        Renderer.pr("  Locality-based [L] or Random [R]? (default L): ");
        boolean locality = !sc.nextLine().trim().toUpperCase().equals("R");

        String rawFile     = CSVExporter.timestampedName("raw_runs");
        String summaryFile = CSVExporter.timestampedName("summary");
        String trendFile   = CSVExporter.timestampedName("hit_ratio_trend");

        Renderer.ln();
        Renderer.ln(Renderer.CYAN + "  Running " + n + " tests..." + Renderer.RESET);
        Renderer.ln();

        // Per-algo accumulators
        int[]    faultSums  = new int[algos.length];
        int[]    faultMins  = new int[algos.length];
        int[]    faultMaxs  = new int[algos.length];
        double[] faultSumSq = new double[algos.length]; // for std dev
        for (int i = 0; i < algos.length; i++) {
            faultMins[i] = Integer.MAX_VALUE;
            faultMaxs[i] = Integer.MIN_VALUE;
        }

        // Keep last run's results for hit-ratio-over-time export
        SimulationResult[] lastResults = null;

        for (int run = 1; run <= n; run++) {
            int[]              refs    = WorkloadGenerator.get(cfg, locality);
            SimulationResult[] results = new SimulationResult[algos.length];

            for (int i = 0; i < algos.length; i++) {
                results[i] = algos[i].simulate(refs, cfg.frames);
                int f = results[i].pageFaults;
                faultSums[i]  += f;
                faultSumSq[i] += (double) f * f;
                if (f < faultMins[i]) faultMins[i] = f;
                if (f > faultMaxs[i]) faultMaxs[i] = f;
            }
            lastResults = results;

            // Write raw row
            try {
                CSVExporter.append(rawFile, results,
                    cfg.frames, cfg.pageRange, cfg.hotPages,
                    cfg.refLength, cfg.hotRatio, run);
            } catch (IOException e) {
                Renderer.error("CSV write error: " + e.getMessage());
                Renderer.pressEnter(sc); return;
            }

            // Progress bar
            if (run % 5 == 0 || run == n) {
                int filled = run * 20 / n;
                String bar = Renderer.GREEN + "█".repeat(filled)
                           + Renderer.DIM   + "░".repeat(20 - filled)
                           + Renderer.RESET;
                System.out.print("\r  [" + bar + "] " + (run*100/n) + "%  run " + run + "/" + n + "   ");
            }
        }

        Renderer.ln(); Renderer.ln();

        // ── Build summary SimulationResult objects ──
        SimulationResult[] summaries = new SimulationResult[algos.length];
        for (int i = 0; i < algos.length; i++) {
            double avg = (double) faultSums[i] / n;
            double variance = faultSumSq[i] / n - avg * avg;
            double std = Math.sqrt(Math.max(0, variance));

            // Use a dummy result just to carry the stats
            summaries[i] = new SimulationResult(
                algos[i].getName(), cfg.refLength,
                (int) Math.round(avg), new ArrayList<>());
            summaries[i].avgFaults = avg;
            summaries[i].stdFaults = std;
            summaries[i].minFaults = faultMins[i];
            summaries[i].maxFaults = faultMaxs[i];
        }

        // ── Write summary CSV ──
        try {
            CSVExporter.writeSummary(summaryFile, summaries,
                cfg.frames, cfg.pageRange, cfg.hotPages,
                cfg.refLength, cfg.hotRatio, n);
        } catch (IOException e) {
            Renderer.error("Summary CSV error: " + e.getMessage());
        }

        // ── Write hit ratio over time CSV (from last run) ──
        if (lastResults != null) {
            try {
                CSVExporter.writeHitRatioOverTime(trendFile, lastResults);
            } catch (IOException e) {
                Renderer.error("Trend CSV error: " + e.getMessage());
            }
        }

        // ── Print terminal summary ──
        printSummaryTable(summaries, n);
        printHitRatioTrend(lastResults);

        Renderer.ln();
        Renderer.ln(Renderer.GREEN + "  ✓  3 files saved:" + Renderer.RESET);
        Renderer.ln("     " + Renderer.BOLD + rawFile     + Renderer.RESET + Renderer.DIM + "  ← all raw runs"         + Renderer.RESET);
        Renderer.ln("     " + Renderer.BOLD + summaryFile + Renderer.RESET + Renderer.DIM + "  ← avg/std/min/max"       + Renderer.RESET);
        Renderer.ln("     " + Renderer.BOLD + trendFile   + Renderer.RESET + Renderer.DIM + "  ← hit ratio over time"   + Renderer.RESET);
        Renderer.ln(Renderer.DIM + Renderer.RESET);
        Renderer.ln();
        Renderer.pressEnter(sc);
    }

    // ─────────────────────────────────────────────────────────
    //   MODE 2 — frame sweep
    // ─────────────────────────────────────────────────────────
    private void frameSweep(Scanner sc) {
        Renderer.ln();
        Renderer.pr("  Min frames (e.g. 2): ");
        int minF = readInt(sc, 1, 999);
        if (minF < 0) return;

        Renderer.pr("  Max frames (e.g. 16): ");
        int maxF = readInt(sc, minF + 1, 999);
        if (maxF < 0) return;

        Renderer.pr("  Step size (e.g. 1 or 2): ");
        int step = readInt(sc, 1, 999);
        if (step < 0) return;

        Renderer.pr("  Runs per frame count (e.g. 30): ");
        int runsPerFrame = readInt(sc, 1, 9999);
        if (runsPerFrame < 0) return;

        Renderer.pr("  Locality-based [L] or Random [R]? (default L): ");
        boolean locality = !sc.nextLine().trim().toUpperCase().equals("R");

        int count = 0;
        for (int f = minF; f <= maxF; f += step) count++;
        int[] frameValues = new int[count];
        int idx = 0;
        for (int f = minF; f <= maxF; f += step) frameValues[idx++] = f;

        SimulationResult[][] avgResults = new SimulationResult[count][algos.length];
        String sweepFile = CSVExporter.timestampedName("frame_sweep");

        Renderer.ln();
        Renderer.ln(Renderer.CYAN + "  Sweeping frames " + minF + "→" + maxF
            + " (step=" + step + ", " + runsPerFrame + " runs each)..." + Renderer.RESET);
        Renderer.ln();

        int total = count * runsPerFrame, done = 0;

        for (int fi = 0; fi < count; fi++) {
            int      frames    = frameValues[fi];
            int[]    faultSums  = new int[algos.length];
            int[]    faultMins  = new int[algos.length];
            int[]    faultMaxs  = new int[algos.length];
            double[] faultSumSq = new double[algos.length];
            for (int i = 0; i < algos.length; i++) {
                faultMins[i] = Integer.MAX_VALUE;
                faultMaxs[i] = Integer.MIN_VALUE;
            }

            for (int run = 0; run < runsPerFrame; run++) {
                int[] refs = WorkloadGenerator.get(cfg, locality);
                for (int ai = 0; ai < algos.length; ai++) {
                    int f = algos[ai].simulate(refs, frames).pageFaults;
                    faultSums[ai]  += f;
                    faultSumSq[ai] += (double) f * f;
                    if (f < faultMins[ai]) faultMins[ai] = f;
                    if (f > faultMaxs[ai]) faultMaxs[ai] = f;
                }
                done++;
                int filled = done * 20 / total;
                String bar = Renderer.GREEN + "█".repeat(filled)
                           + Renderer.DIM   + "░".repeat(20 - filled) + Renderer.RESET;
                System.out.print("\r  [" + bar + "] " + (done*100/total)
                    + "%  frames=" + frames + " run " + (run+1) + "/" + runsPerFrame + "   ");
            }

            for (int ai = 0; ai < algos.length; ai++) {
                double avg = (double) faultSums[ai] / runsPerFrame;
                double std = Math.sqrt(Math.max(0, faultSumSq[ai] / runsPerFrame - avg * avg));
                avgResults[fi][ai] = new SimulationResult(
                    algos[ai].getName(), cfg.refLength,
                    (int) Math.round(avg), new ArrayList<>());
                avgResults[fi][ai].avgFaults = avg;
                avgResults[fi][ai].stdFaults = std;
                avgResults[fi][ai].minFaults = faultMins[ai];
                avgResults[fi][ai].maxFaults = faultMaxs[ai];
            }
        }

        Renderer.ln(); Renderer.ln();

        try {
            CSVExporter.writeBatch(sweepFile, frameValues, avgResults,
                cfg.pageRange, cfg.hotPages, cfg.refLength, cfg.hotRatio);
        } catch (IOException e) {
            Renderer.error("CSV write error: " + e.getMessage()); return;
        }

        // Print sweep table
        Renderer.ln(Renderer.CYAN + "  " + Renderer.BOLD + "FRAME SWEEP SUMMARY"
            + Renderer.RESET + Renderer.DIM + "  (avg over " + runsPerFrame + " runs)" + Renderer.RESET);
        Renderer.ln();
        System.out.printf("  %-8s", "Frames");
        for (PageReplacementAlgo a : algos)
            System.out.printf("  %-20s", a.getName() + " avg±std");
        System.out.printf("  %s%n", "Winner");
        Renderer.ln(Renderer.DIM + "  " + "─".repeat(72) + Renderer.RESET);

        for (int fi = 0; fi < count; fi++) {
            double bestAvg = Double.MAX_VALUE;
            for (SimulationResult r : avgResults[fi])
                if (r.avgFaults < bestAvg) bestAvg = r.avgFaults;

            System.out.printf("  %-8d", frameValues[fi]);
            StringBuilder winners = new StringBuilder();
            for (int ai = 0; ai < algos.length; ai++) {
                boolean w   = avgResults[fi][ai].avgFaults == bestAvg;
                String  val = String.format("%.1f±%.1f",
                    avgResults[fi][ai].avgFaults, avgResults[fi][ai].stdFaults);
                System.out.printf("  " + (w ? Renderer.GREEN + Renderer.BOLD : Renderer.DIM)
                    + "%-20s" + Renderer.RESET, val);
                if (w) winners.append(algos[ai].getName()).append(" ");
            }
            System.out.printf("  %s%n", winners.toString().trim());
        }

        Renderer.ln();
        Renderer.ln(Renderer.GREEN + "  ✓  Saved: " + Renderer.RESET + Renderer.BOLD + sweepFile + Renderer.RESET);
        Renderer.ln(Renderer.DIM + Renderer.RESET);
        Renderer.ln();
        Renderer.pressEnter(sc);
    }

    // ─────────────────────────────────────────────────────────
    //   TERMINAL DISPLAY HELPERS
    // ─────────────────────────────────────────────────────────
    private void printSummaryTable(SimulationResult[] summaries, int n) {
        Renderer.ln(Renderer.CYAN + "  " + Renderer.BOLD
            + "SUMMARY STATISTICS  (" + n + " runs)" + Renderer.RESET);
        Renderer.ln();

        // Find best avg
        double bestAvg = Double.MAX_VALUE;
        for (SimulationResult r : summaries) bestAvg = Math.min(bestAvg, r.avgFaults);

        System.out.printf("  %-12s  %-10s  %-10s  %-8s  %-8s  %-10s%n",
            "Algorithm", "Avg Faults", "Std Dev", "Min", "Max", "Avg HitRatio");
        Renderer.ln(Renderer.DIM + "  " + "─".repeat(64) + Renderer.RESET);

        for (int i = 0; i < summaries.length; i++) {
            SimulationResult r = summaries[i];
            boolean winner = r.avgFaults == bestAvg;
            double avgHitRatio = (cfg.refLength - r.avgFaults) / cfg.refLength * 100;

            System.out.printf("  " + colors[i] + Renderer.BOLD + "%-12s" + Renderer.RESET
                + "  " + Renderer.RED   + "%-10s" + Renderer.RESET
                + "  " + Renderer.DIM   + "%-10s" + Renderer.RESET
                + "  " + Renderer.DIM   + "%-8s"  + Renderer.RESET
                + "  " + Renderer.DIM   + "%-8s"  + Renderer.RESET
                + "  " + colors[i]      + "%-10s" + Renderer.RESET
                + "%s%n",
                r.algoName,
                String.format("%.2f", r.avgFaults),
                String.format("±%.2f", r.stdFaults),
                r.minFaults,
                r.maxFaults,
                String.format("%.1f%%", avgHitRatio),
                winner ? "  " + Renderer.GREEN + Renderer.BOLD + "★ BEST" + Renderer.RESET : "");
        }
        Renderer.ln();
    }

    private void printHitRatioTrend(SimulationResult[] results) {
        if (results == null) return;
        int snapCount = Integer.MAX_VALUE;
        for (SimulationResult r : results)
            snapCount = Math.min(snapCount, r.hitRatioOverTime.length);
        if (snapCount == 0) return;

        Renderer.ln(Renderer.CYAN + "  " + Renderer.BOLD
            + "HIT RATIO OVER TIME  (last run)" + Renderer.RESET
            + Renderer.DIM + "  — sampled every 5 steps" + Renderer.RESET);
        Renderer.ln();

        for (int i = 0; i < results.length; i++) {
            System.out.printf("  " + colors[i] + "%-12s" + Renderer.RESET + "  ", results[i].algoName);
            for (int s = 0; s < snapCount; s++) {
                double v = results[i].hitRatioOverTime[s];
                // Mini sparkline: block height based on ratio
                String block = v >= 80 ? "█" : v >= 60 ? "▆" : v >= 40 ? "▄" : v >= 20 ? "▂" : "▁";
                System.out.print(colors[i] + block + Renderer.RESET);
            }
            // Print final value
            System.out.printf("  %s%.1f%%%s%n",
                colors[i], results[i].hitRatioOverTime[snapCount-1], Renderer.RESET);
        }
        Renderer.ln();
    }

    // ─────────────────────────────────────────────────────────
    //   HELPERS
    // ─────────────────────────────────────────────────────────
    private int readInt(Scanner sc, int min, int max) {
        try {
            int v = Integer.parseInt(sc.nextLine().trim());
            if (v < min || v > max) {
                Renderer.error("Must be between " + min + " and " + max + ".");
                return -1;
            }
            return v;
        } catch (NumberFormatException e) {
            Renderer.error("Please enter a valid number.");
            return -1;
        }
    }
}
