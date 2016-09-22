package poc.gp.ecj;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.multiobjective.spea2.SPEA2MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import poc.dataObjects.Pair;
import poc.dataObjects.UserData;
import poc.dataObjects.UserRatings;
import poc.gp.Fitness;
import poc.gp.Parameters;
import poc.io.IO;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by edson on 12/05/16
 */
public class RankAggregationProblem extends GPProblem implements SimpleProblemForm {

    public static final String P_DATA = "data";

    private static UserRatings ratings = null;
    private static UserRatings completeRatings = null;
    private static UserRatings ratingsByItem = null;
    private static HashMap<Integer, Integer> ratingCount = new HashMap<>();
    public static HashMap<Integer, Double> teste = new HashMap<>();

    public RankAggregationProblem() {
        super();
        ratings = null;
        completeRatings = null;
        ratingsByItem = null;
        ratingCount = new HashMap<>();
    }

    public void setup(final EvolutionState state, final Parameter base) {
        // very important, remember this
        super.setup(state, base);

        // verify our input is the right class (or subclasses from it)
        if (!(input instanceof RankData)) {
            state.output.fatal("GPData class must subclass from " + RankData.class,
                    base.push(P_DATA), null);
        }
    }

    @Override
    public void evaluate(final EvolutionState state,
                         final Individual ind,
                         final int subpopulation,
                         final int threadnum) {

        if (!ind.evaluated) // don't bother reevaluating
        {
            ratings = getUserRatings();
            completeRatings = getCompleteRatingsList();
            ratingsByItem = getRatingsByItem();

            RankData input = (RankData) (this.input);

            double result;

            ((GPIndividual) ind).trees[0].child.eval(
                    state, threadnum, input, stack, ((GPIndividual) ind), this);


            Pair<Double, Double> accuracyFitness = Fitness.map(input.ranking, ratings, Parameters.map_k);
            Pair<Double, Double> noveltyFitness = Fitness.novelty(input.ranking, completeRatings, ratingCount, Parameters.map_k);
            Pair<Double, Double> diversityFitness = Fitness.diversity(input.ranking, completeRatings, ratingsByItem, Parameters.map_k);

            if (state.parameters.get("sampling").equals("m3")) {
                for (UserData userData : input.ranking.getRankings()) {
                    Integer userId = userData.getUserId();
                    Double value = teste.get(userData.getUserId());
                    if (value == null) {
                        teste.put(userId, userData.userFitness);
                    } else {
                        teste.put(userId, value + userData.userFitness);
                    }
                }
            }

            result = 1 - accuracyFitness.left;

            //multiobjective fitness
            double[] fitnessValues = new double[3];
            fitnessValues[0] = 1 - result;
            fitnessValues[1] = noveltyFitness.left;
            fitnessValues[2] = diversityFitness.left;

            SPEA2MultiObjectiveFitness fmo = (SPEA2MultiObjectiveFitness) ind.fitness;
            fmo.setObjectives(state, fitnessValues);

            ind.evaluated = true;
        }

    }


    public static UserRatings getUserRatings() {
        if (ratings == null) {
            ratings = IO.readUserRatings(Parameters.basePath, Parameters.userRatingsFileName);
        }
        return ratings;
    }

    public static synchronized UserRatings getCompleteRatingsList() {
        if (completeRatings == null) {
            completeRatings = IO.readUserRatings(Parameters.basePath, Parameters.completeUserRatingsFileName);
            HashMap<Integer, HashMap<Integer, Integer>> userRatings = completeRatings.getUserRatings();
            Collection<HashMap<Integer, Integer>> values = userRatings.values();
            for (HashMap<Integer, Integer> rating : values) {
                Integer[] keys = rating.keySet().toArray(new Integer[rating.keySet().size()]);
                for (Integer key : keys) {
                    Integer count = ratingCount.get(key);
                    if (count == null) {
                        ratingCount.put(key, 1);
                    } else {
                        ratingCount.put(key, count + 1);
                    }
                }

            }
        }
        return completeRatings;
    }

    public static UserRatings getRatingsByItem() {
        if (ratingsByItem == null) {
            ratingsByItem = IO.readRatingsByItem(Parameters.basePath, Parameters.completeUserRatingsFileName);
        }
        return ratingsByItem;
    }

    public static HashMap<Integer, Integer> getRatingCount() {
        return ratingCount;
    }

}
