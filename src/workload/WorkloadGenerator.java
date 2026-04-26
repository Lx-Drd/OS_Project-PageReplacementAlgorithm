package workload;

import model.SimConfig;

import java.util.Random;

public class WorkloadGenerator {

    private static final Random rng = new Random();

    /**
     * Returns the reference string to use based on config:
     * - if fixedRefs is set → return it directly
     * - otherwise → generate based on type
     */
    public static int[] get(SimConfig cfg, boolean locality) {
        if (cfg.fixedRefs != null) return cfg.fixedRefs;
        return locality ? locality(cfg) : random(cfg);
    }

    /** Uniform random — every page equally likely. */
    public static int[] random(SimConfig cfg) {
        int[] refs = new int[cfg.refLength];
        for (int i = 0; i < cfg.refLength; i++)
            refs[i] = rng.nextInt(cfg.pageRange) + 1;
        return refs;
    }

    /**
     * Locality-based — simulates real program behavior.
     * hotRatio % of accesses go to the small hot working set,
     * the rest go anywhere in the full page range.
     * With default settings (20 pages, 4 hot, 80% ratio) this
     * produces a clear gap vs random access.
     */
    public static int[] locality(SimConfig cfg) {
        int[] hotSet = new int[cfg.hotPages];
        for (int i = 0; i < cfg.hotPages; i++) hotSet[i] = i + 1;

        int[] refs = new int[cfg.refLength];
        for (int i = 0; i < cfg.refLength; i++) {
            refs[i] = rng.nextDouble() < cfg.hotRatio
                ? hotSet[rng.nextInt(cfg.hotPages)]
                : rng.nextInt(cfg.pageRange) + 1;
        }
        return refs;
    }

    public static String toString(int[] refs) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < refs.length; i++) {
            sb.append(refs[i]);
            if (i < refs.length - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
