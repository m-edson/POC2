package poc.gp;

import poc.dataObjects.*;

import java.util.*;
import java.util.function.Function;

/**
 * Created by edson on 11/06/16
 */
public class Aggr {
    private static double rrf_k = 60;

    private static Function<List<Pair<Integer, Double>>, Double> Max = (list) -> {
        double acc = -Double.MAX_VALUE;
        for (Pair<Integer, Double> v : list) {
            if (v != null && acc < v.right) {
                acc = v.right;
            }
        }
        return acc;
    };

    private static Function<List<Pair<Integer, Double>>, Double> Min = (list) -> {
        double acc = Double.MAX_VALUE;
        for (Pair<Integer, Double> v : list) {
            if (v != null && acc > v.right) {
                acc = v.right;
            }
        }
        return acc;
    };

    private static Function<List<Pair<Integer, Double>>, Double> Sum = (list) -> {
        double acc = 0.0;
        for (Pair<Integer, Double> v : list) {
            if (v != null) {
                acc += v.right;
            }
        }
        return acc;
    };

    private static Function<List<Pair<Integer, Double>>, Double> Med = (list) -> {
        Double sum = Sum.apply(list);
        Double size = (double) list.size();
        Double result = sum / size;
        return result;
    };

    private static Function<List<Pair<Integer, Double>>, Double> Anz = (list) -> {
        double hits = 0;
        for (Pair<Integer, Double> v : list) {
            if (v != null) {
                hits += 1;
            }
        }
        return Sum.apply(list) / hits;
    };

    private static Function<List<Pair<Integer, Double>>, Double> Mnz = (list) -> {
        double hits = 0;
        for (Pair<Integer, Double> v : list) {
            if (v != null) {
                hits += 1;
            }
        }
        return Sum.apply(list) * hits;
    };

    private static Function<List<Pair<Integer, Double>>, Double> ReciprocalRankingFusion = (list) -> {

        double acc = 0.0;
        for (Pair<Integer, Double> v : list) {
            if (v != null) {
                acc += 1 / (rrf_k + v.left);
            }
        }
        return acc;
    };

    public static Function<List<Pair<Integer, Double>>, Double> BordaCount = (list) -> {
        double acc = 0.0;
        for (Pair<Integer, Double> v : list) {
            if (v != null) {
                acc += 1 / (double) v.left;
            }
        }
        return acc;
    };

    public static Function<List<Pair<Integer, Double>>, Double> RLSimRankAggregation = (list) -> {
        double acc = 1.0;
        for (Pair<Integer, Double> v : list) {
            if (v != null) {
                acc *= v.right;
            }
        }
        return acc;
    };


    private static Comparator<Rating> pairSorterDec = (v1, v2) -> {
        if (v1.value < v2.value)
            return 1;
        if (v1.value > v2.value)
            return -1;
        return 0;
    };

    private static Ranking Aggregate(List<Ranking> rankings, String methodName,
                                     Function<List<Pair<Integer, Double>>, Double> aggregationMethod) {

        ArrayList<Iterator<UserData>> iterators = new ArrayList<>();

        for (Ranking r : rankings) {
            iterators.add(r.getRankings().iterator());
        }

        List<UserData> usersData = new ArrayList<>();

        while (iterators.parallelStream().allMatch(Iterator::hasNext)) {
            ArrayList<UserData> userDataList = new ArrayList<>();
            for (Iterator<UserData> it : iterators) {
                userDataList.add(it.next());
            }

            int userId = userDataList.get(0).getUserId();

            Set<Integer> keys = new HashSet<>();
            for (UserData ud : userDataList) {
                keys.addAll(ud.gethUserData().keySet());
            }


            ArrayList<Rating> temp = new ArrayList<>();
            for (Integer key : keys) {
                ArrayList<Pair<Integer, Double>> values = new ArrayList<>();
                for (UserData ud : userDataList) {
                    Pair<Integer, Double> integerDoublePair = ud.gethUserData().get(key);
                    values.add(integerDoublePair);
                }
                temp.add(new Rating(key, aggregationMethod.apply(values)));
                temp.sort(pairSorterDec);

            }
            CustomHashMap<Integer, Pair<Integer, Double>> data = new CustomHashMap<>();
            for (int i = 0; i < rankings.get(0).getRankSize(); i++) {
                Rating r = temp.get(i);
                data.put(r.key, new Pair<>(i + 1, r.value));
            }
//            if (data.size() < 100) {
//                System.out.println(methodName);
//            }
            usersData.add(new UserData(userId, data));
//            System.out.println("Done with user " + userId);
        }
        String rankName = "(" + methodName;
        for (Ranking r : rankings) {
            rankName += " " + r.getRankName();
        }
        rankName += ")";

        return new Ranking(usersData, rankName);
    }


    public static Ranking CombMax(List<Ranking> rankings) {
        return Aggregate(rankings, "CombMax", Max);
    }

    public static Ranking CombMin(List<Ranking> rankings) {
        return Aggregate(rankings, "CombMin", Min);
    }

    public static Ranking CombSum(List<Ranking> rankings) {
        return Aggregate(rankings, "CombSum", Sum);
    }

    public static Ranking CombMed(List<Ranking> rankings) {
        return Aggregate(rankings, "CombMed", Med);
    }

    public static Ranking CombAnz(List<Ranking> rankings) {
        return Aggregate(rankings, "CombAnz", Anz);
    }

    public static Ranking CombMnz(List<Ranking> rankings) {
        return Aggregate(rankings, "CombMnz", Mnz);
    }

    public static Ranking ReciprocalRankingFusion(List<Ranking> rankings) {
        return Aggregate(rankings, "ReciprocalRankingFusion", ReciprocalRankingFusion);
    }

    public static Ranking BordaCount(List<Ranking> rankings) {
        return Aggregate(rankings, "BordaCount", BordaCount);
    }

    public static Ranking RLSimRankAggregation(List<Ranking> rankings) {
        return Aggregate(rankings, "RLSimRankAggregation", RLSimRankAggregation);
    }

    public static Ranking MedianRankAggregation(List<Ranking> rankings) {

        ArrayList<Iterator<UserData>> iterators = new ArrayList<>();

        LinkedHashMap<Integer, Integer> counter;

        double median = rankings.size() / 2.0;

        ArrayList<UserData> newData = new ArrayList<>();

        for (Ranking r : rankings) {
            iterators.add(r.getRankings().iterator());
        }

        while (iterators.stream().allMatch(Iterator::hasNext)) {
            ArrayList<UserData> userDataList = new ArrayList<>();
            for (Iterator<UserData> it : iterators) {
                userDataList.add(it.next());
            }

//            iterators.stream().map(Iterator<UserData>::next).collect(Collectors.toCollection(ArrayList::new));

            counter = new LinkedHashMap<>();
            ArrayList<ArrayList<Integer>> orderedKeys = new ArrayList<>();

            for (UserData ud : userDataList) {
//                System.out.println(ud.getUserId() + " - " + ud.gethUserData().size());
                orderedKeys.add(ud.gethUserData().orderedKeys());
            }


            List<Integer> temp = new ArrayList<>();

            for (int i = 0; i < rankings.get(0).getRankSize(); i++) {
                for (int j = 0; j < userDataList.size(); j++) {
                    int key = orderedKeys.get(j).get(i);
                    Pair<Integer, Double> integerDoublePair = userDataList.get(j).gethUserData().get(key);
//                    System.out.println("List = " + j + " Column = " + i + " = (" + key + " ," + integerDoublePair + ")");

                    Integer value = counter.get(key);
                    if (value == null)
                        value = 0;
                    value += 1;
                    if (value > median && !temp.contains(key)) {
                        temp.add(key);
                    }
                    counter.put(key, value);
                }
            }

            Set<Integer> keys = counter.keySet();
            double backupSize = median - 1;
            while (temp.size() < rankings.get(0).getRankSize()) {
                for (Integer key : keys) {
                    Integer value = counter.get(key);
                    if (temp.size() == rankings.get(0).getRankSize()) {
                        break;
                    }
                    if (value > backupSize && value <= median) {
                        temp.add(key);
                    }
                }
                backupSize = backupSize - 1;
            }

            for (ArrayList<Integer> orderedKey : orderedKeys) {
                ArrayList<Integer> x = (ArrayList<Integer>) orderedKey.clone();
                x.removeAll(temp);
                temp.addAll(x);
            }

            CustomHashMap<Integer, Pair<Integer, Double>> newUserData = new CustomHashMap<>();
            for (int i = 0; i < rankings.get(0).getRankSize(); i++) {
                newUserData.put(temp.get(i), new Pair<>(i + 1, 1 - (i / (double) rankings.get(0).getRankSize())));
            }
            newData.add(new UserData(userDataList.get(0).getUserId(), newUserData));

        }
        String rankName = "(MedianRankAggregation";
        for (Ranking r : rankings) {
            rankName += " " + r.getRankName();
        }
        rankName += ")";
        return new Ranking(newData, rankName);
    }

    public static Ranking OutRank(List<Ranking> rankings) {
        Integer rankSize = rankings.size();
        Integer preferenceThreshold = (int) (rankSize * 0.25);
        Integer vetoThreshold = (int) (rankSize * 0.8);
        Integer concordanceThreshold = (int) (rankSize * 0.5);
        Integer discordanceThreshold = (int) (rankSize * 0.25);

        ArrayList<Iterator<UserData>> iterators = new ArrayList<>();
        for (Ranking r : rankings) {
            iterators.add(r.getRankings().iterator());
        }

        List<UserData> usersData = new ArrayList<>();

        while (iterators.stream().allMatch(Iterator::hasNext)) {
            Set<Integer> aux = new HashSet<>();
            ArrayList<UserData> userDataList = new ArrayList<>();

            for (Iterator<UserData> it : iterators) {
                userDataList.add(it.next());
            }

            for (UserData ud : userDataList) {
                aux.addAll(ud.gethUserData().orderedKeys());
            }
            ArrayList<Integer> items = new ArrayList<>(aux);
            int matrixSize = items.size();

            int[][] rankingsMatrix = new int[userDataList.size()][matrixSize];
            int[][] concordanceMatrix = new int[matrixSize][matrixSize];
            int[][] discordanceMatrix = new int[matrixSize][matrixSize];
            int[][] outRankingMatrix = new int[matrixSize][matrixSize];

            fillArray(rankingsMatrix, -1);
            fillArray(concordanceMatrix, -1);
            fillArray(discordanceMatrix, -1);
            fillArray(outRankingMatrix, -1);

            for (int i = 0; i < userDataList.size(); i++) {
                for (int j = 0; j < items.size(); j++) {
                    Pair<Integer, Double> data = userDataList.get(i).gethUserData().get(items.get(j));
                    if (data != null) {
                        rankingsMatrix[i][j] = data.left;
                    }
                }
            }

            for (int i = 0; i < matrixSize; i++) {
                for (int j = 0; j < matrixSize; j++) {
                    if (i == j) continue;
                    int concordance = 0;
                    int discordance = 0;
                    for (int k = 0; k < userDataList.size(); k++) {
                        int v1 = rankingsMatrix[k][i];
                        int v2 = rankingsMatrix[k][j];
                        if (v1 != -1 && v2 != -1) {
                            if (v1 <= v2 - preferenceThreshold) {
                                concordance++;
                            } else if (v1 >= v2 + vetoThreshold) {
                                discordance++;
                            }
                        }
                    }
                    concordanceMatrix[i][j] = concordance;
                    discordanceMatrix[i][j] = discordance;

                }
            }

            for (int i = 0; i < matrixSize; i++) {
                for (int j = 0; j < matrixSize; j++) {
                    if (i == j) continue;
                    int value = (concordanceMatrix[i][j] >= concordanceThreshold) && (discordanceMatrix[i][j] <= discordanceThreshold) ? 1 : 0;
                    outRankingMatrix[i][j] = value;
                }
            }

            ArrayList<Pair<Integer, Integer>> temp = new ArrayList<>();
            for (int i = 0; i < matrixSize; i++) {
                int line = 0;
                int column = 0;
                for (int j = 0; j < matrixSize; j++) {
                    if (outRankingMatrix[i][j] > 0) line++;
                }
                for (int j = 0; j < matrixSize; j++) {
                    if (outRankingMatrix[j][i] > 0) column++;
                }
                temp.add(new Pair<>(i, line - column));
            }
            temp.sort(sorter);
            CustomHashMap<Integer, Pair<Integer, Double>> data = new CustomHashMap<>();
            ArrayList<Pair<Integer, Double>> newData = new ArrayList<>();
            for (int i = 0; i < (double) rankings.get(0).getRankSize(); i++) {
                data.put(items.get(temp.get(i).left), new Pair<>(i + 1, 1 - i / (double) rankings.get(0).getRankSize()));
            }
            usersData.add(new UserData(userDataList.get(0).getUserId(), data));
        }
        String rankName = "(OutRank";
        for (Ranking r : rankings) {
            rankName += " " + r.getRankName();
        }
        rankName += ")";
        return new Ranking(usersData, rankName);
    }

    private static void fillArray(int[][] arr, int value) {
        for (int[] x : arr) {
            Arrays.fill(x, -1);
        }
    }

    private static Comparator<Pair<Integer, Integer>> sorter = (v1, v2) -> {
        if (v1.right < v2.right)
            return 1;
        if (v1.right > v2.right)
            return -1;
        return 0;
    };
}
