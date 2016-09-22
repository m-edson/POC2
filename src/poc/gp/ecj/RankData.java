package poc.gp.ecj;

/**
 * Created by edson on 12/05/16
 */

import ec.gp.GPData;
import poc.dataObjects.Ranking;

public class RankData extends GPData {

    public RankData() {

    }

    public RankData(Ranking x) {
        this.ranking = x;
    }

    public Ranking ranking;    // return value

    public void copyTo(final GPData gpd)   // copy my stuff to another DoubleData
    {
        ((RankData) gpd).ranking = ranking;
    }
}