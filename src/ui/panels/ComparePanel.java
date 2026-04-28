package ui.panels;

import algorithms.PageReplacementAlgo;
import model.SimConfig;
import model.SimulationResult;
import ui.Renderer;
import workload.WorkloadGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ComparePanel {

    private final PageReplacementAlgo[] algos;
    private final String[]              colors;
    private final SimConfig             cfg;

    public ComparePanel(PageReplacementAlgo[] algos, String[] colors, SimConfig cfg) {
        this.algos  = algos;
        this.colors = colors;
        this.cfg    = cfg;
    }

    public void show(Scanner sc) {
        Renderer.clearScreen();
        Renderer.sectionHeader("▤  Comparison — All Algorithms", Renderer.GREEN);

        int[] refs = WorkloadGenerator.get(cfg, true);

        Renderer.ln(Renderer.DIM
            + "  Reference string : " + WorkloadGenerator.toString(refs)
            + "   frames = " + cfg.frames + Renderer.RESET);
        Renderer.ln();

        SimulationResult[] results = new SimulationResult[algos.length];
        for (int i = 0; i < algos.length; i++)
            results[i] = algos[i].simulate(refs, cfg.frames);

        int bestFaults = Integer.MAX_VALUE;
        for (SimulationResult r : results)
            if (r.pageFaults < bestFaults) bestFaults = r.pageFaults;

        boolean[]    isWinner    = new boolean[results.length];
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

        Renderer.pressEnter(sc);
    }
}
