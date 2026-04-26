package algorithms;

import model.SimulationResult;
import model.TraceStep;

import java.util.ArrayList;
import java.util.List;

public class ARC implements PageReplacementAlgo {

    @Override
    public String getName() { return "ARC"; }

    @Override
    public SimulationResult simulate(int[] refs, int frames) {
        List<Integer> t1 = new ArrayList<>();   // recently used once
        List<Integer> t2 = new ArrayList<>();   // recently used twice+
        List<Integer> b1 = new ArrayList<>();   // ghost of t1 (evicted)
        List<Integer> b2 = new ArrayList<>();   // ghost of t2 (evicted)
        int p      = 0;   // adaptive parameter: target size for T1
        int faults = 0;
        List<TraceStep> trace = new ArrayList<>();

        for (int i = 0; i < refs.length; i++) {
            int x     = refs[i];
            boolean inT1 = t1.contains(x), inT2 = t2.contains(x);
            boolean inB1 = b1.contains(x), inB2 = b2.contains(x);
            boolean hit  = inT1 || inT2;
            if (!hit) faults++;

            if (inT2) {
                // Cache hit in T2 → move to MRU of T2
                t2.remove((Integer) x);
                t2.add(0, x);

            } else if (inT1) {
                // Cache hit in T1 → promote to T2
                t1.remove((Integer) x);
                t2.add(0, x);

            } else if (inB1) {
                // Ghost hit in B1 → adapt p upward, replace, move to T2
                p = Math.min(frames, p + Math.max(1, b2.size() / Math.max(1, b1.size())));
                replace(t1, t2, b1, b2, p, frames);
                t2.add(0, x);
                b1.remove((Integer) x);

            } else if (inB2) {
                // Ghost hit in B2 → adapt p downward, replace, move to T2
                p = Math.max(0, p - Math.max(1, b1.size() / Math.max(1, b2.size())));
                replace(t1, t2, b1, b2, p, frames);
                t2.add(0, x);
                b2.remove((Integer) x);

            } else {
                // Full miss
                if (t1.size() + t2.size() >= frames) {
                    // Cache full → evict
                    if (!t1.isEmpty()) b1.add(0, t1.remove(t1.size() - 1));
                    else if (!t2.isEmpty()) b2.add(0, t2.remove(t2.size() - 1));
                }
                t1.add(0, x);
            }

            trace.add(new TraceStep(
                i + 1, x, hit, faults,
                t1.toString(),   // stateA = T1 (recent)
                t2.toString()    // stateB = T2 (frequent)
            ));
        }

        return new SimulationResult(getName(), refs.length, faults, trace);
    }

    /** Evict one page when cache is full, guided by the adaptive parameter p. */
    private void replace(List<Integer> t1, List<Integer> t2,
                         List<Integer> b1, List<Integer> b2,
                         int p, int frames) {
        if (!t1.isEmpty() && (t1.size() > p || t2.isEmpty())) {
            b1.add(0, t1.remove(t1.size() - 1));
        } else if (!t2.isEmpty()) {
            b2.add(0, t2.remove(t2.size() - 1));
        }
    }
}
