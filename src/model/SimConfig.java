package model;

/**
 * Holds all simulation settings.
 * One instance is created at startup and passed to every panel —
 * changing it here affects all panels immediately.
 */
public class SimConfig {

    // ── Defaults ──────────────────────────────────────────
    public int    frames    = 4;
    public int    pageRange = 20;   // total distinct pages
    public int    hotPages  = 4;    // pages in the "hot" working set
    public int    refLength = 30;   // how many references to generate
    public double hotRatio  = 0.80; // % of accesses that hit hot pages

    // ── Manually fixed reference string (null = auto-generate) ──
    public int[]  fixedRefs = null;

    /** Returns true if the config makes sense. */
    public boolean isValid() {
        return frames    >= 1
            && pageRange >= 2
            && hotPages  >= 1
            && hotPages  <  pageRange
            && refLength >= 1
            && hotRatio  >  0.0 && hotRatio < 1.0;
    }

    /** Human-readable summary shown in every panel header. */
    public String summary() {
        if (fixedRefs != null)
            return "fixed refs (" + fixedRefs.length + " steps)"
                 + "  frames=" + frames;
        return "pages=" + pageRange
             + "  hot=" + hotPages
             + "  frames=" + frames
             + "  refs=" + refLength
             + "  hotRatio=" + (int)(hotRatio * 100) + "%";
    }
}
