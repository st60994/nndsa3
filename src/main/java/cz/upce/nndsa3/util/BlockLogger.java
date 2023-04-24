package cz.upce.nndsa3.util;

public class BlockLogger {

    private BlockLogger() {

    }

    private static StringBuilder log = new StringBuilder();

    public static StringBuilder getLog() {
        return log;
    }

    public static void writeDataBlockAccessToLog(long position, int blockIndex) {
        log.append("    Accessing data block of index ").append(blockIndex).append(" on position ").append(position).append("\n");
    }

    public static void writeIndexFileToLog(int index, int key) {
        log.append("Accessing index ").append(index).append(" of the index file with key ").append(key).append("\n");
    }

    public static void writeDataFileToLog(int index, int key) {
        log.append("Accessing index ").append(index).append(" of the data file with key ").append(key).append("\n");
    }

    public static void clearLog() {
        log = new StringBuilder();
    }

}
