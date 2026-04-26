package ui.panels;

import algorithms.PageReplacementAlgo;
import model.SimConfig;
import model.SimulationResult;
import ui.Renderer;
import workload.WorkloadGenerator;

import java.util.Scanner;

public class AlgoPanel {

    private final PageReplacementAlgo algo;
    private final SimConfig           cfg;
    private final String              color;
    private final String              colA;
    private final String              colB;

    public AlgoPanel(PageReplacementAlgo algo, SimConfig cfg,
                     String color, String colA, String colB) {
        this.algo  = algo;
        this.cfg   = cfg;
        this.color = color;
        this.colA  = colA;
        this.colB  = colB;
    }

    public void show(Scanner sc) {
        Renderer.clearScreen();
        Renderer.sectionHeader(algo.getName() + " Algorithm", color);

        // Use fixed refs if set, otherwise generate from config
        int[] refs = WorkloadGenerator.get(cfg, true);

        Renderer.ln(Renderer.DIM
            + "  Reference string : " + WorkloadGenerator.toString(refs)
            + "   frames = " + cfg.frames + Renderer.RESET);
        Renderer.ln();

        SimulationResult result = algo.simulate(refs, cfg.frames);

        Renderer.traceTable(result, colA, colB, color);
        Renderer.stats(result, color);
        Renderer.pressEnter();
        sc.nextLine();
    }
}
