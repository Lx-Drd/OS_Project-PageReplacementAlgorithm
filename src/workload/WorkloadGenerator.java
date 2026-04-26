package workload;

import java.util.Random;

public class WorkloadGenerator {

    private static final Random rng = new Random();

    /** Uniform random page references — worst case for locality-based algos. */
    public static int[] random(int length, int pageRange) {
        int[] refs = new int[length];
        for (int i = 0; i < length; i++)
            refs[i] = rng.nextInt(pageRange) + 1;
        return refs;
    }

    /**
     * Locality-based — simulates real programs.
     * 70% of accesses go to a small "hot" working set,
     * 30% go anywhere in the page range.
     */
    public static int[] locality(int length, int pageRange) {
        int hotSize  = Math.max(1, pageRange / 3);
        int[] hotSet = new int[hotSize];
        for (int i = 0; i < hotSize; i++) hotSet[i] = i + 1;

        int[] refs = new int[length];
        for (int i = 0; i < length; i++) {
            refs[i] = rng.nextDouble() < 0.70
                ? hotSet[rng.nextInt(hotSize)]
                : rng.nextInt(pageRange) + 1;
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
