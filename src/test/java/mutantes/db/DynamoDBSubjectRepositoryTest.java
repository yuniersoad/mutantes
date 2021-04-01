package mutantes.db;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class DynamoDBSubjectRepositoryTest {

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

        final DynamoDBSubjectRepository.SubjectDynamoDB s = new DynamoDBSubjectRepository.SubjectDynamoDB(Arrays.asList(dna), true);
        assertEquals("a65d94c3e61fa21513338bde031f53064ad9cb63d8a9b4514e3869cc67db6d32", s.getId());
    }

}