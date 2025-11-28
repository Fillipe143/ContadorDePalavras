import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ChartGenerator {

    private static final String CSV_PATH = "res/resultados_busca_threads.csv";
    private static final String OUTPUT_DIR = "res/graficos";

    private static final Map<Integer, String> SAMPLE_LABELS = new HashMap<>();
    static {
        SAMPLE_LABELS.put(0, "10% (Pequeno)");
        SAMPLE_LABELS.put(1, "50% (Médio)");
        SAMPLE_LABELS.put(2, "100% (Completo)");
    }

    static class ResultData {
        String arquivo;
        long tamanhoBytes;
        String algoritmo;
        String threads;
        long tempoMs;

        public ResultData(String line) {
            String[] parts = line.split(",");
            this.arquivo = parts[0];
            this.tamanhoBytes = Long.parseLong(parts[1]);
            this.algoritmo = parts[2];
            this.threads = parts[3];
            this.tempoMs = Long.parseLong(parts[5]);
        }
    }

    public static void main(String[] args) {
        generate();
    }

    public static void generate() {
        System.out.println("Iniciando geração de gráficos para todas as amostras...");

        //noinspection ResultOfMethodCallIgnored
        new File(OUTPUT_DIR).mkdirs();

        List<ResultData> dataList = readCSV();
        if (dataList.isEmpty()) {
            System.err.println("Nenhum dado encontrado no CSV.");
            return;
        }

        Map<String, List<Long>> sizesPerFile = new HashMap<>();
        for (ResultData r : dataList) {
            sizesPerFile.computeIfAbsent(r.arquivo, k -> new ArrayList<>()).add(r.tamanhoBytes);
        }

        for (String file : sizesPerFile.keySet()) {
            List<Long> uniqueSorted = sizesPerFile.get(file).stream()
                    .distinct().sorted().collect(Collectors.toList());
            sizesPerFile.put(file, uniqueSorted);
        }

        int maxSamples = 3;

        for (int i = 0; i < maxSamples; i++) {
            String label = SAMPLE_LABELS.getOrDefault(i, "Amostra " + i);
            List<ResultData> currentSampleData = new ArrayList<>();

            for (ResultData r : dataList) {
                List<Long> sortedSizes = sizesPerFile.get(r.arquivo);
                if (i < sortedSizes.size() && r.tamanhoBytes == sortedSizes.get(i)) {
                    currentSampleData.add(r);
                }
            }

            if (!currentSampleData.isEmpty()) {
                System.out.println("Gerando gráficos para: " + label);
                createComparisonChart(currentSampleData, label, "comparacao_amostra_" + (i == 0 ? "10" : i == 1 ? "50" : "100") + ".png");
                createScalabilityChart(currentSampleData, label, "escalabilidade_amostra_" + (i == 0 ? "10" : i == 1 ? "50" : "100") + ".png");
            }
        }

        System.out.println("Todos os gráficos foram salvos em: " + OUTPUT_DIR);
    }

    private static List<ResultData> readCSV() {
        List<ResultData> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_PATH))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) list.add(new ResultData(line));
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler CSV: " + e.getMessage());
        }
        return list;
    }

    private static void createComparisonChart(List<ResultData> data, String sampleLabel, String filename) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<String, List<ResultData>> byFile = data.stream().collect(Collectors.groupingBy(r -> r.arquivo));

        for (String file : byFile.keySet()) {
            List<ResultData> results = byFile.get(file);

            ResultData serial = results.stream().filter(r -> r.algoritmo.equals("SerialCPU")).findFirst().orElse(null);
            ResultData gpu = results.stream().filter(r -> r.algoritmo.contains("GPU")).findFirst().orElse(null);
            ResultData bestParallel = results.stream()
                    .filter(r -> r.algoritmo.contains("ParallelCPU"))
                    .max(Comparator.comparingInt(r -> Integer.parseInt(r.threads)))
                    .orElse(null);

            if (serial != null) dataset.addValue(serial.tempoMs, "Serial CPU", file);
            if (bestParallel != null) dataset.addValue(bestParallel.tempoMs, "Melhor Parallel CPU", file);
            if (gpu != null) dataset.addValue(gpu.tempoMs, "Parallel GPU", file);
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Comparação de Desempenho - " + sampleLabel,
                "Arquivo", "Tempo (ms)", dataset,
                PlotOrientation.VERTICAL, true, true, false);

        customizePlot(barChart);
        saveChart(barChart, filename);
    }

    private static void createScalabilityChart(List<ResultData> data, String sampleLabel, String filename) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String largestFile = data.stream()
                .max(Comparator.comparingLong(r -> r.tamanhoBytes))
                .map(r -> r.arquivo).orElse("");

        List<ResultData> cpuData = data.stream()
                .filter(r -> r.arquivo.equals(largestFile))
                .filter(r -> r.algoritmo.contains("ParallelCPU"))
                .sorted(Comparator.comparingInt(r -> Integer.parseInt(r.threads)))
                .toList();

        for (ResultData r : cpuData) {
            dataset.addValue(r.tempoMs, "Tempo (" + largestFile + ")", r.threads);
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Escalabilidade CPU (Amdahl) - " + sampleLabel,
                "Número de Threads", "Tempo (ms)", dataset,
                PlotOrientation.VERTICAL, true, true, false);

        customizeLinePlot(lineChart);
        saveChart(lineChart, filename);
    }

    private static void customizePlot(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.lightGray);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(220, 53, 69));
        renderer.setSeriesPaint(1, new Color(40, 167, 69));
        renderer.setSeriesPaint(2, new Color(0, 123, 255));
        renderer.setDrawBarOutline(false);
        renderer.setItemMargin(0.05);
    }

    private static void customizeLinePlot(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.lightGray);

        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(102, 16, 242)); // Roxo
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);
    }

    private static void saveChart(JFreeChart chart, String fileName) {
        try {
            ChartUtils.saveChartAsPNG(new File(OUTPUT_DIR, fileName), chart, 1000, 600);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}