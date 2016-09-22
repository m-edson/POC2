package poc.gp;

/**
 * Created by edson on 10/06/16
 */
public enum ECJGpParams {

    EVALTHREADS("evalthreads"),
    BREEDTHREADS("breedthreads"),
    SILENT("silent"),
    GENERATION("generations"),
    POPULATION("pop.subpop.0.size"),
    ELITISM("breed.elite.0"),
    CROSSOVER_PROBABILITY("pop.subpop.0.species.pipe.source.0.prob"),
    MUTATION_PROBABILITY("pop.subpop.0.species.pipe.source.2.prob"),
    REPRODUCTION_PROBABILITY("pop.subpop.0.species.pipe.source.1.prob"),
    CROSSOVER_MAX_DEPTH("gp.koza.xover.maxdepth"),
    MUTATION_MAX_DEPTH("gp.koza.mutate.maxdepth"),
    BUILD_MAX_DEPTH("gp.koza.half.max-depth"),
    BUILD_MIN_DEPTH("gp.koza.half.min-depth"),
    TOURNAMENT_SIZE("select.tournament.size ");

    private final String name;

    ECJGpParams(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return otherName != null && name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
