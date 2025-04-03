package tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class SequentialLogAnalyzer {

    private static final String[] LEVELS = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"};

    public static void main(String[] args) throws IOException {
        Path dir = Paths.get(".");
        String prefix = "app-";

        List<Path> logFiles = Files.list(dir)
                .filter(p -> p.getFileName().toString().startsWith(prefix) && p.toString().endsWith(".log"))
                .collect(Collectors.toList());

        Map<String, Integer> totalCounts = new HashMap<>();
        Arrays.stream(LEVELS).forEach(lvl -> totalCounts.put(lvl, 0));

        long startTime = System.nanoTime();

        for (Path file : logFiles) {
            Map<String, Integer> fileCounts = new HashMap<>();
            Arrays.stream(LEVELS).forEach(lvl -> fileCounts.put(lvl, 0));

            try (BufferedReader reader = Files.newBufferedReader(file)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    for (String level : LEVELS) {
                        if (line.contains(" " + level + " ")) {
                            fileCounts.put(level, fileCounts.get(level) + 1);
                            totalCounts.put(level, totalCounts.get(level) + 1);
                            break;
                        }
                    }
                }
            }

            System.out.println("Datei: " + file.getFileName());
            fileCounts.forEach((level, count) -> System.out.printf("  %s: %d%n", level, count));
            System.out.println();
        }

        long duration = System.nanoTime() - startTime;

        System.out.println("=== Gesamtzusammenfassung ===");
        totalCounts.forEach((level, count) -> System.out.printf("%s: %d%n", level, count));

        System.out.printf("Ausführungszeit (sequentiell): %.2f ms%n", duration / 1_000_000.0);
    }
}

