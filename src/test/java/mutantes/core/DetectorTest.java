package mutantes.core;

import org.junit.Test;

import static mutantes.core.Detector.isMutant;
import static org.junit.Assert.*;

public class DetectorTest {

    @Test
    public void isMutantPositive() {
        final String[] sequence = new String[] {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
        };
        assertTrue(isMutant(sequence));
    }

    @Test
    public void isMutantPositiveOnlyHorizVert() {
        final String[] sequence = new String[] {
                "ATGCGA",
                "CGGTGC",
                "TTATGT",
                "AGAAGG",
                "AACCCC",
                "TCACTG"
        };
        assertTrue(isMutant(sequence));
    }

    @Test
    public void isMutantPositiveMixedCase() {
        final String[] sequence = new String[] {
                "atgCGA",
                "CaGTGC",
                "TTATGT",
                "agAAGG",
                "CccCTA",
                "TCACTG"
        };
        assertTrue(isMutant(sequence));
    }

    @Test
    public void isMutantPositive5Sequence() {
        // TODO: Ask if this 2 subseq of 4 in a 5 seq should be considered
        final String[] sequence = new String[] {
                "ATGCGA",
                "CAGTGC",
                "TTTTTC",
                "AGACGG",
                "GCGTCA",
                "TCACTG"
        };
        assertTrue(isMutant(sequence));
    }

    @Test
    public void isMutantOnlyDiagonalSeq() {
        final String[] sequence = new String[] {
                "ATGCAA",
                "CAGTGC",
                "TTAAAT",
                "AGAAGG",
                "CACCTA",
                "ACACTG"
        };
        assertTrue(isMutant(sequence));
    }

    @Test
    public void isMutantNegative() {
        final String[] sequence = new String[] {
                "ATGCGA",
                "CAGTGC",
                "TTATTT",
                "AGACGG",
                "GCGTCA",
                "TCACTG"
        };
        assertFalse(isMutant(sequence));
    }

    @Test
    public void isMutantNegativeOnlyOne4Seq() {
        final String[] sequence = new String[] {
                "ATGCGA",
                "CAGTGC",
                "TTATTT",
                "AGAAGG",
                "GCGTCA",
                "TCACTG"
        };
        assertFalse(isMutant(sequence));
    }
}