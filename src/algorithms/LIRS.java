package algorithms;

import model.SimulationResult;
import model.TraceStep;

import java.util.ArrayList;
import java.util.List;

public class LIRS implements PageReplacementAlgo {

    @Override
    public String getName() { return "LIRS"; }

    @Override
    public SimulationResult simulate(int[] refs, int frames) {
        List<Integer> lirStack = new ArrayList<>();
        List<Integer> hirList  = new ArrayList<>();
        int lirCap = frames - 1;   // reserve 1 slot for HIR
        int hirCap = 1;
        int faults = 0;
        List<TraceStep> trace = new ArrayList<>();

        for (int i = 0; i < refs.length; i++) {
            int p     = refs[i];
            boolean inLIR = lirStack.contains(p);
            boolean inHIR = hirList.contains(p);
            boolean hit   = inLIR || inHIR;
            if (!hit) faults++;

            if (inLIR) {
                // Move to top of LIR stack (most recently used)
                lirStack.remove((Integer) p);
                lirStack.add(0, p);

            } else if (inHIR) {
                // Promote HIR page → LIR
                hirList.remove((Integer) p);
                lirStack.remove((Integer) p);
                if (lirStack.size() >= lirCap) {
                    int demoted = lirStack.remove(lirStack.size() - 1);
                    hirList.add(0, demoted);
                }
                lirStack.add(0, p);

            } else {
                // Cold miss — bring page in
                if (hirList.size() >= hirCap) hirList.remove(hirList.size() - 1);
                if (lirStack.size() < lirCap)  lirStack.add(0, p);
                else                            hirList.add(0, p);
            }

            // Enforce capacities
            while (lirStack.size() > lirCap) {
                int d = lirStack.remove(lirStack.size() - 1);
                hirList.add(0, d);
            }
            while (hirList.size() > hirCap) hirList.remove(hirList.size() - 1);

            trace.add(new TraceStep(
                i + 1, p, hit, faults,
                lirStack.toString(),   // stateA = LIR stack
                hirList.toString()     // stateB = HIR list
            ));
        }

        return new SimulationResult(getName(), refs.length, faults, trace);
    }
}
