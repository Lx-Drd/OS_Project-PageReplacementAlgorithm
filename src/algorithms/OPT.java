package algorithms;

import model.SimulationResult;
import model.TraceStep;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Belady's OPT — the theoretical optimal algorithm.
 * Evicts the page that will not be used for the longest time in the future.
 * Cannot be implemented in a real OS (requires future knowledge),
 * but gives the absolute lower bound on page faults for any algorithm.
 */
public class OPT implements PageReplacementAlgo {

    @Override
    public String getName() { return "OPT"; }

    @Override
    public SimulationResult simulate(int[] refs, int frames) {
        Set<Integer>    cache  = new HashSet<>();
        int             faults = 0;
        List<TraceStep> trace  = new ArrayList<>();

        for (int i = 0; i < refs.length; i++) {
            int p   = refs[i];
            boolean hit = cache.contains(p);
            if (!hit) {
                faults++;
                if (cache.size() < frames) {
                    cache.add(p);
                } else {
                    // Evict the page used farthest in the future
                    int victim = findVictim(cache, refs, i + 1);
                    cache.remove(victim);
                    cache.add(p);
                }
            }

            trace.add(new TraceStep(
                i + 1, p, hit, faults,
                cache.toString(),   // stateA = current frames
                hit ? "HIT" : "evict→" + (hit ? "-" : "")  // stateB
            ));
        }

        return new SimulationResult(getName(), refs.length, faults, trace);
    }

    /**
     * Among pages currently in cache, find the one whose next use
     * is farthest away (or never used again → evict immediately).
     */
    private int findVictim(Set<Integer> cache, int[] refs, int fromIndex) {
        int victim   = -1;
        int farthest = -1;

        for (int page : cache) {
            int nextUse = Integer.MAX_VALUE;
            for (int j = fromIndex; j < refs.length; j++) {
                if (refs[j] == page) { nextUse = j; break; }
            }
            if (nextUse > farthest) {
                farthest = nextUse;
                victim   = page;
            }
        }
        return victim;
    }
}
