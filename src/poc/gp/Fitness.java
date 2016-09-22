package poc.gp;

import poc.dataObjects.Pair;
import poc.dataObjects.Ranking;
import poc.dataObjects.UserData;
import poc.dataObjects.UserRatings;
import poc.util.Util;

import java.util.*;

/**
 * Created by edson on 24/05/16
 */
public class Fitness {

    public static Pair<Double, Double> map(Ranking ranking, UserRatings userRatings, int k) {
        double score = 0.0;
        double hitCount = 0.0;

        ArrayList<Double> scores = new ArrayList<>();
//        ArrayList<Integer> ids = new ArrayList<>();
//
//        for (UserData userData : ranking.getRankings()) {
//            ids.add(userData.getUserId());
//        }
//        System.out.println(ids);

        for (UserData userData : ranking.getRankings()) {
            List<Integer> l = userData.gethUserData().orderedKeys().subList(0, k);
            int userId = userData.getUserId();
            HashMap<Integer, Integer> ur = userRatings.getUserRatingsById(userId);
            int i = 0;
            if (ur != null) {
                for (Integer key : l) {
                    if (ur.get(key) != null && !l.subList(0, i).contains(key)) {
                        hitCount += 1.0;
                        score += hitCount / (i + 1.0);
                    }
                    i++;
                }
                score = score / Math.min(ur.size(), k);
                scores.add(score);
                userData.userFitness = score;
                score = 0.0;
                hitCount = 0.0;
            } else {
//                scores.add(0.0);
//                System.out.println("user not in validation file");
            }

        }
        double acc = 0.0f;
        for (Double d : scores) {
            acc += d;
        }

        ArrayList<Double> sortedScores = new ArrayList<>();
        sortedScores.addAll(scores);
        Collections.sort(sortedScores);
        double median = 0.0f;
        if (sortedScores.size() % 2 == 0) {
            int i = sortedScores.size() / 2;
            median = (sortedScores.get(i) + sortedScores.get(i - 2)) / 2;
        } else {
            median = sortedScores.get(sortedScores.size() / 2);
        }


        Double[] data = new Double[scores.size()];
        data = scores.toArray(data);

        double stdDev = Util.getStdDev(data);

        double value = acc /
                scores.size();
        return new Pair<Double, Double>(value, stdDev);
    }

    public static Pair<Double, Double> ndcg(Ranking ranking, UserRatings userRatings, int k) {
        ArrayList<Double> ndcg = new ArrayList<>();

        double ideal_ndcg = 0.0f;
        for (int i = 0; i < k; i++) {
            if (i + 1 == 1)
                ideal_ndcg += i + 1;
            else
                ideal_ndcg += 1 / Util.log(i + 1, 2);
        }
        ideal_ndcg = 1 / ideal_ndcg;

        for (UserData userData : ranking.getRankings()) {
            HashMap<Integer, Integer> userRatingsById = userRatings.getUserRatingsById(userData.getUserId());
            if (userRatingsById != null) {
                int index = 1;
                double value = 0.0;
                List<Integer> userRanking = userData.gethUserData().orderedKeys().subList(0, k);
                for (Integer p : userRanking) {
                    Integer i = userRatingsById.get(p);
                    i = (i == null) ? 0 : 1;
                    value += (Math.pow(2, i) - 1) / (Util.log(index + 1, 2));
                    index++;
                }
                ndcg.add(value);
            }
        }
        double returnValue = 0.0;
        for (Double d : ndcg) {
            returnValue += d * ideal_ndcg;
        }
        returnValue = returnValue / ranking.getUserCount();

        Double[] data = new Double[ndcg.size()];
        data = ndcg.toArray(data);

        double stdDev = Util.getStdDev(data);

        return new Pair<>(returnValue, stdDev);
    }

    public static Pair<Double, Double> precision(Ranking ranking, UserRatings userRatings, int k) {
        ArrayList<Double> precision = new ArrayList<>();

        for (UserData userData : ranking.getRankings()) {
            HashMap<Integer, Integer> userRatingsById = userRatings.getUserRatingsById(userData.getUserId());
            if (userRatingsById != null) {
                List<Integer> userRanking = userData.gethUserData().orderedKeys().subList(0, k);
                double hits = 0.0;
                for (Integer p : userRanking) {
                    Integer i = userRatingsById.get(p);
                    if (i != null)
                        hits += 1.0;
                }
                precision.add(hits / k);
            }
        }
        double returnValue = 0.0;
        for (Double d : precision) {
            returnValue += d;
        }
        returnValue = returnValue / ranking.getUserCount();


        Double[] data = new Double[precision.size()];
        data = precision.toArray(data);

        double stdDev = Util.getStdDev(data);

        return new Pair<>(returnValue, stdDev);
    }

    public static Pair<Double, Double> recall(Ranking ranking, UserRatings userRatings, int k) {
        ArrayList<Double> recall = new ArrayList<>();

        for (UserData userData : ranking.getRankings()) {
            HashMap<Integer, Integer> userRatingsById = userRatings.getUserRatingsById(userData.getUserId());
            if (userRatingsById != null) {
                List<Integer> userRanking = userData.gethUserData().orderedKeys().subList(0, k);
                double hits = 0.0;
                for (Integer p : userRanking) {
                    Integer i = userRatingsById.get(p);
                    if (i != null)
                        hits += 1.0;
                }
                double temp = hits / userRatingsById.size();
                recall.add(temp);
            }
        }
        double returnValue = 0.0;
        for (Double d : recall) {
            returnValue += d;
        }
        returnValue = returnValue / ranking.getUserCount();


        Double[] data = new Double[recall.size()];
        data = recall.toArray(data);

        double stdDev = Util.getStdDev(data);

        return new Pair<>(returnValue, stdDev);
    }

    public static Pair<Double, Double> novelty(Ranking ranking, UserRatings userRatings, HashMap<Integer, Integer> ratingCount, int k) {
        ArrayList<Double> novelty = new ArrayList<>();

        double ideal_novelty = 0.0f;
        for (int i = 1; i <= k; i++) {
            ideal_novelty += discount(i);
        }
//        ideal_novelty = 1 / ideal_novelty;


        for (UserData userData : ranking.getRankings()) {
            HashMap<Integer, Integer> userRatingsById = userRatings.getUserRatingsById(userData.getUserId());
            double temp;
            if (userRatingsById != null) {
                temp = 0.0;
                List<Integer> userRanking = userData.gethUserData().orderedKeys().subList(0, k);
                int rating = 0;
                int j = 1;
                for (Integer p : userRanking) {
                    Integer i = userRatingsById.get(p);
                    if (i != null)
                        rating = i;
                    else
                        rating = 0;
                    int rc = 1;
                    if (ratingCount.get(p) != null)
                        rc = ratingCount.get(p);
                    temp += (discount(j) * relevance(rating) * (1 - (rc / (double) userRatings.getUserRatings().size())))
                    ;
                    j++;
                }
//                if (temp / ideal_novelty < 0) System.out.println("LOL");
                novelty.add(temp / ideal_novelty);
            }
        }
        double returnValue = 0.0;
        for (Double d : novelty) {
            returnValue += d;
        }
        returnValue = returnValue / ranking.getUserCount();


        Double[] data = new Double[novelty.size()];
        data = novelty.toArray(data);

        double stdDev = Util.getStdDev(data);

        return new Pair<>(returnValue, stdDev);
    }

    private static double distance(UserRatings ratingByItemId, int itemId1, int itemId2) {
        HashMap<Integer, HashMap<Integer, Integer>> ratings = ratingByItemId.getUserRatings();
        HashMap<Integer, Integer> r1 = ratings.get(itemId1);
        HashMap<Integer, Integer> r2 = ratings.get(itemId2);

        if (r1 == null)
            r1 = new HashMap<>();
        if (r2 == null)
            r2 = new HashMap<>();

        Set<Integer> userIds1 = new HashSet<Integer>();
        Set<Integer> userIds2 = new HashSet<Integer>();
        userIds1.addAll(r1.keySet());
        userIds2.addAll(r2.keySet());
        Set<Integer> result = null;
        if (r1.size() > r2.size()) {
            result = userIds1;
            result.retainAll(userIds2);
        } else {
            result = userIds2;
            result.retainAll(userIds1);
        }
        double v1 = Math.sqrt(r1.size());
        double v2 = Math.sqrt(r2.size());

        if (v1 == 0.0f)
            v1 = 1.0f;
        if (v2 == 0.0f)
            v2 = 1.0f;

        return 1 - (result.size() / (v1 * v2));
    }

    private static double discount(int index) {
        return Math.pow(0.85, index - 1);
    }

    private static double relevance(int rating) {
        if (rating == 0)
            return 0.15;
        else
            return Math.pow(1.65, rating) / Math.pow(1.65, 5);
    }

    public static Pair<Double, Double> diversity(Ranking ranking, UserRatings userRatings, UserRatings ratingByItemId, int k) {
        ArrayList<Double> diversity = new ArrayList<>();

        double ideal_diversity = 0.0f;
        for (int i = 0; i < k; i++) {
            for (int j = i; j < k; j++) {
                if (i != j) {
                    ideal_diversity += 1 / Util.log(i + 2, 2) * Util.log(Math.max(2, j - i), 2);
                }
            }
        }
//        ideal_novelty = 1 / ideal_novelty;


        for (UserData userData : ranking.getRankings()) {
            HashMap<Integer, Integer> userRatingsById = userRatings.getUserRatingsById(userData.getUserId());
            double temp;
            if (userRatingsById != null) {
                temp = 0.0;
                List<Integer> userRanking = userData.gethUserData().orderedKeys().subList(0, k);
                int ratingK = 0;
                int ratingL = 0;
                for (int a = 0; a < k; a++) {
                    for (int b = a; b < k; b++) {
                        if (a != b) {
                            int movieIdK = userRanking.get(a);
                            int movieIdL = userRanking.get(b);
                            Integer K = userRatingsById.get(movieIdK);
                            Integer L = userRatingsById.get(movieIdL);
                            if (K != null)
                                ratingK = K;
                            else
                                ratingK = 0;
                            if (L != null)
                                ratingL = L;
                            else
                                ratingL = 0;
                            double dist = distance(ratingByItemId, movieIdK, movieIdL);
                            temp += (dist * discount(a + 1) * discount(Math.max(1, b - a))) / discount(a + 1);
                        }
                    }
                }
                diversity.add(temp / ideal_diversity);
            }
        }
        double returnValue = 0.0;
        for (Double d : diversity) {
            returnValue += d;
        }
        returnValue = returnValue / ranking.getUserCount();


        Double[] data = new Double[diversity.size()];
        data = diversity.toArray(data);

        double stdDev = Util.getStdDev(data);

        return new Pair<>(returnValue, stdDev);
    }
}
