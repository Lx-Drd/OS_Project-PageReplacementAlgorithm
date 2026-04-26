package algorithms;

import model.SimulationResult;

public interface PageReplacementAlgo {
    SimulationResult simulate(int[] refs, int frames);
    String getName();
}
