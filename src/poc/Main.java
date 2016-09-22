package poc;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.ParameterDatabase;
import poc.gp.Parameters;
import poc.gp.ecj.MyStatistics;
import poc.gp.ecj.terminals.Rank;
import poc.io.IO;
import poc.util.CmdLineParser;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        Parameters p = new Parameters();

        CmdLineParser cmdLineParser = new CmdLineParser();
        cmdLineParser.parse(args, p);

        int partitions = 1;
        int executions = 1;
        if (Parameters.getProperty("executions") != null)
            executions = Integer.parseInt(Parameters.getProperty("executions"));

        if (Parameters.getProperty("partitions") != null)
            partitions = Integer.parseInt(Parameters.getProperty("partitions"));

        if (Parameters.getProperty("sampling") == null)
            Parameters.setProperty("sampling", "n");

        String baseName = "100k";
        if (Parameters.getProperty("dataSource") != null)
            baseName = Parameters.getProperty("dataSource");

        String norm = "score_norm";
        MyStatistics.setNorm(norm);

        p.generateECJParamFile("resources/parameters/", "params.properties");

        for (int currentPartition = 0; currentPartition < partitions; currentPartition++) {

            int cp = currentPartition + 1;

            Parameters.basePath = "resources/data/" + baseName + "/";
            Parameters.ranksPath = Parameters.basePath + norm + "/";
            Parameters.completeUserRatingsFileName = "u" + cp + ".base";
            Parameters.userRatingsFileName = "u" + cp + ".validation";

            IO.resetData();
            Rank.resetRankingSource();
            IO.readInputRanks(Parameters.ranksPath, "n", cp);

            System.out.println("Read Data...");

            File file = new File("resources/parameters/params.properties");
            ParameterDatabase pd = null;
            try {
                pd = new ParameterDatabase(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (Map.Entry<Object, Object> entry : Parameters.getProperties().entrySet()) {
                pd.putIfAbsent(entry.getKey(), entry.getValue());
            }

            for (int currentExecution = 0; currentExecution < executions; currentExecution++) {

                Rank.resetRankingSource();

                MyStatistics.setCurrentExecution(currentExecution + 1);
                MyStatistics.setCurrentPartition(currentPartition + 1);

                EvolutionState evolutionState = Evolve.initialize(pd, 0);
                evolutionState.run(EvolutionState.C_STARTED_FRESH);

                Evolve.cleanup(evolutionState);

                System.out.println("End exec " + (currentExecution + 1));

            }

            System.out.println("End part" + (currentPartition + 1));
            System.out.flush();

        }
        MyStatistics.generateSummaryFile(partitions, executions);

//        Runtime rt = Runtime.getRuntime();
//        try {
//            Process pr = rt.exec("python out/results/plot.py");
//
//            BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//
//            BufferedReader stdError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
//
//            String s = null;
//            while ((s = stdInput.readLine()) != null) {
//                System.out.println(s);
//            }
//
//            while ((s = stdError.readLine()) != null) {
//                System.out.println(s);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }

}
