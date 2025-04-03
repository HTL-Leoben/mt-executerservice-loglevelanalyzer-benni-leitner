package tools;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class LogAnalyzerTask implements Callable<Map<String, Integer>> {

    private final Path file;
    private static final String[] LEVELS = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"};

    public LogAnalyzerTask(Path file) {
        this.file = file;
    }

    @Override
    public Map<String, Integer> call() throws Exception {
        Map<String, Integer> counts = new HashMap<>();
        for (String level : LEVELS) {
            counts.put(level, 0);
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (String level : LEVELS) {
                    if (line.contains(" " + level + " ")) {
                        counts.put(level, counts.get(level) + 1);
                        break;
                    }
                }
            }
        }

        System.out.println("Fertig analysiert: " + file.getFileName());
        return counts;
    }
}
