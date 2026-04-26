package ui.panels;

import algorithms.PageReplacementAlgo;
import model.SimulationResult;
import ui.Renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ComparePanel {

    private final PageReplacementAlgo[] algos;
    private final String[]              colors;
    private final int[]                 refs;
    private final int                   frames;

    public ComparePanel(PageReplacementAlgo[] algos, String[] colors,
                        int[] refs, int frames) {
        this.algos  = algos;
        this.colors = colors;
        this.refs   = refs;
        this.frames = frames;
    }

    public void show(Scanner sc) {
        Renderer.clearScreen();
        Renderer.sectionHeader("▤  Comparison — All Algorithms", Renderer.GREEN);

        Renderer.ln(Renderer.DIM
                + "  Reference string : " + workload.WorkloadGenerator.toString(refs)
                + "   frames = " + frames + Renderer.RESET);
        Renderer.ln();

        // Run all simulations
        SimulationResult[] results = new SimulationResult[algos.length];
        for (int i = 0; i < algos.length; i++)
            results[i] = algos[i].simulate(refs, frames);

        // Find best (fewest faults) — may be a tie
        int bestFaults = Integer.MAX_VALUE;
        for (SimulationResult r : results)
            if (r.pageFaults < bestFaults) bestFaults = r.pageFaults;

        boolean[] isWinner = new boolean[results.length];
        List<String> winnerNames = new ArrayList<>();
        for (int i = 0; i < results.length; i++) {
            isWinner[i] = results[i].pageFaults == bestFaults;
            if (isWinner[i]) winnerNames.add(results[i].algoName);
        }

        // ── Summary table ──
        Renderer.compTableHeader();
        for (int i = 0; i < results.length; i++)
            Renderer.compTableRow(results[i], colors[i], isWinner[i]);
        Renderer.compTableFooter();

        // ── Bar chart ──
        Renderer.ln(Renderer.CYAN + "  " + Renderer.BOLD
                + "PAGE FAULTS BAR CHART" + Renderer.RESET
                + Renderer.DIM + "  (fewer = better)" + Renderer.RESET);
        Renderer.ln();
        Renderer.barChart(results);

        // ── Winner callout (handles ties) ──
        String winLabel = winnerNames.size() > 1
                ? "★  Tied on this workload: " + String.join(" & ", winnerNames)
                : "★  Winner on this workload: " + winnerNames.get(0);
        Renderer.ln("  " + Renderer.GREEN + Renderer.BOLD + winLabel + Renderer.RESET);
        Renderer.ln();

        Renderer.pressEnter();
        sc.nextLine();
    }
}