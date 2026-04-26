package ui.panels;

import algorithms.PageReplacementAlgo;
import export.CSVExporter;
import model.SimConfig;
import model.SimulationResult;
import ui.Renderer;
import workload.WorkloadGenerator;

import java.io.IOException;
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
            + "  Repeated runs      — run N tests on current config, save each");
        Renderer.ln("    " + Renderer.CYAN   + "[2]" + Renderer.RESET
            + "  Frame sweep        — test all frame counts from min to max");
        Renderer.ln("    " + Renderer.RED    + "[0]" + Renderer.RESET
            + "  Cancel");
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
    //   MODE 1 — repeated runs on current config
    // ─────────────────────────────────────────────────────────
    private void repeatedRuns(Scanner sc) {
        Renderer.ln();
        Renderer.pr("  How many runs? (e.g. 30): ");
        int n = readInt(sc, 1, 9999);
        if (n < 0) return;

        Renderer.pr("  Locality-based [L] or Random [R]? (default L): ");
        String wlChoice = sc.nextLine().trim().toUpperCase();
        boolean locality = !wlChoice.equals("R");

        String fileName = CSVExporter.timestampedName("results");
        Renderer.ln();
        Renderer.ln(Renderer.CYAN + "  Running " + n + " tests..." + Renderer.RESET);
        Renderer.ln();

        int[] faultTotals = new int[algos.length];

        for (int run = 1; run <= n; run++) {
            int[] refs = WorkloadGenerator.get(cfg, locality);
            SimulationResult[] results = new SimulationResult[algos.length];

            for (int i = 0; i < algos.length; i++) {
                results[i] = algos[i].simulate(refs, cfg.frames);
                faultTotals[i] += results[i].pageFaults;
            }

            try {
                CSVExporter.append(fileName, results,
                    cfg.frames, cfg.pageRange, cfg.hotPages,
                    cfg.refLength, cfg.hotRatio, run);
            } catch (IOException e) {
                Renderer.error("Could not write CSV: " + e.getMessage());
                Renderer.pressEnter(); sc.nextLine(); return;
            }

            // Progress bar every 5 runs
            if (run % 5 == 0 || run == n) {
                int pct   = run * 100 / n;
                int filled = run * 20 / n;
                String bar = Renderer.GREEN + "█".repeat(filled)
                           + Renderer.DIM   + "░".repeat(20 - filled)
                           + Renderer.RESET;
                System.out.print("\r  [" + bar + "] " + pct + "%  run " + run + "/" + n + "   ");
            }
        }

        Renderer.ln();
        Renderer.ln();

        // ── Average summary ──
        Renderer.ln(Renderer.CYAN + "  " + Renderer.BOLD
            + "AVERAGE RESULTS OVER " + n + " RUNS" + Renderer.RESET);
        Renderer.ln();

        int bestAvg = Integer.MAX_VALUE;
        for (int t : faultTotals) bestAvg = Math.min(bestAvg, t);

        for (int i = 0; i < algos.length; i++) {
            double avgFaults = (double) faultTotals[i] / n;
            double avgHits   = cfg.refLength - avgFaults;
            double hitRatio  = avgHits / cfg.refLength * 100;
            boolean winner   = faultTotals[i] == bestAvg;

            Renderer.ln("  " + colors[i] + Renderer.BOLD
                + Renderer.pad(algos[i].getName(), 12) + Renderer.RESET
                + "  avg faults: " + Renderer.RED   + String.format("%.1f", avgFaults) + Renderer.RESET
                + "   avg hits: " + Renderer.GREEN  + String.format("%.1f", avgHits)   + Renderer.RESET
                + "   hit ratio: " + colors[i]      + String.format("%.1f%%", hitRatio) + Renderer.RESET
                + (winner ? "  " + Renderer.GREEN + Renderer.BOLD + "★ BEST" + Renderer.RESET : ""));
        }

        Renderer.ln();
        printSaved(fileName);
        Renderer.pressEnter();
        sc.nextLine();
    }

    // ─────────────────────────────────────────────────────────
    //   MODE 2 — sweep across frame counts
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

        Renderer.pr("  Runs per frame count (e.g. 20): ");
        int runsPerFrame = readInt(sc, 1, 9999);
        if (runsPerFrame < 0) return;

        Renderer.pr("  Locality-based [L] or Random [R]? (default L): ");
        boolean locality = !sc.nextLine().trim().toUpperCase().equals("R");

        // Build frame values array
        int count = 0;
        for (int f = minF; f <= maxF; f += step) count++;
        int[] frameValues = new int[count];
        int idx = 0;
        for (int f = minF; f <= maxF; f += step) frameValues[idx++] = f;

        // Accumulate average results per frame
        SimulationResult[][] avgResults = new SimulationResult[count][algos.length];

        String fileName = CSVExporter.timestampedName("frame_sweep");
        Renderer.ln();
        Renderer.ln(Renderer.CYAN + "  Sweeping frames " + minF + " → " + maxF
            + " (step " + step + ", " + runsPerFrame + " runs each)..." + Renderer.RESET);
        Renderer.ln();

        int total = count * runsPerFrame;
        int done  = 0;

        for (int fi = 0; fi < count; fi++) {
            int frames = frameValues[fi];
            int[] faultSums = new int[algos.length];

            for (int run = 0; run < runsPerFrame; run++) {
                int[] refs = WorkloadGenerator.get(cfg, locality);
                for (int ai = 0; ai < algos.length; ai++)
                    faultSums[ai] += algos[ai].simulate(refs, frames).pageFaults;
                done++;

                int pct    = done * 100 / total;
                int filled = done * 20  / total;
                String bar = Renderer.GREEN + "█".repeat(filled)
                           + Renderer.DIM   + "░".repeat(20 - filled)
                           + Renderer.RESET;
                System.out.print("\r  [" + bar + "] " + pct + "%  frames=" + frames
                    + " run " + (run+1) + "/" + runsPerFrame + "   ");
            }

            // Build averaged SimulationResult objects for CSV
            for (int ai = 0; ai < algos.length; ai++) {
                double avgF = (double) faultSums[ai] / runsPerFrame;
                int avgFInt = (int) Math.round(avgF);
                int[] dummyRefs = new int[cfg.refLength];
                // create a fake result to hold the averaged numbers
                avgResults[fi][ai] = new model.SimulationResult(
                    algos[ai].getName(), cfg.refLength, avgFInt, new java.util.ArrayList<>());
            }
        }

        Renderer.ln();
        Renderer.ln();

        // Write CSV
        try {
            CSVExporter.writeBatch(fileName, frameValues, avgResults,
                cfg.pageRange, cfg.hotPages, cfg.refLength, cfg.hotRatio);
        } catch (IOException e) {
            Renderer.error("Could not write CSV: " + e.getMessage()); return;
        }

        // Print summary table
        Renderer.ln(Renderer.CYAN + "  " + Renderer.BOLD
            + "FRAME SWEEP SUMMARY" + Renderer.RESET
            + Renderer.DIM + "  (avg over " + runsPerFrame + " runs each)" + Renderer.RESET);
        Renderer.ln();

        // Header
        System.out.printf("  %-8s", "Frames");
        for (PageReplacementAlgo a : algos)
            System.out.printf("  %-14s", a.getName() + " faults");
        System.out.printf("  %-10s%n", "Winner");
        Renderer.ln(Renderer.DIM + "  " + "─".repeat(60) + Renderer.RESET);

        for (int fi = 0; fi < count; fi++) {
            int bestF = Integer.MAX_VALUE;
            for (SimulationResult r : avgResults[fi]) bestF = Math.min(bestF, r.pageFaults);

            System.out.printf("  %-8d", frameValues[fi]);
            for (int ai = 0; ai < algos.length; ai++) {
                String val = String.valueOf(avgResults[fi][ai].pageFaults);
                boolean w  = avgResults[fi][ai].pageFaults == bestF;
                System.out.printf("  " + (w ? Renderer.GREEN + Renderer.BOLD : Renderer.DIM)
                    + "%-14s" + Renderer.RESET, val);
            }

            // Winner name(s)
            StringBuilder wb = new StringBuilder();
            for (int ai = 0; ai < algos.length; ai++)
                if (avgResults[fi][ai].pageFaults == bestF)
                    wb.append(algos[ai].getName()).append(" ");
            System.out.printf("  %s%n", wb.toString().trim());
        }

        Renderer.ln();
        printSaved(fileName);
        Renderer.pressEnter();
        sc.nextLine();
    }

    // ─────────────────────────────────────────────────────────
    //   HELPERS
    // ─────────────────────────────────────────────────────────
    private void printSaved(String fileName) {
        Renderer.ln(Renderer.GREEN + "  ✓  Saved to: " + Renderer.RESET
            + Renderer.BOLD + fileName + Renderer.RESET);
        Renderer.ln(Renderer.DIM + Renderer.RESET);
        Renderer.ln();
    }

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
