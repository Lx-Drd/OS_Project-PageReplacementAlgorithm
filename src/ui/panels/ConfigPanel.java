package ui.panels;

import model.SimConfig;
import ui.Renderer;

import java.util.Scanner;

public class ConfigPanel {

    private final SimConfig cfg;

    public ConfigPanel(SimConfig cfg) {
        this.cfg = cfg;
    }

    public void show(Scanner sc) {
        boolean editing = true;
        while (editing) {
            Renderer.clearScreen();
            printConfigMenu();

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> setFrames(sc);
                case "2" -> setPageRange(sc);
                case "3" -> setHotPages(sc);
                case "4" -> setRefLength(sc);
                case "5" -> setHotRatio(sc);
                case "6" -> setFixedRefs(sc);
                case "7" -> { cfg.fixedRefs = null; ok("Switched to auto-generated refs."); pause(700); }
                case "0" -> editing = false;
                default  -> { Renderer.error("Invalid option."); pause(600); }
            }
        }
    }

    // ─────────────────────────────────────────────────────
    //   MENU
    // ─────────────────────────────────────────────────────
    private void printConfigMenu() {
        Renderer.sectionHeader("⚙  Simulation Settings", Renderer.CYAN);

        Renderer.ln(Renderer.CYAN
            + "  ┌─────────────────────────────────────────────────────────┐");
        Renderer.ln("  │  " + Renderer.BOLD + Renderer.WHITE
            + "Current Configuration" + Renderer.RESET + Renderer.CYAN
            + "                                    │");
        Renderer.ln("  ├─────────────────────────────────────────────────────────┤");

        configRow("1", "Frames          ", cfg.frames + " frames");
        configRow("2", "Page Range      ", cfg.pageRange + " total pages");
        configRow("3", "Hot Pages       ", cfg.hotPages + " pages  ("
            + (int)(100.0 * cfg.hotPages / cfg.pageRange) + "% of total)");
        configRow("4", "Reference Length", cfg.refLength + " accesses");
        configRow("5", "Hot Ratio       ", (int)(cfg.hotRatio * 100) + "% of refs hit hot pages");
        configRow("6", "Fixed Ref String",
            cfg.fixedRefs == null ? Renderer.DIM + "not set (auto)" + Renderer.RESET
                                  : Renderer.GREEN + "set (" + cfg.fixedRefs.length + " steps)" + Renderer.RESET);

        Renderer.ln("  ├─────────────────────────────────────────────────────────┤");
        Renderer.ln("  │   " + Renderer.YELLOW + "[7]" + Renderer.RESET
            + "  Clear fixed refs → use auto-generated            "
            + Renderer.CYAN + "│");
        Renderer.ln("  │   " + Renderer.RED + "[0]" + Renderer.RESET
            + "  Done — save & return to menu                     "
            + Renderer.CYAN + "│");
        Renderer.ln("  └─────────────────────────────────────────────────────────┘"
            + Renderer.RESET);
        Renderer.ln();

        // Show what LIRS/ARC/Compare will use
        Renderer.ln(Renderer.DIM + "  Active config: " + Renderer.RESET
            + Renderer.CYAN + cfg.summary() + Renderer.RESET);
        Renderer.ln();
        Renderer.pr(Renderer.CYAN + "  ❯ " + Renderer.RESET + "Choose setting to change: ");
    }

    private void configRow(String num, String label, String value) {
        Renderer.ln("  │   " + Renderer.YELLOW + "[" + num + "]" + Renderer.RESET
            + "  " + Renderer.pad(label, 20)
            + Renderer.CYAN + Renderer.pad(value, 30) + Renderer.RESET
            + Renderer.CYAN + "│");
    }

    // ─────────────────────────────────────────────────────
    //   SETTERS
    // ─────────────────────────────────────────────────────
    private void setFrames(Scanner sc) {
        Renderer.pr("\n  New number of frames (current=" + cfg.frames + ", min=1): ");
        int v = readInt(sc, 1, 999);
        if (v > 0) { cfg.frames = v; ok("Frames set to " + v + "."); }
        pause(700);
    }

    private void setPageRange(Scanner sc) {
        Renderer.pr("\n  Total number of pages (current=" + cfg.pageRange
            + ", min=" + (cfg.hotPages + 1) + "): ");
        int v = readInt(sc, cfg.hotPages + 1, 9999);
        if (v > 0) { cfg.pageRange = v; ok("Page range set to " + v + "."); }
        pause(700);
    }

    private void setHotPages(Scanner sc) {
        Renderer.pr("\n  Hot pages in working set (current=" + cfg.hotPages
            + ", max=" + (cfg.pageRange - 1) + "): ");
        int v = readInt(sc, 1, cfg.pageRange - 1);
        if (v > 0) { cfg.hotPages = v; ok("Hot pages set to " + v + "."); }
        pause(700);
    }

    private void setRefLength(Scanner sc) {
        Renderer.pr("\n  Reference string length (current=" + cfg.refLength + ", min=5): ");
        int v = readInt(sc, 5, 9999);
        if (v > 0) { cfg.refLength = v; ok("Reference length set to " + v + "."); }
        pause(700);
    }

    private void setHotRatio(Scanner sc) {
        Renderer.pr("\n  Hot access ratio % (current=" + (int)(cfg.hotRatio * 100)
            + ", e.g. 80 means 80%): ");
        int v = readInt(sc, 1, 99);
        if (v > 0) { cfg.hotRatio = v / 100.0; ok("Hot ratio set to " + v + "%."); }
        pause(700);
    }

    private void setFixedRefs(Scanner sc) {
        Renderer.ln();
        Renderer.ln(Renderer.DIM
            + "  Enter a space-separated list of page numbers (e.g: 1 2 3 4 1 2 5 1 2 3 4 5)"
            + Renderer.RESET);
        Renderer.pr("  ❯ ");
        String line = sc.nextLine().trim();
        if (line.isEmpty()) { Renderer.error("Nothing entered."); pause(700); return; }

        String[] parts = line.split("\\s+");
        int[] refs = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                refs[i] = Integer.parseInt(parts[i]);
                if (refs[i] < 1) throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Renderer.error("Invalid input — use positive integers only.");
            pause(900); return;
        }

        cfg.fixedRefs = refs;
        ok("Fixed reference string set (" + refs.length + " steps).");
        pause(800);
    }

    // ─────────────────────────────────────────────────────
    //   HELPERS
    // ─────────────────────────────────────────────────────
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

    private void ok(String msg) {
        Renderer.ln(Renderer.GREEN + "  ✓  " + msg + Renderer.RESET);
    }

    private void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
