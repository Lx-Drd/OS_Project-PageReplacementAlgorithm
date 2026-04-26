package ui;

import model.SimulationResult;
import model.TraceStep;

public class Renderer {

    // ── ANSI codes ────────────────────────────────────────────
    public static final String RESET   = "\u001B[0m";
    public static final String BOLD    = "\u001B[1m";
    public static final String DIM     = "\u001B[2m";
    public static final String RED     = "\u001B[31m";
    public static final String GREEN   = "\u001B[32m";
    public static final String YELLOW  = "\u001B[33m";
    public static final String BLUE    = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN    = "\u001B[36m";
    public static final String WHITE   = "\u001B[37m";

    // ── Column widths for the trace table ─────────────────────
    private static final int[] COL_W = {6, 5, 18, 16, 8, 14};

    // ─────────────────────────────────────────────────────────
    //   SECTION HEADER
    // ─────────────────────────────────────────────────────────
    public static void sectionHeader(String title, String color) {
        ln(color + BOLD);
        ln("  ╔══════════════════════════════════════════════════════════╗");
        ln("  ║ " + pad(title, 56) + "║");
        ln("  ╚══════════════════════════════════════════════════════════╝");
        ln(RESET);
    }

    // ─────────────────────────────────────────────────────────
    //   TRACE TABLE  (used by every algo panel)
    // ─────────────────────────────────────────────────────────
    public static void traceTable(SimulationResult r,
                                  String colA, String colB,
                                  String accentColor) {
        tableHeader(new String[]{"STEP", "REF", colA, colB, "RESULT", "FAULTS"});

        for (TraceStep s : r.trace) {
            String hitStr = s.hit
                ? GREEN + BOLD + "HIT " + RESET
                : RED   + BOLD + "MISS" + RESET;

            tableRow(new String[]{
                String.valueOf(s.step),
                accentColor + BOLD + s.ref + RESET,
                accentColor + s.stateA + RESET,
                CYAN        + s.stateB + RESET,
                hitStr,
                RED + s.totalFaults + RESET
            });
        }
        tableFooter();
    }

    // ─────────────────────────────────────────────────────────
    //   STATS BLOCK
    // ─────────────────────────────────────────────────────────
    public static void stats(SimulationResult r, String color) {
        ln("  " + DIM + "─".repeat(56) + RESET);
        ln("  " + BOLD + "Results:  " + RESET
            + "Faults: " + RED    + r.pageFaults + RESET
            + "   Hits: " + GREEN + r.pageHits   + RESET
            + "   Hit Ratio: " + color + BOLD
            + String.format("%.1f%%", r.hitRatio) + RESET);
        ln();
    }

    // ─────────────────────────────────────────────────────────
    //   COMPARISON TABLE  (one row)
    // ─────────────────────────────────────────────────────────
    public static void compTableHeader() {
        ln(CYAN
            + "  ┌──────────────────┬──────────────┬──────────────┬─────────────────┐");
        ln("  │ " + BOLD + WHITE + pad("Algorithm", 16) + RESET + CYAN
            + " │ " + BOLD + pad("Page Faults", 12) + RESET + CYAN
            + " │ " + BOLD + pad("Page Hits",   12) + RESET + CYAN
            + " │  " + BOLD + pad("Hit Ratio",   15) + RESET + CYAN + "│");
        ln("  ├──────────────────┼──────────────┼──────────────┼─────────────────┤");
    }

    public static void compTableRow(SimulationResult r, String color, boolean winner) {
        String star = winner ? GREEN + "★ BEST " + RESET + CYAN : "       ";
        ln(CYAN + "  │ " + color + BOLD + pad(r.algoName, 16) + RESET + CYAN
            + " │ " + RED    + pad(String.valueOf(r.pageFaults), 12) + RESET + CYAN
            + " │ " + GREEN  + pad(String.valueOf(r.pageHits),   12) + RESET + CYAN
            + " │" + YELLOW + pad(String.format("%.1f%%", r.hitRatio), 9)
            + RESET + star + CYAN + " │" + RESET);
    }

    public static void compTableFooter() {
        ln(CYAN
            + "  └──────────────────┴──────────────┴──────────────┴─────────────────┘"
            + RESET);
        ln();
    }

    // ─────────────────────────────────────────────────────────
    //   BAR CHART
    // ─────────────────────────────────────────────────────────
    public static void barChart(SimulationResult[] results) {
        int max = 1;
        for (SimulationResult r : results) max = Math.max(max, r.pageFaults);

        String[] colors = {YELLOW, MAGENTA, BLUE};
        for (int i = 0; i < results.length; i++) {
            int barLen = (int)((double) results[i].pageFaults / max * 30);
            String bar  = colors[i] + "█".repeat(barLen) + RESET;
            String rest = DIM       + "░".repeat(30 - barLen) + RESET;
            ln("  " + colors[i] + pad(results[i].algoName, 10) + RESET
                + "  " + bar + rest
                + "  " + colors[i] + results[i].pageFaults + RESET);
        }
        ln();
    }

    // ─────────────────────────────────────────────────────────
    //   SPLASH  &  MAIN MENU
    // ─────────────────────────────────────────────────────────
    public static void splash() {
        ln(CYAN + BOLD);
        ln("  ╔══════════════════════════════════════════════════════════╗");
        ln("  ║                                                          ║");
        ln("  ║     ✦  PAGE REPLACEMENT ALGORITHM DEMO  ✦               ║");
        ln("  ║                                                          ║");
        ln("  ║     LIRS  ·  CLOCK-Pro  ·  ARC                           ║");
        ln("  ║                                                          ║");
        ln("  ║        By: Hatim Al-Muzaini | 451008063                  ║");
        ln("  ║        & Moayad Al-Blaadi | 451008293                    ║");
        ln("  ║                                                          ║");
        ln("  ║     Operating Systems Project  ·  2025-26                ║");
        ln("  ║     Islamic University of Madinah                        ║");
        ln("  ║                                                          ║");
        ln("  ╚══════════════════════════════════════════════════════════╝");
        ln(RESET);
        ln(DIM + CYAN
            + "  ✦   ·  ✦  ·   ·   ✦     ·   ✦  ·  ·   ✦   ·    ✦  ·   ✦"
            + RESET);
        ln();
    }

    public static void mainMenu() {
        ln(CYAN
            + "  ┌─────────────────────────────────────────────────────────┐");
        ln("  │  " + BOLD + WHITE + "✦  MAIN MENU" + RESET + CYAN
            + "                                          │");
        ln("  ├─────────────────────────────────────────────────────────┤");
        ln("  │                                                         │");
        ln("  │   " + YELLOW   + "[1]" + RESET + "  " + BOLD + "LIRS Algorithm      " + RESET
            + "— step-by-step trace         " + CYAN + "│");
        ln("  │   " + MAGENTA  + "[2]" + RESET + "  " + BOLD + "CLOCK-Pro Algorithm " + RESET
            + "— clock hand simulation      " + CYAN + "│");
        ln("  │   " + BLUE     + "[3]" + RESET + "  " + BOLD + "ARC Algorithm       " + RESET
            + "— adaptive T1/T2 lists       " + CYAN + "│");
        ln("  │                                                         │");
        ln("  ├─────────────────────────────────────────────────────────┤");
        ln("  │                                                         │");
        ln("  │   " + GREEN    + "[4]" + RESET + "  " + BOLD + "Compare All         " + RESET
            + "— side-by-side results       " + CYAN + "│");
        ln("  │   " + WHITE    + "[5]" + RESET + "  " + BOLD + "Workload Simulator  " + RESET
            + "— random / locality-based    " + CYAN + "│");
        ln("  │                                                         │");
        ln("  ├─────────────────────────────────────────────────────────┤");
        ln("  │   " + RED      + "[0]" + RESET
            + "  Exit                                             " + CYAN + "│");
        ln("  └─────────────────────────────────────────────────────────┘"
            + RESET);
        ln();
        pr(CYAN + "  ❯ " + RESET + "Enter choice: ");
    }

    public static void goodbye() {
        clearScreen();
        ln(CYAN + BOLD);
        ln("  ╔══════════════════════════════════════════════════════════╗");
        ln("  ║                                                          ║");
        ln("  ║     Thanks for watching the demo!  ✦  Good luck!         ║");
        ln("  ║     OS Project 2025-26                                   ║");
        ln("  ║                                                          ║");
        ln("  ╚══════════════════════════════════════════════════════════╝");
        ln(RESET);
    }

    public static void pressEnter() {
        ln();
        ln(DIM + "  Press " + RESET + CYAN + "ENTER" + RESET
            + DIM + " to return to menu..." + RESET);
    }

    public static void error(String msg) {
        ln();
        ln("  " + RED + "✖  " + msg + RESET);
    }

    // ─────────────────────────────────────────────────────────
    //   INTERNAL TABLE HELPERS
    // ─────────────────────────────────────────────────────────
    private static void tableHeader(String[] cols) {
        StringBuilder top = new StringBuilder(CYAN + "  ┌");
        StringBuilder mid = new StringBuilder(CYAN + "  │");
        StringBuilder sep = new StringBuilder(CYAN + "  ├");
        for (int i = 0; i < cols.length; i++) {
            top.append("─".repeat(COL_W[i] + 2)).append(i < cols.length-1 ? "┬" : "┐");
            mid.append(" ").append(BOLD).append(WHITE)
               .append(pad(cols[i], COL_W[i])).append(RESET).append(CYAN).append(" │");
            sep.append("─".repeat(COL_W[i] + 2)).append(i < cols.length-1 ? "┼" : "┤");
        }
        ln(top + RESET); ln(mid + RESET); ln(sep + RESET);
    }

    private static void tableRow(String[] cols) {
        StringBuilder row = new StringBuilder(CYAN + "  │");
        for (int i = 0; i < cols.length; i++) {
            String vis = stripAnsi(cols[i]);
            int    pad = COL_W[i] - vis.length();
            row.append(" ").append(cols[i])
               .append(" ".repeat(Math.max(0, pad)))
               .append(RESET).append(CYAN).append(" │");
        }
        ln(row + RESET);
    }

    private static void tableFooter() {
        StringBuilder bot = new StringBuilder(CYAN + "  └");
        for (int i = 0; i < COL_W.length; i++)
            bot.append("─".repeat(COL_W[i] + 2)).append(i < COL_W.length-1 ? "┴" : "┘");
        ln(bot + RESET); ln();
    }

    // ─────────────────────────────────────────────────────────
    //   TINY UTILS
    // ─────────────────────────────────────────────────────────
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static String pad(String s, int w) {
        if (s == null) s = "";
        if (s.length() >= w) return s.substring(0, w);
        return s + " ".repeat(w - s.length());
    }

    private static String stripAnsi(String s) {
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    public static void ln(String s) { System.out.println(s); }
    public static void ln()         { System.out.println(); }
    public static void pr(String s) { System.out.print(s); }
}
