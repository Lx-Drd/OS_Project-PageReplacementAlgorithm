package ui.panels;

import algorithms.PageReplacementAlgo;
import model.SimConfig;
import model.SimulationResult;
import ui.Renderer;
import workload.WorkloadGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WorkloadPanel {

    private final PageReplacementAlgo[] algos;
    private final String[]              colors;
    private final SimConfig             cfg;

    public WorkloadPanel(PageReplacementAlgo[] algos, String[] colors, SimConfig cfg) {
        this.algos  = algos;
        this.colors = colors;
        this.cfg    = cfg;
    }

    public void show(Scanner sc) {
        Renderer.clearScreen();
        Renderer.sectionHeader("≋  Workload Simulator", Renderer.WHITE);

        Renderer.ln(Renderer.DIM + "  Active config: " + Renderer.RESET
            + Renderer.CYAN + cfg.summary() + Renderer.RESET);
        Renderer.ln();

        Renderer.ln("  Select workload type:");
        Renderer.ln("    " + Renderer.YELLOW + "[1]" + Renderer.RESET
            + "  Random Access   — every page equally likely");
        Renderer.ln("    " + Renderer.CYAN   + "[2]" + Renderer.RESET
            + "  Locality-based  — " + (int)(cfg.hotRatio*100) + "% of refs hit "
            + cfg.hotPages + " hot pages  (out of " + cfg.pageRange + " total)");
        if (cfg.fixedRefs != null)
            Renderer.ln("    " + Renderer.GREEN + "[3]" + Renderer.RESET
                + "  Fixed ref string — use the one you set in config");
        Renderer.ln();
        Renderer.pr(Renderer.CYAN + "  ❯ " + Renderer.RESET + "Choice: ");

        String choice = sc.nextLine().trim();
        int[]  refs;
        String wlName;

        if (choice.equals("2")) {
            refs   = WorkloadGenerator.locality(cfg);
            wlName = "Locality-based  (hot=" + cfg.hotPages
                   + "/" + cfg.pageRange + " pages, " + (int)(cfg.hotRatio*100) + "% ratio)";
        } else if (choice.equals("3") && cfg.fixedRefs != null) {
            refs   = cfg.fixedRefs;
            wlName = "Fixed reference string";
        } else {
            refs   = WorkloadGenerator.random(cfg);
            wlName = "Random Access  (" + cfg.pageRange + " pages)";
        }

        Renderer.ln();
        Renderer.ln(Renderer.DIM  + "  Workload  : " + Renderer.RESET + Renderer.BOLD + wlName + Renderer.RESET);
        Renderer.ln(Renderer.DIM  + "  Generated : " + Renderer.RESET + Renderer.CYAN + WorkloadGenerator.toString(refs) + Renderer.RESET);
        Renderer.ln();
        Renderer.ln(Renderer.CYAN + "  Running simulation..." + Renderer.RESET);
        pause(400);
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

        Renderer.ln(Renderer.CYAN + "  " + Renderer.BOLD + "PAGE FAULTS BAR CHART"
            + Renderer.RESET + Renderer.DIM + "  (fewer = better)" + Renderer.RESET);
        Renderer.ln();
        Renderer.barChart(results);

        String winLabel = winnerNames.size() > 1
            ? "★  Tied: " + String.join(" & ", winnerNames)
            : "★  Winner: " + winnerNames.get(0);
        Renderer.ln("  " + Renderer.GREEN + Renderer.BOLD + winLabel + Renderer.RESET);
        Renderer.ln();

        Renderer.pressEnter();
        sc.nextLine();
    }

    private void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
