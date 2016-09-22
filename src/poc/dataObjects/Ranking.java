package poc.dataObjects;

import java.util.List;

/**
 * Created by edson on 10/06/16
 */
public class Ranking {
    private String rankName;
    private List<UserData> rankings;
    private Integer rankSize;
    private Integer userCount;

    public Integer getRankSize() {
        return rankSize;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public String getRankName() {
        return rankName;
    }

    public Ranking() {
    }

//    public static Ranking copy(Ranking r){
//        Ranking ranking = new Ranking();
//        ranking.rankName = new String(r.getRankName());
//        ranking.rankSize = new Integer(r.getRankSize());
//        ranking.userCount = new Integer(r.getUserCount());
//        ranking.rankings = new ArrayList<>();
//        for (UserData u : r.rankings){
//            ranking.rankings.add(UserData.copy(u));
//        }
//        return  ranking;
//    }

    public Ranking(List<UserData> rankings, String rankName) {
        this.rankName = rankName;
        this.rankings = rankings;
        this.userCount = rankings.size();
        this.rankSize = rankings.get(0).gethUserData().size();
    }

    public List<UserData> getRankings() {
        return rankings;
    }
}
