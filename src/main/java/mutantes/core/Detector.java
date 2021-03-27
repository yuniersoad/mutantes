package mutantes.core;

public class Detector {
    private static int[][] HORIZONTAL = {{0,1}, {0,1}, {0,1}},
            VERTICAL = {{1,0}, {1,0}, {1,0}},
            DIAGONAL = {{1,1}, {1,1}, {1,1}},
            IDIAGONAL = {{-1,1}, {-1,1}, {-1,1}};

    private static int[][][] ALL_DIRECTIONS = {HORIZONTAL, VERTICAL, DIAGONAL, IDIAGONAL};

    public static boolean isMutant(String[] dna){
        final char[][] sequence = new char[dna.length][];
        for (int i = 0; i < dna.length; i++) {
            sequence[i] = dna[i].toUpperCase().toCharArray();
        }
        int num4sequences = 0;

        for (int i = 0; i < sequence.length; i++)
            for (int j = 0; j < sequence[i].length; j++)
                for (int[][] direction : ALL_DIRECTIONS)
                    if (isEqualSequence(sequence, i, j, direction))
                        if (++num4sequences > 1) return true;
        return false;
    }

    private static boolean isEqualSequence(char[][] sequence, int i, int j, int[][] direction) {
        final char baseValue = sequence[i][j];
        for (int[] move: direction) {
            i += move[0];
            j += move[1];
            if (i < 0 || i >= sequence.length || j < 0 || j >= sequence.length // Check bounds
                    || baseValue != sequence[i][j]){
                return false;
            }
        }
        return true;
    }
}
