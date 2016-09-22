package poc.gp.ecj.terminals;

import ec.gp.GPNode;
import poc.dataObjects.Ranking;
import poc.gp.Parameters;
import poc.io.IO;

/**
 * Created by edson on 24/05/16
 */
public abstract class Rank extends GPNode {
    public static Ranking[] rankings = null;

    protected static synchronized Ranking[] getRankings() {
        if (rankings == null) {
            rankings = IO.readInputRanks(Parameters.ranksPath, Parameters.getProperty("sampling"), null);
//            ArrayList<Integer> ids = new ArrayList<>();
//            for (UserData ud : rankings[0].getRankings()) {
//                ids.add(ud.getUserId());
//            }
//            System.out.println(ids.size() + " " + ids);
        }
//        System.out.println(rankings[0].getRankings().size());
        return rankings;
    }

    public static void resetRankingSource() {
        rankings = null;
//        rankings = IO.readInputRanks(Parameters.ranksPath, Parameters.getProperty("sampling"), null);
    }
}
