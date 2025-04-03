package tools;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ParallelLogAnalyzer {

    private static final String[] LEVELS = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"};

    public static void main(String[] args) throws Exception {
        Path dir = Paths.get("."); // Oder z.B. Paths.get("logs");
        String prefix = "app-";

        List<Path> logFiles = Files.list(dir)
                .filter(p -> p.getFileName().toString().startsWith(prefix) && p.toString().endsWith(".log"))
                .collect(Collectors.toList());

        Map<String, Integer> totalCounts = new HashMap<>();
        for (String level : LEVELS) {
            totalCounts.put(level, 0);
        }

        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        List<Future<Map<String, Integer>>> futures = new ArrayList<>();

        long startTime = System.nanoTime();

        for (Path file : logFiles) {
            futures.add(executor.submit(new LogAnalyzerTask(file)));
        }

        for (int i = 0; i < futures.size(); i++) {
            Map<String, Integer> result = futures.get(i).get();
            Path fileName = logFiles.get(i).getFileName();
            System.out.println("Datei: " + fileName);
            for (String level : LEVELS) {
                System.out.printf("  %s: %d%n", level, result.get(level));
                totalCounts.put(level, totalCounts.get(level) + result.get(level));
            }
            System.out.println();
        }

        long duration = System.nanoTime() - startTime;

        executor.shutdown();

        System.out.println("=== Gesamtzusammenfassung ===");
        for (String level : LEVELS) {
            System.out.printf("%s: %d%n", level, totalCounts.get(level));
        }

        System.out.printf("Ausführungszeit (parallel): %.2f ms%n", duration / 1_000_000.0);
    }
}
