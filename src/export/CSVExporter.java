package export;

import model.SimulationResult;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CSVExporter {

    /**
     * Appends one batch of results (one run) to the CSV file.
     * Creates the file with headers if it doesn't exist yet.
     */
    public static void append(String filePath,
                              SimulationResult[] results,
                              int frames, int pageRange,
                              int hotPages, int refLength,
                              double hotRatio, int runNumber) throws IOException {

        boolean newFile = !new java.io.File(filePath).exists();

        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, true))) {
            if (newFile) writeHeader(pw);
            String ts = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            for (SimulationResult r : results) {
                pw.printf("%d,%s,%d,%d,%d,%d,%.0f%%,%d,%d,%.1f%%%n",
                    runNumber,
                    r.algoName,
                    frames,
                    pageRange,
                    hotPages,
                    refLength,
                    hotRatio * 100,
                    r.pageFaults,
                    r.pageHits,
                    r.hitRatio
                );
            }
        }
    }

    /**
     * Writes a full batch report — one row per (frames, algo) combination.
     * Used by the batch runner that sweeps across frame counts.
     */
    public static void writeBatch(String filePath,
                                  int[] frameValues,
                                  SimulationResult[][] resultsByFrame,
                                  int pageRange, int hotPages,
                                  int refLength, double hotRatio) throws IOException {

        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, false))) {
            pw.println("Frames,Algorithm,PageRange,HotPages,RefLength,HotRatio,PageFaults,PageHits,HitRatio");

            for (int f = 0; f < frameValues.length; f++) {
                for (SimulationResult r : resultsByFrame[f]) {
                    pw.printf("%d,%s,%d,%d,%d,%.0f%%,%d,%d,%.1f%%%n",
                        frameValues[f],
                        r.algoName,
                        pageRange,
                        hotPages,
                        refLength,
                        hotRatio * 100,
                        r.pageFaults,
                        r.pageHits,
                        r.hitRatio
                    );
                }
            }
        }
    }

    private static void writeHeader(PrintWriter pw) {
        pw.println("Run,Algorithm,Frames,PageRange,HotPages,RefLength,HotRatio,PageFaults,PageHits,HitRatio");
    }

    /** Returns a timestamped filename like: results_2025-06-01_14-30.csv */
    public static String timestampedName(String prefix) {
        String ts = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        return prefix + "_" + ts + ".csv";
    }
}
