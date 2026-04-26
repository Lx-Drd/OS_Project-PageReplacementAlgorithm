package algorithms;

import model.SimulationResult;
import model.TraceStep;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class CLOCKPro implements PageReplacementAlgo {

    @Override
    public String getName() { return "CLOCK-Pro"; }

    @Override
    public SimulationResult simulate(int[] refs, int frames) {
        int[] pageFrames = new int[frames];
        int[] refBits    = new int[frames];
        Arrays.fill(pageFrames, -1);

        int hand   = 0;
        int faults = 0;
        List<TraceStep> trace = new ArrayList<>();

        for (int i = 0; i < refs.length; i++) {
            int p   = refs[i];
            int idx = indexOf(pageFrames, p);
            boolean hit = idx != -1;

            if (!hit) {
                faults++;
                // Advance hand past pages with reference bit = 1
                while (refBits[hand] > 0) {
                    refBits[hand] = 0;
                    hand = (hand + 1) % frames;
                }
                pageFrames[hand] = p;
                refBits[hand]    = 1;
                hand = (hand + 1) % frames;
            } else {
                refBits[idx] = 1;   // set reference bit on hit
            }

            trace.add(new TraceStep(
                i + 1, p, hit, faults,
                framesStr(pageFrames, hand),   // stateA = frame contents + hand
                "hand:" + hand                 // stateB = hand position
            ));
        }

        return new SimulationResult(getName(), refs.length, faults, trace);
    }

    private int indexOf(int[] arr, int val) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == val) return i;
        return -1;
    }

    private String framesStr(int[] frames, int hand) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < frames.length; i++) {
            if (i == hand) sb.append(">");
            sb.append(frames[i] == -1 ? "·" : frames[i]);
            if (i < frames.length - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
