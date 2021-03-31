package mutantes.db;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SubjectTest {

    @Test
    public void testGetId() {
        String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
        };

        Subject s = new Subject(Arrays.asList(dna));
        assertEquals("a65d94c3e61fa21513338bde031f53064ad9cb63d8a9b4514e3869cc67db6d32", s.getId());
    }

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
        Subject s1 = new Subject(Arrays.asList(dna));
        Subject s2 = new Subject(Arrays.asList(Arrays.copyOf(dna, dna.length)));
        assertEquals(s1, s2);
    }
}