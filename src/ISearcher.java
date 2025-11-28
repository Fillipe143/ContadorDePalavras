public interface ISearcher {
    long search(byte[] text, byte[] word);

    String getName();
}