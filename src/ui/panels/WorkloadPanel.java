package ui.panels;

import algorithms.PageReplacementAlgo;
import model.SimulationResult;
import ui.Renderer;
import workload.WorkloadGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WorkloadPanel {

    private static final int PAGE_RANGE = 8;
    private static final int LENGTH     = 20;

    private final PageReplacementAlgo[] algos;
    private final String[]              colors;
    private final int                   frames;

    public WorkloadPanel(PageReplacementAlgo[] algos, String[] colors, int frames) {
        this.algos  = algos;
        this.colors = colors;
        this.frames = frames;
    }

    public void show(Scanner sc) {
        Renderer.clearScreen();
        Renderer.sectionHeader("≋  Workload Simulator", Renderer.WHITE);

        Renderer.ln("  Select workload type:");
        Renderer.ln("    " + Renderer.YELLOW + "[1]" + Renderer.RESET
                + "  Random Access   — uniform random page references");
        Renderer.ln("    " + Renderer.CYAN   + "[2]" + Renderer.RESET
                + "  Locality-based  — 70% hot working set, simulates real programs");
        Renderer.ln();
        Renderer.pr(Renderer.CYAN + "  ❯ " + Renderer.RESET + "Choice: ");

        String choice = sc.nextLine().trim();
        int[] refs;
        String wlName;

        if (choice.equals("2")) {
            refs   = WorkloadGenerator.locality(LENGTH, PAGE_RANGE);
            wlName = "Locality-based";
        } else {
            refs   = WorkloadGenerator.random(LENGTH, PAGE_RANGE);
            wlName = "Random Access";
        }

        Renderer.ln();
        Renderer.ln(Renderer.DIM  + "  Workload  : " + Renderer.RESET
                + Renderer.BOLD + wlName + Renderer.RESET);
        Renderer.ln(Renderer.DIM  + "  Generated : " + Renderer.RESET
                + Renderer.CYAN + WorkloadGenerator.toString(refs) + Renderer.RESET);
        Renderer.ln();
        Renderer.ln(Renderer.CYAN + "  Running simulation..." + Renderer.RESET);

        pause(400);
        Renderer.ln();

        // Run all algorithms on this workload
        SimulationResult[] results = new SimulationResult[algos.length];
        for (int i = 0; i < algos.length; i++)
            results[i] = algos[i].simulate(refs, frames);

        // Find best — handle ties
        int bestFaults = Integer.MAX_VALUE;
        for (SimulationResult r : results)
            if (r.pageFaults < bestFaults) bestFaults = r.pageFaults;

        boolean[] isWinner = new boolean[results.length];
        List<String> winnerNames = new ArrayList<>();
        for (int i = 0; i < results.length; i++) {
            isWinner[i] = results[i].pageFaults == bestFaults;
            if (isWinner[i]) winnerNames.add(results[i].algoName);
        }

        Renderer.compTableHeader();
        for (int i = 0; i < results.length; i++)
            Renderer.compTableRow(results[i], colors[i], isWinner[i]);
        Renderer.compTableFooter();

        Renderer.ln(Renderer.CYAN + "  " + Renderer.BOLD
                + "PAGE FAULTS BAR CHART" + Renderer.RESET
                + Renderer.DIM + "  (fewer = better)" + Renderer.RESET);
        Renderer.ln();
        Renderer.barChart(results);

        String winLabel = winnerNames.size() > 1
                ? "★  Tied on this workload: " + String.join(" & ", winnerNames)
                : "★  Winner on this workload: " + winnerNames.get(0);
        Renderer.ln("  " + Renderer.GREEN + Renderer.BOLD + winLabel + Renderer.RESET);
        Renderer.ln();

        Renderer.pressEnter();
        sc.nextLine();
    }

    private void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}