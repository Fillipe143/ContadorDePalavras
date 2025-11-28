import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Main {

    private static final String SEARCH_WORD = "the";

    private static final String[] FILES = {
            "res/DonQuixote-388208.txt",
            "res/Dracula-165307.txt",
            "res/MobyDick-217452.txt"
    };

    private static final double[] SAMPLES_PERCENT = {0.10, 0.50, 1.00};

    public static void main(String[] args) {
        List<ISearcher> searchers = new ArrayList<>();

        searchers.add(new SerialCPUSearcher());

        int maxCores = Runtime.getRuntime().availableProcessors();

        for (int i = 1; i <= maxCores; i *= 2) {
            searchers.add(new ParallelCPUSearcher(i));
        }
        if ((maxCores & (maxCores - 1)) != 0) {
            searchers.add(new ParallelCPUSearcher(maxCores));
        }

        JOCLGPUSearcher gpuSearcher = null;
        try {
            gpuSearcher = new JOCLGPUSearcher();
            searchers.add(gpuSearcher);
        } catch (Exception e) {
            System.err.println("GPU não disponível (JOCL): " + e.getMessage());
            System.err.println("A continuar apenas com CPU...");
        }

        StringBuilder csvContent = new StringBuilder();
        csvContent.append("Arquivo,Tamanho_Bytes,Algoritmo,Threads,Ocorrencias,Tempo_ms\n");

        byte[] wordBytes = SEARCH_WORD.getBytes(StandardCharsets.ISO_8859_1);
        System.out.println("=== Benchmark: Escalonamento de Threads e GPU ===");
        System.out.println("Núcleos de CPU disponíveis: " + maxCores);
        System.out.println("--------------------------------------------------");

        for (String filePath : FILES) {
            try {
                File f = new File(filePath);
                if (!f.exists()) {
                    System.err.println("Ficheiro não encontrado: " + filePath);
                    continue;
                }

                byte[] fullContent = Files.readAllBytes(Paths.get(filePath));

                System.out.println("A processar Ficheiro: " + f.getName());

                for (double percent : SAMPLES_PERCENT) {
                    int sampleSize = (int) (fullContent.length * percent);
                    byte[] sampleText = Arrays.copyOf(fullContent, sampleSize);

                    System.out.printf("  > Tamanho: %d%% (%d bytes)%n", (int)(percent*100), sampleSize);

                    for (ISearcher searcher : searchers) {
                        System.gc();

                        long startTime = System.nanoTime();
                        long count = searcher.search(sampleText, wordBytes);
                        long endTime = System.nanoTime();

                        long durationMs = (endTime - startTime) / 1_000_000;

                        System.out.printf("    %-22s: %5d ms | %d encontradas%n",
                                searcher.getName(), durationMs, count);

                        String threadInfo = "1";
                        if (searcher instanceof ParallelCPUSearcher) {
                            threadInfo = searcher.getName().replaceAll("\\D+", "");
                        } else if (searcher instanceof JOCLGPUSearcher) {
                            threadInfo = "GPU_Cores";
                        }

                        csvContent.append(String.format("%s,%d,%s,%s,%d,%d\n",
                                f.getName(), sampleSize, searcher.getName(), threadInfo, count, durationMs));
                    }
                    System.out.println();
                }

            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }

        for (ISearcher s : searchers) {
            if (s instanceof ParallelCPUSearcher) {
                ((ParallelCPUSearcher) s).shutdown();
            }
        }
        if (gpuSearcher != null) gpuSearcher.cleanup();

        saveCSV(csvContent.toString());

        System.out.println("--------------------------------------------------");
        System.out.println("A iniciar geração automática de gráficos...");
        try {
            ChartGenerator.generate();
        } catch (Exception e) {
            System.err.println("Erro ao gerar gráficos: " + e.getMessage());
            System.err.println("Verifique se a biblioteca JFreeChart está no classpath.");
        }
        System.out.println("Concluído.");
    }

    private static void saveCSV(String content) {
        String path = "res/resultados_busca_threads.csv";
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
            System.out.println("Resultados guardados em: " + path);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}