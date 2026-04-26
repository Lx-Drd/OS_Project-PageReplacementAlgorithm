package ui;

import algorithms.*;
import model.SimConfig;
import ui.panels.*;

import java.util.Scanner;

public class DemoUI {

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
        Scanner   sc  = new Scanner(System.in);
        SimConfig cfg = new SimConfig();   // one config, shared by all panels

        AlgoPanel     lirsPanel     = new AlgoPanel(ALGOS[0], cfg, Renderer.YELLOW,  "LIR STACK",   "HIR LIST");
        AlgoPanel     clockPanel    = new AlgoPanel(ALGOS[1], cfg, Renderer.MAGENTA, "FRAMES",      "HAND");
        AlgoPanel     arcPanel      = new AlgoPanel(ALGOS[2], cfg, Renderer.BLUE,    "T1 (recent)", "T2 (freq)");
        ComparePanel  comparePanel  = new ComparePanel(ALGOS, COLORS, cfg);
        WorkloadPanel workloadPanel = new WorkloadPanel(ALGOS, COLORS, cfg);
        ConfigPanel   configPanel   = new ConfigPanel(cfg);

        Renderer.clearScreen();
        Renderer.splash();
        pause(1000);

        boolean running = true;
        while (running) {
            Renderer.clearScreen();
            Renderer.mainMenu(cfg);
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> lirsPanel.show(sc);
                case "2" -> clockPanel.show(sc);
                case "3" -> arcPanel.show(sc);
                case "4" -> comparePanel.show(sc);
                case "5" -> workloadPanel.show(sc);
                case "6" -> configPanel.show(sc);
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
