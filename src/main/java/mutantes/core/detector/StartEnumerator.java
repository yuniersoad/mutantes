package mutantes.core.detector;

/**
 * Abstracts how to enumerate the starts for each direction
 */
interface StartEnumerator {
    int[] start(int nc);

    int[] next(int i, int j, int nc, int nr);
}
