package mutantes.core;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class SubjectTest {


    @Test
    public void testEquality(){
        String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
        };
        Subject s1 = new Subject(Arrays.asList(dna), true);
        Subject s2 = new Subject(Arrays.asList(Arrays.copyOf(dna, dna.length)), true);
        assertEquals(s1, s2);
    }
}