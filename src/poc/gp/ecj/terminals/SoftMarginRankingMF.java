package poc.gp.ecj.terminals;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import poc.dataObjects.Ranking;
import poc.dataObjects.RankingNames;
import poc.gp.ecj.RankData;

/**
 * Created by edson on 24/05/16
 */
public class SoftMarginRankingMF extends Rank {

    @Override
    public String toString() {
        return RankingNames.SOFT_MARGIN_RANKING_MF;
    }

    @Override
    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem) {
        RankData rd = ((RankData) (input));
//        int randInt = Math.abs(((RankAggregationProblem) problem).randInt);
        Ranking[] rankings = getRankings();

        for(Ranking r : rankings){
            if(r.getRankName().equals(this.toString())){
                rd.ranking = r;
                break;
            }
        }

//        rd.ranking = Util.selectRandomRanking(rankings, rankings.length, randInt);
    }


    public int expectedChildren() {
        return 0;
    }
}
