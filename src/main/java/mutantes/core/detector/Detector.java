package mutantes.core.detector;

import static java.lang.Character.toLowerCase;

public class Detector {

    public static final int MUTANT_SEQ_LENGTH = 4;
    public static final int MUTANT_MIN_NUM_SEQUENCES = 2;

    public static boolean isMutant(String[] dna){
        final int nRows = dna.length;
        final int nColumns = dna[0].length();

        int numNSequences = 0; //number of sequences of the desired length we have seen so far

        // Worst case all cells are inspected 4 times, so time complexity is O(nRows*NColumns) in constant space O(1)
        for (Direction direction : Direction.values()){
            int []moveDirection = direction.moveDirection;
            int[] start = direction.startEnum.start(nColumns);

            do {
                int n = 0; //number of times we have seen the same character
                char lastSeen = 0;

                int i = start[0];
                int j = start[1];
                while (i < nRows && i >= 0 && j < nColumns && j >= 0){
                    if(toLowerCase(dna[i].charAt(j)) == lastSeen || lastSeen == 0){
                        n += 1;
                    } else {
                        if (n == MUTANT_SEQ_LENGTH)
                            numNSequences++;
                        n = 1;
                    }
                    lastSeen = toLowerCase(dna[i].charAt(j));

                    i += moveDirection[0];
                    j += moveDirection[1];
                }

                // Check if a segment of the desired length was found at the end
                if (n == MUTANT_SEQ_LENGTH)
                    numNSequences++;

                if (numNSequences >= MUTANT_MIN_NUM_SEQUENCES)
                    return true;

                start = direction.startEnum.next(start[0], start[1], nRows, nColumns);
            } while (start != null);
        }

        return false;
    }
}
