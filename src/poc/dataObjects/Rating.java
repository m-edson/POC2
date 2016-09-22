package poc.dataObjects;

/**
 * Created by edson on 07/05/16
 */

public class Rating implements Comparable {

    public Integer key;
    public Double value;

    public Rating(Integer key, double value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "(" + key + "," + value + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof Rating)) {
            return false;
        }
        Rating o = (Rating) other;
        boolean a = o.key.equals(this.key);
        boolean b = Double.compare(o.value, this.value) == 0;
        return a && b;
    }

    @Override
    public int hashCode() {
        final int prime = 13;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Rating) {
            Rating p = (Rating) o;
            if (this.value < p.value)
                return -1;
            if (this.value > p.value)
                return 1;
            return 0;
        } else
            throw new IllegalArgumentException("Invalid Type. Expected Rating");
    }
}