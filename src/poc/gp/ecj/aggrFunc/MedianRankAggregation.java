package poc.gp.ecj.aggrFunc;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import poc.dataObjects.Ranking;
import poc.gp.Aggr;
import poc.gp.ecj.RankData;

import java.util.ArrayList;

/**
 * Created by edson on 29/05/16
 */

public class MedianRankAggregation extends GPNode {
    @Override
    public String toString() {
        return "MedianRankAggregation";
    }

    @Override
    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem) {

        RankData rd = ((RankData) (input));

        ArrayList<Ranking> rankings = new ArrayList<>();

        for (GPNode g : children){
            g.eval(state, thread, input, stack, individual, problem);
            rankings.add(rd.ranking);
        }

//        this.children[0].eval(state, thread, input, stack, individual, problem);
//        rankings.add(rd.ranking);
//
//        this.children[1].eval(state, thread, input, stack, individual, problem);
//        rankings.add(rd.ranking);

        rd.ranking = Aggr.MedianRankAggregation(rankings);
    }

    public int expectedChildren() {
        return this.children.length;
    }
}
