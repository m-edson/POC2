package poc.dataObjects;

import java.util.*;

/**
 * Created by edson on 11/06/16
 */
public class CustomHashMap<X, Y> {
    private HashMap<X, Y> map;
    private ArrayList<X> insertHistory;

    public CustomHashMap() {
        map = new HashMap<>();
        insertHistory = new ArrayList<>();
    }

    public int size() {
        return map.size();
    }

    public Y get(Object key) {
        return map.get(key);
    }

    public Y put(X key, Y value) {
        if (!insertHistory.contains(key)) {
            insertHistory.add(key);
        }
        return map.put(key, value);
    }

    public ArrayList<X> orderedKeys() {
        return insertHistory;
    }

    public Set<X> keySet() {
        return map.keySet();
    }
}
