import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class ParallelCPUSearcher implements ISearcher {

    private final int threadCount;
    private final ForkJoinPool customThreadPool;

    public ParallelCPUSearcher(int threadCount) {
        this.threadCount = threadCount;
        this.customThreadPool = new ForkJoinPool(threadCount);
    }

    @Override
    public long search(byte[] text, byte[] word) {
        int n = text.length;
        int m = word.length;

        try {
            return customThreadPool.submit(() ->
                    IntStream.rangeClosed(0, n - m).parallel().filter(i -> {
                        for (int j = 0; j < m; j++) {
                            if (text[i + j] != word[j]) {
                                return false;
                            }
                        }
                        return true;
                    }).count()
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public String getName() {
        return "ParallelCPU-" + threadCount + "Threads";
    }

    public void shutdown() {
        customThreadPool.shutdown();
    }
}