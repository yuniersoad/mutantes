package mutantes.core;


import java.util.List;
import java.util.Objects;

public class Subject {
    private final List<String> dna;
    private final Boolean isMutant;

    public Subject(List<String> dna, boolean isMutant) {
        this.dna = dna;
        this.isMutant = isMutant;
    }

    public List<String> getDna() {
        return dna;
    }

    public Boolean getMutant() {
        return isMutant;
    }

   @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return isMutant.equals(subject.isMutant) && dna.equals(subject.dna);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isMutant, dna);
    }
}

