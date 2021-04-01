package mutantes.db;

import mutantes.core.Subject;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface SubjectRepository {
    CompletionStage<Optional<Subject>> find(String[] dna);
    CompletionStage<Void> save(Subject subject);
}
