package model;

public class TraceStep {
    public final int     step;
    public final int     ref;
    public final boolean hit;
    public final int     totalFaults;
    public final String  stateA;   // LIR stack / frames / T1
    public final String  stateB;   // HIR list  / hand  / T2

    public TraceStep(int step, int ref, boolean hit, int totalFaults,
                     String stateA, String stateB) {
        this.step        = step;
        this.ref         = ref;
        this.hit         = hit;
        this.totalFaults = totalFaults;
        this.stateA      = stateA;
        this.stateB      = stateB;
    }
}
