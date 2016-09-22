package poc.dataObjects;

/**
 * Created by edson on 10/06/16
 */
public class UserData {

    private int userId;
    //    private List<Rating> userData;

    public double userFitness;

    private CustomHashMap<Integer, Pair<Integer, Double>> hUserData;

    public CustomHashMap<Integer, Pair<Integer, Double>> gethUserData() {
        return hUserData;
    }

    public UserData(int userId, CustomHashMap<Integer, Pair<Integer, Double>> hUserData) {
        this.userId = userId;
        this.hUserData = hUserData;
    }


    public int getUserId() {
        return userId;
    }
}
