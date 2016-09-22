package poc.gp.ecj;

import com.jakewharton.fliptables.FlipTable;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.ADFStack;
import ec.gp.GPIndividual;
import ec.multiobjective.MultiObjectiveStatistics;
import ec.multiobjective.spea2.SPEA2MultiObjectiveFitness;
import ec.util.Parameter;
import poc.dataObjects.*;
import poc.gp.Aggr;
import poc.gp.Fitness;
import poc.gp.Parameters;
import poc.gp.ecj.terminals.Rank;
import poc.io.IO;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by edson on 14/09/16
 */
public class MyStatistics extends MultiObjectiveStatistics {

    private static int currentExecution;
    private static int currentPartition;
    private static String norm;


    private static ArrayList<GPIndividual> bestIndividuals = new ArrayList<>();

    public static void setCurrentPartition(int value) {
        currentPartition = value;
    }

    public static void setCurrentExecution(int value) {
        currentExecution = value;
    }

    public static void setNorm(String norm) {
        MyStatistics.norm = norm;
    }

    PrintWriter evolutionInfo = null;
    PrintWriter bestOfRun = null;
    public static PrintWriter summary = null;
    public static PrintWriter staticInfo = null;

    private static double[][][] results = null;


    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);

        String evoInfoFileName = "results_p" + currentPartition + "_e" + currentExecution;
        String bestOfRunFileName = "best_of_run_p" + currentPartition + "_e" + currentExecution;
        try {
            evolutionInfo = new PrintWriter("./out/results/" + evoInfoFileName, "UTF-8");
            bestOfRun = new PrintWriter("./out/results/" + bestOfRunFileName, "UTF-8");
            summary = new PrintWriter("./out/results/summary", "UTF-8");

            String dataSource = state.parameters.getProperty("dataSource");
            File f = new File("./out/results/staticInfo" + dataSource);
            if (!f.exists() && !f.isDirectory())
                staticInfo = new PrintWriter("./out/results/staticInfo" + dataSource, "UTF-8");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (results == null) {

            Integer partitions = Integer.parseInt((String) state.parameters.get("partitions"));
            Integer executions = Integer.parseInt((String) state.parameters.get("executions"));

            results = new double[partitions][executions][3];
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);

        Individual[] individuals = state.population.subpops[0].individuals;

        evolutionInfo.write("g" + (state.generation + 1) + "\n");
        for (Individual i : individuals) {
            double[] objectives = ((SPEA2MultiObjectiveFitness) i.fitness).getObjectives();
            evolutionInfo.write(objectives[0] + " " + objectives[1] + " " + objectives[2] + "\n");
        }

    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);

        printBestOfRun(state);

        evolutionInfo.close();
        bestOfRun.close();


    }

    private void printBestOfRun(EvolutionState state) {
        RankData input = new RankData();


        GPIndividual ind = null;
        Individual[] individuals = state.population.subpops[0].individuals;
        for (Individual i : individuals) {
            SPEA2MultiObjectiveFitness fitness = (SPEA2MultiObjectiveFitness) i.fitness;
            double accuracy = fitness.getObjectives()[0];

            if (ind == null) {
                ind = (GPIndividual) i;
            } else {
                if (((SPEA2MultiObjectiveFitness) ind.fitness).getObjectives()[0] < accuracy)
                    ind = (GPIndividual) i;
            }
        }

        results[currentPartition - 1][currentExecution - 1] = ((SPEA2MultiObjectiveFitness) ind.fitness).getObjectives();

        String sampling = Parameters.getProperty("sampling");
        Parameters.setProperty("sampling", "n");
        Rank.resetRankingSource();

        ind.trees[0].child.eval(state, 1, input, new ADFStack(), ind, new RankAggregationProblem());
        Ranking ranking = input.ranking;

        for (UserData userRanking : ranking.getRankings()) {
            bestOfRun.write(userRanking.getUserId() + "\t[");
            CustomHashMap<Integer, Pair<Integer, Double>> ratings = userRanking.gethUserData();
            Integer finalKey = ratings.orderedKeys().get(ratings.orderedKeys().size() - 1);
            for (Integer key : ratings.orderedKeys()) {
                bestOfRun.write(key + ":" + ratings.get(key).right);
                if (!key.equals(finalKey))
                    bestOfRun.write(",");
                else
                    bestOfRun.write("]\n");
            }
        }
        double[] validationObjectives = ((SPEA2MultiObjectiveFitness) ind.fitness).getObjectives();

//        Rank.resetRankingSource();
//        Parameters.basePath = Parameters.ranksPath + "reeval/";

        ind.trees[0].child.eval(state, 1, input, new ADFStack(), ind, new RankAggregationProblem());

        bestIndividuals.add(ind);
//        System.out.println("[" + objectivess[0] + "," + objectivess[1] + "," + objectivess[2] + "]");

        Parameters.setProperty("sampling", sampling);
        Rank.resetRankingSource();
    }

    private static String[][][] recVal() {

        String[][][] data = null;

        for (int p = 1; p <= 5; p++) {

            Parameters.basePath = "resources/data/" + Parameters.getProperty("dataSource") + "/";
            Parameters.ranksPath = Parameters.basePath + norm + "/";
            Parameters.completeUserRatingsFileName = "u" + p + ".base";
            Parameters.userRatingsFileName = "u" + p + ".validation";

            IO.resetData();
            Rank.resetRankingSource();
            Ranking[] rankings = IO.readInputRanks(Parameters.ranksPath, "n", p);

            UserRatings ratings = null;
            UserRatings completeRatings = null;
            UserRatings ratingsByItem = null;


            new RankAggregationProblem();

            ratings = RankAggregationProblem.getUserRatings();
            completeRatings = RankAggregationProblem.getCompleteRatingsList();
            ratingsByItem = RankAggregationProblem.getRatingsByItem();
            HashMap<Integer, Integer> ratingCount = RankAggregationProblem.getRatingCount();

            if (data == null) {
                data = new String[5][rankings.length][4];
            }
            int i = 0;

            for (Ranking r : rankings) {
                Pair<Double, Double> accuracyFitness = Fitness.map(r, ratings, Parameters.map_k);
                Pair<Double, Double> noveltyFitness = Fitness.novelty(r, completeRatings, ratingCount, Parameters.map_k);
                Pair<Double, Double> diversityFitness = Fitness.diversity(r, completeRatings, ratingsByItem, Parameters.map_k);

                data[p - 1][i][0] = String.valueOf(r.getRankName());
                data[p - 1][i][1] = String.valueOf(accuracyFitness.left);
                data[p - 1][i][2] = String.valueOf(noveltyFitness.left);
                data[p - 1][i][3] = String.valueOf(diversityFitness.left);
                i++;
            }
        }
        return data;
    }

    private static String[][][] recTest() {

        String[][][] data = null;

        for (int p = 1; p <= 5; p++) {

            if (!Parameters.ranksPath.endsWith("reeval/"))
                Parameters.ranksPath = Parameters.ranksPath + "reeval/";

            Parameters.completeUserRatingsFileName = "u" + p + ".base";
            Parameters.userRatingsFileName = "u" + p + ".test";

            IO.resetData();
            Rank.resetRankingSource();
            Ranking[] rankings = IO.readInputRanks(Parameters.ranksPath, "n", p);

            UserRatings ratings = null;
            UserRatings completeRatings = null;
            UserRatings ratingsByItem = null;
            HashMap<Integer, Integer> ratingCount = RankAggregationProblem.getRatingCount();

            new RankAggregationProblem();

            ratings = RankAggregationProblem.getUserRatings();
            completeRatings = RankAggregationProblem.getCompleteRatingsList();
            ratingsByItem = RankAggregationProblem.getRatingsByItem();

            if (data == null) {
                data = new String[5][rankings.length][4];
            }
            int i = 0;

            for (Ranking r : rankings) {
                Pair<Double, Double> accuracyFitness = Fitness.map(r, ratings, Parameters.map_k);
                Pair<Double, Double> noveltyFitness = Fitness.novelty(r, completeRatings, ratingCount, Parameters.map_k);
                Pair<Double, Double> diversityFitness = Fitness.diversity(r, completeRatings, ratingsByItem, Parameters.map_k);

                data[p - 1][i][0] = r.getRankName();
                data[p - 1][i][1] = String.valueOf(accuracyFitness.left);
                data[p - 1][i][2] = String.valueOf(noveltyFitness.left);
                data[p - 1][i][3] = String.valueOf(diversityFitness.left);
                i++;
            }
        }
        return data;
    }

    private static String[][][] aggrVal() {

        String[][][] data = null;

        for (int p = 1; p <= 5; p++) {

            Parameters.basePath = "resources/data/" + Parameters.getProperty("dataSource") + "/";
            Parameters.ranksPath = Parameters.basePath + norm + "/";
            Parameters.completeUserRatingsFileName = "u" + p + ".base";
            Parameters.userRatingsFileName = "u" + p + ".validation";

            IO.resetData();
            Rank.resetRankingSource();
            Ranking[] rankings = IO.readInputRanks(Parameters.ranksPath, "n", p);

            UserRatings ratings = null;
            UserRatings completeRatings = null;
            UserRatings ratingsByItem = null;


            new RankAggregationProblem();

            ratings = RankAggregationProblem.getUserRatings();
            completeRatings = RankAggregationProblem.getCompleteRatingsList();
            ratingsByItem = RankAggregationProblem.getRatingsByItem();
            HashMap<Integer, Integer> ratingCount = RankAggregationProblem.getRatingCount();

            ArrayList<Method> aggregationMethods = new ArrayList<>();
            Method[] methods = Aggr.class.getMethods();
            for (Method m : methods) {
                if (m.getReturnType() == Ranking.class)
                    aggregationMethods.add(m);
            }

            ArrayList<Ranking> resultRankings = new ArrayList<>();
            Object[] args = new Object[1];
            args[0] = new ArrayList<>(Arrays.asList(rankings));

            for (Method aggrMethod : aggregationMethods) {
                try {
                    resultRankings.add((Ranking) aggrMethod.invoke(null, args));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            resultRankings.sort((o1, o2) -> {
                String x = o1.getRankName().split(" ")[0].substring(1);
                String y = o2.getRankName().split(" ")[0].substring(1);
                return x.compareTo(y);
            });

            if (data == null) {
                data = new String[5][resultRankings.size()][4];
            }
            int i = 0;

            for (Ranking r : resultRankings) {

                Pair<Double, Double> accuracyFitness = Fitness.map(r, ratings, Parameters.map_k);
                Pair<Double, Double> noveltyFitness = Fitness.novelty(r, completeRatings, ratingCount, Parameters.map_k);
                Pair<Double, Double> diversityFitness = Fitness.diversity(r, completeRatings, ratingsByItem, Parameters.map_k);

                data[p - 1][i][0] = r.getRankName().split(" ")[0].substring(1);
                data[p - 1][i][1] = String.valueOf(accuracyFitness.left);
                data[p - 1][i][2] = String.valueOf(noveltyFitness.left);
                data[p - 1][i][3] = String.valueOf(diversityFitness.left);
                i++;
            }
        }
        return data;
    }

    private static String[][][] aggrTest() {

        String[][][] data = null;

        for (int p = 1; p <= 5; p++) {

            if (!Parameters.ranksPath.endsWith("reeval/"))
                Parameters.ranksPath = Parameters.ranksPath + "reeval/";

            Parameters.completeUserRatingsFileName = "u" + p + ".base";
            Parameters.userRatingsFileName = "u" + p + ".test";

            IO.resetData();
            Rank.resetRankingSource();
            Ranking[] rankings = IO.readInputRanks(Parameters.ranksPath, "n", p);


            UserRatings ratings = null;
            UserRatings completeRatings = null;
            UserRatings ratingsByItem = null;
            HashMap<Integer, Integer> ratingCount = RankAggregationProblem.getRatingCount();

            new RankAggregationProblem();

            ratings = RankAggregationProblem.getUserRatings();
            completeRatings = RankAggregationProblem.getCompleteRatingsList();
            ratingsByItem = RankAggregationProblem.getRatingsByItem();

            ArrayList<Method> aggregationMethods = new ArrayList<>();
            Method[] methods = Aggr.class.getMethods();
            for (Method m : methods) {
                if (m.getReturnType() == Ranking.class)
                    aggregationMethods.add(m);
            }

            ArrayList<Ranking> resultRankings = new ArrayList<>();
            Object[] args = new Object[1];
            args[0] = new ArrayList<>(Arrays.asList(rankings));


            for (Method aggrMethod : aggregationMethods) {
                try {
                    resultRankings.add((Ranking) aggrMethod.invoke(null, args));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            resultRankings.sort((o1, o2) -> {
                String x = o1.getRankName().split(" ")[0].substring(1);
                String y = o2.getRankName().split(" ")[0].substring(1);
                return x.compareTo(y);
            });


            if (data == null) {
                data = new String[5][resultRankings.size()][4];
            }
            int i = 0;

            for (Ranking r : resultRankings) {
                Pair<Double, Double> accuracyFitness = Fitness.map(r, ratings, Parameters.map_k);
                Pair<Double, Double> noveltyFitness = Fitness.novelty(r, completeRatings, ratingCount, Parameters.map_k);
                Pair<Double, Double> diversityFitness = Fitness.diversity(r, completeRatings, ratingsByItem, Parameters.map_k);

                data[p - 1][i][0] = r.getRankName().split(" ")[0].substring(1);
                data[p - 1][i][1] = String.valueOf(accuracyFitness.left);
                data[p - 1][i][2] = String.valueOf(noveltyFitness.left);
                data[p - 1][i][3] = String.valueOf(diversityFitness.left);
                i++;
            }
        }
        return data;
    }

    private static String[][][] gpVal(int partitions, int executions) {
        String[][][] data = null;

        for (int p = 1; p <= partitions; p++) {

            Parameters.basePath = "resources/data/" + Parameters.getProperty("dataSource") + "/";
            Parameters.ranksPath = Parameters.basePath + norm + "/";
            Parameters.completeUserRatingsFileName = "u" + p + ".base";
            Parameters.userRatingsFileName = "u" + p + ".validation";

            IO.resetData();
            Rank.resetRankingSource();
            Ranking[] rankings = IO.readInputRanks(Parameters.ranksPath, "n", p);

            UserRatings ratings = null;
            UserRatings completeRatings = null;
            UserRatings ratingsByItem = null;

            new RankAggregationProblem();

            ratings = RankAggregationProblem.getUserRatings();
            completeRatings = RankAggregationProblem.getCompleteRatingsList();
            ratingsByItem = RankAggregationProblem.getRatingsByItem();
            HashMap<Integer, Integer> ratingCount = RankAggregationProblem.getRatingCount();

            ArrayList<Ranking> resultRankings = new ArrayList<>();

            for (int e = 0; e < executions; e++) {
                GPIndividual ind = bestIndividuals.get(((p - 1) * (partitions - 1)) + e);
                RankData input = new RankData();
                ind.trees[0].child.eval(new EvolutionState(), 1, input, new ADFStack(), ind, new RankAggregationProblem());
                resultRankings.add(input.ranking);
            }

            if (data == null) {
                data = new String[partitions][executions][4];
            }

            int i = 0;
            for (Ranking r : resultRankings) {

                Pair<Double, Double> accuracyFitness = Fitness.map(r, ratings, Parameters.map_k);
                Pair<Double, Double> noveltyFitness = Fitness.novelty(r, completeRatings, ratingCount, Parameters.map_k);
                Pair<Double, Double> diversityFitness = Fitness.diversity(r, completeRatings, ratingsByItem, Parameters.map_k);

                data[p - 1][i][0] = "GP Exec. " + (i + 1);
                data[p - 1][i][1] = String.valueOf(accuracyFitness.left);
                data[p - 1][i][2] = String.valueOf(noveltyFitness.left);
                data[p - 1][i][3] = String.valueOf(diversityFitness.left);
                i++;
            }
        }
        return data;
    }

    private static String[][][] gpTest(int partitions, int executions) {
        String[][][] data = null;


        for (int p = 1; p <= partitions; p++) {

            if (!Parameters.ranksPath.endsWith("reeval/"))
                Parameters.ranksPath = Parameters.ranksPath + "reeval/";

            Parameters.completeUserRatingsFileName = "u" + p + ".base";
            Parameters.userRatingsFileName = "u" + p + ".test";

            IO.resetData();
            Rank.resetRankingSource();
            Ranking[] rankings = IO.readInputRanks(Parameters.ranksPath, "n", p);

            UserRatings ratings = null;
            UserRatings completeRatings = null;
            UserRatings ratingsByItem = null;

            new RankAggregationProblem();

            ratings = RankAggregationProblem.getUserRatings();
            completeRatings = RankAggregationProblem.getCompleteRatingsList();
            ratingsByItem = RankAggregationProblem.getRatingsByItem();
            HashMap<Integer, Integer> ratingCount = RankAggregationProblem.getRatingCount();

            ArrayList<Ranking> resultRankings = new ArrayList<>();

            for (int e = 0; e < executions; e++) {
                GPIndividual ind = bestIndividuals.get(((p - 1) * (partitions - 1)) + e);
                RankData input = new RankData();
                ind.trees[0].child.eval(new EvolutionState(), 1, input, new ADFStack(), ind, new RankAggregationProblem());
                resultRankings.add(input.ranking);
            }

            if (data == null) {
                data = new String[partitions][executions][4];
            }

            int i = 0;
            for (Ranking r : resultRankings) {

                Pair<Double, Double> accuracyFitness = Fitness.map(r, ratings, Parameters.map_k);
                Pair<Double, Double> noveltyFitness = Fitness.novelty(r, completeRatings, ratingCount, Parameters.map_k);
                Pair<Double, Double> diversityFitness = Fitness.diversity(r, completeRatings, ratingsByItem, Parameters.map_k);

                data[p - 1][i][0] = "GP Exec. " + (i + 1);
                data[p - 1][i][1] = String.valueOf(accuracyFitness.left);
                data[p - 1][i][2] = String.valueOf(noveltyFitness.left);
                data[p - 1][i][3] = String.valueOf(diversityFitness.left);
                i++;
            }
        }
        return data;
    }

    private static void genTables(String[][][] recVal, String[][][] aggrVal,
                                  String[][][] recTest, String[][][] aggrTest) {
        String[] headers = {"Alg. Name", "Acc(Validation)", "Acc(Test)", "Nov(Validation)", "Nov(Test)", "Div(Validation)", "Div(Test)"};

        int partitions = recVal.length;
        String values[][] = new String[recVal[0].length + aggrVal[0].length][7];
        for (int p = 0; p < partitions; p++) {
            int count = 0;
            for (int i = 0; i < recVal[0].length; i++) {
                values[count][0] = recVal[p][i][0];
                values[count][1] = recVal[p][i][1];
                values[count][2] = recTest[p][i][1];
                values[count][3] = recVal[p][i][2];
                values[count][4] = recTest[p][i][2];
                values[count][5] = recVal[p][i][3];
                values[count][6] = recTest[p][i][3];
                count++;
            }
            for (int i = 0; i < aggrVal[0].length; i++) {
                values[count][0] = aggrVal[p][i][0];
                values[count][1] = aggrVal[p][i][1];
                values[count][2] = aggrTest[p][i][1];
                values[count][3] = aggrVal[p][i][2];
                values[count][4] = aggrTest[p][i][2];
                values[count][5] = aggrVal[p][i][3];
                values[count][6] = aggrTest[p][i][3];
                count++;
            }
            staticInfo.write("<---------> Partition " + (p + 1) + " <--------->\n\n");
            staticInfo.write(FlipTable.of(headers, values));
            staticInfo.write("\n\n");
        }
    }

    private static void generateStaticInfo() {
        if (staticInfo != null) {
            Integer partitions = Integer.valueOf(Parameters.getProperty("partitions"));
            Integer executions = Integer.valueOf(Parameters.getProperty("executions"));

            System.out.println("Generating missing static info...");


            String[][][] recVal = recVal();
            String[][][] aggrVal = aggrVal();

            String[][][] recTest = recTest();
            String[][][] aggrTest = aggrTest();

            genTables(recVal, aggrVal, recTest, aggrTest);

            staticInfo.close();
        }
    }


    public static void generateSummaryFile(int partitions, int executions) {

        generateStaticInfo();

        String sampling = Parameters.getProperty("sampling");
        Parameters.setProperty("sampling", "n");

        String dataSource = Parameters.getProperty("dataSource");
        BufferedReader reader = null;

        ArrayList<ArrayList<String>> tables = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader("./out/results/staticInfo" + dataSource));
            String sCurrentLine;
            while ((sCurrentLine = reader.readLine()) != null) {
                ArrayList<String> table = new ArrayList<>();
                while (!sCurrentLine.contains("<-") && sCurrentLine.trim().length() != 0) {
                    table.add(sCurrentLine);
                    sCurrentLine = reader.readLine();
                }
                if (!table.isEmpty())
                    tables.add(table);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[][][] gpVal = gpVal(partitions, executions);
        String[][][] gpTest = gpTest(partitions, executions);

        String[] headers = {"Alg. Name", "Acc(Validation)", "Acc(Test)", "Nov(Validation)", "Nov(Test)", "Div(Validation)", "Div(Test)"};

        int part = gpVal.length;
        String gpValues[][] = new String[gpVal[0].length][7];
        for (int p = 0; p < part; p++) {
            int count = 0;
            for (int i = 0; i < gpVal[0].length; i++) {
                gpValues[count][0] = gpVal[p][i][0];
                gpValues[count][1] = gpVal[p][i][1];
                gpValues[count][2] = gpTest[p][i][1];
                gpValues[count][3] = gpVal[p][i][2];
                gpValues[count][4] = gpTest[p][i][2];
                gpValues[count][5] = gpVal[p][i][3];
                gpValues[count][6] = gpTest[p][i][3];
                count++;
            }

            String tableValues[][] = new String[tables.get(p).size() - 23 + gpValues.length][7];
            int i = -1;

            for (String s : tables.get(p).subList(3, tables.get(p).size())) {
                StringTokenizer st = new StringTokenizer(s, "╝╧╚╟╢╔═╗╪─┼╧╤╠╣║│ \t");
                int j = 0;
                if (st.hasMoreTokens())
                    i++;
                while (st.hasMoreTokens()) {
                    String str = st.nextToken();
                    tableValues[i][j] = str;
                    j++;
                }
            }

            i = 0;
            for (int j = tables.get(p).size() - 23; j < tables.get(p).size() - 23 + gpValues.length; j++) {
                tableValues[j] = gpValues[i];
                i++;
            }

            summary.write("\nPartition " + (p + 1) + "\n\n");
            summary.write(FlipTable.of(headers, tableValues));

        }
        summary.close();
    }
}
