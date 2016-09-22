package poc.dataObjects;

import java.util.HashMap;

/**
 * Created by edson on 24/05/16
 */
public class UserRatings {
    private HashMap<Integer, HashMap<Integer, Integer>> userRatings;

    public UserRatings(HashMap<Integer, HashMap<Integer, Integer>> userRatings) {
        this.userRatings = userRatings;
    }

    public HashMap<Integer,Integer> getUserRatingsById(Integer key){
        return userRatings.get(key);
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getUserRatings() {
        return userRatings;
    }
}
