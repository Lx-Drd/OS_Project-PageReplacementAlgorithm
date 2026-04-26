package ui.panels;

import algorithms.PageReplacementAlgo;
import model.SimulationResult;
import ui.Renderer;

import java.util.Scanner;

public class AlgoPanel {

    private final PageReplacementAlgo algo;
    private final int[]               refs;
    private final int                 frames;
    private final String              color;
    private final String              colA;   // label for stateA column
    private final String              colB;   // label for stateB column

    public AlgoPanel(PageReplacementAlgo algo, int[] refs, int frames,
                     String color, String colA, String colB) {
        this.algo   = algo;
        this.refs   = refs;
        this.frames = frames;
        this.color  = color;
        this.colA   = colA;
        this.colB   = colB;
    }

    public void show(Scanner sc) {
        Renderer.clearScreen();
        Renderer.sectionHeader(algo.getName() + " Algorithm", color);

        Renderer.ln(Renderer.DIM
            + "  Reference string : " + workload.WorkloadGenerator.toString(refs)
            + "   frames = " + frames + Renderer.RESET);
        Renderer.ln();

        SimulationResult result = algo.simulate(refs, frames);

        Renderer.traceTable(result, colA, colB, color);
        Renderer.stats(result, color);
        Renderer.pressEnter();
        sc.nextLine();
    }
}
