public class SerialCPUSearcher implements ISearcher {
    @Override
    public long search(byte[] text, byte[] word) {
        long count = 0;
        int n = text.length;
        int m = word.length;

        for (int i = 0; i <= n - m; i++) {
            boolean match = true;
            for (int j = 0; j < m; j++) {
                if (text[i + j] != word[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String getName() {
        return "SerialCPU";
    }
}