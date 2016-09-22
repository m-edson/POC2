package poc.util;

/**
 * Created by edson on 11/05/16
 */
public class Util {


    public static double log(double value, double base) {
        return Math.log(value) / Math.log(base);
    }

    public static double getMean(Double[] data) {
        int size = data.length;

        double sum = 0.0;
        for (double a : data)
            sum += a;
        return sum / size;
    }

    public static double getVariance(Double[] data) {
        int size = data.length;

        double sum = 0.0;
        double mean = getMean(data);
        double temp = 0;
        for (double a : data)
            temp += (mean - a) * (mean - a);
        return temp / size;
    }

    public static double getStdDev(Double[] data) {
        return Math.sqrt(getVariance(data));
    }

    public static int getTreeHeight(String tree) {
        int current_height = 1;
        int height = current_height;
        for (int i = 0; i < tree.length(); i++) {
            if (tree.charAt(i) == '(') {
                current_height += 1;
                if (current_height > height) {
                    height = current_height;
                }
            } else if (tree.charAt(i) == ')') {
                current_height--;
            }
        }
        return height;
    }

}
