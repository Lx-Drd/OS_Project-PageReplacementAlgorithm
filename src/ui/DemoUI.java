package ui;

import algorithms.*;
import ui.panels.*;

import java.util.Scanner;

/**
 * Entry point for the terminal demo.
 * This class only handles navigation — all rendering lives in Renderer,
 * all algorithm logic lives in algorithms/, all panels live in ui/panels/.
 */
public class DemoUI {

    // ── Default simulation config ──────────────────────────
    static final int[] REFS   = {1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5};
    static final int   FRAMES = 4;

    // ── Algorithm instances (shared across panels) ─────────
    static final PageReplacementAlgo[] ALGOS = {
        new LIRS(),
        new CLOCKPro(),
        new ARC()
    };

    static final String[] COLORS = {
        Renderer.YELLOW,    // LIRS
        Renderer.MAGENTA,   // CLOCK-Pro
        Renderer.BLUE       // ARC
    };

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // ── Panels ─────────────────────────────────────────
        AlgoPanel lirsPanel = new AlgoPanel(
            ALGOS[0], REFS, FRAMES, Renderer.YELLOW, "LIR STACK", "HIR LIST");

        AlgoPanel clockPanel = new AlgoPanel(
            ALGOS[1], REFS, FRAMES, Renderer.MAGENTA, "FRAMES", "HAND");

        AlgoPanel arcPanel = new AlgoPanel(
            ALGOS[2], REFS, FRAMES, Renderer.BLUE, "T1 (recent)", "T2 (freq)");

        ComparePanel  comparePanel  = new ComparePanel(ALGOS, COLORS, REFS, FRAMES);
        WorkloadPanel workloadPanel = new WorkloadPanel(ALGOS, COLORS, FRAMES);

        // ── Main loop ──────────────────────────────────────
        Renderer.clearScreen();
        Renderer.splash();
        pause(1000);

        boolean running = true;
        while (running) {
            Renderer.clearScreen();
            Renderer.mainMenu();
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> lirsPanel.show(sc);
                case "2" -> clockPanel.show(sc);
                case "3" -> arcPanel.show(sc);
                case "4" -> comparePanel.show(sc);
                case "5" -> workloadPanel.show(sc);
                case "0" -> running = false;
                default  -> { Renderer.error("Invalid option. Try again."); pause(600); }
            }
        }

        Renderer.goodbye();
    }

    private static void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
