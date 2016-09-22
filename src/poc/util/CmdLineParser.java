package poc.util;

import org.apache.commons.cli.*;
import poc.gp.Parameters;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CmdLineParser {

    private static final Logger log = Logger.getLogger(CmdLineParser.class.getName());
    private Options options = new Options();

    public CmdLineParser() {

//        this.args = args;

        options.addOption("h", "help", false, "show help.");
        options.addOption("b", "base", true, "Data source name ('100k(Default)','1m' or 'bx')");
        options.addOption("ex", "execution", true, "Number of executions per partition");
        options.addOption("et", "evalThreads", true, "Number of Threads used to evaluate individuals(Default 8");
        options.addOption("bt", "breedThreads", true, "Number of Threads to breed individual (Default = 4)");
        options.addOption("v", "verbose", true, "Enable program output (Default = true)");
        options.addOption("g", "generation", true, "Number of generations (Default 100");
        options.addOption("p", "population", true, "Size of population (Default = 100)");
        options.addOption("e", "elitism", true, "Set the elite size (Default = 2)");
        options.addOption("t", "tournamentSize", true, "Set the number of individuals used in tournament selection (Default = 5)");
        options.addOption("cp", "crossoverProb", true, "Set the crossover probability (Default = 0.85)");
        options.addOption("mp", "mutationProb", true, "Set the mutation probability (Default = 0.15)");
        options.addOption("rp", "reproductionProb", true, "Set the reproduction probability (Default = 0.00) ");
        options.addOption("mh", "maxHeight", true, "Set the maximum individual height (Default = 7)");
        options.addOption(null, "initialMinHeight", true, "Set the initial population minimum height (Default = 2)");
        options.addOption(null, "initialMaxHeight", true, "Set the initial population maximum height (Default = 4)");
        options.addOption("sp", "sampling", true, "Set sampling mode:" +
                "\nn = don't use sampling (default)" +
                "\nsr = use sampling with replacement" +
                "\nm3 = Keep the worst individuals of the sample");
        options.addOption("ss", "sampleSize", true, "Number of rankings to use on sampling");
        options.addOption("rsi", "resamplingInterval", true, "Number generation between ech resampling (Default = 10)");
        options.addOption("part", "partitions", true, "Number of partitions to use");


    }

    public void parse(String[] args, Parameters p) {
        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
                help();
            if (cmd.hasOption("et"))
                p.setEvalThreadCount(Integer.valueOf(cmd.getOptionValue("et")));

            if (cmd.hasOption("bt"))
                p.setBreedThreadCount(Integer.valueOf(cmd.getOptionValue("bt")));

            if (cmd.hasOption("v"))
                p.setVerboseMode(Boolean.valueOf(cmd.getOptionValue("v")));

            if (cmd.hasOption("g"))
                p.setGenerationCount(Integer.valueOf(cmd.getOptionValue("g")));

            if (cmd.hasOption("p"))
                p.setPopulationSize(Integer.valueOf(cmd.getOptionValue("p")));

            if (cmd.hasOption("e"))
                p.setEliteSize(Integer.valueOf(cmd.getOptionValue("e")));

            if (cmd.hasOption("t"))
                p.setTournamentSize(Integer.valueOf(cmd.getOptionValue("t")));

            if (cmd.hasOption("initialMinHeight"))
                p.setMinInitialHeight(Integer.valueOf(cmd.getOptionValue("initialMinHeight")));

            if (cmd.hasOption("initialMaxHeight"))
                p.setMaxInitialHeight(Integer.valueOf(cmd.getOptionValue("initialMaxHeight")));

            if (cmd.hasOption("cp"))
                p.setCrossoverProb(Double.valueOf(cmd.getOptionValue("cp")));

            if (cmd.hasOption("mp"))
                p.setMutationProb(Double.valueOf(cmd.getOptionValue("mp")));

            if (cmd.hasOption("rp"))
                p.setReproductionProb(Double.valueOf(cmd.getOptionValue("rp")));

            if (cmd.hasOption("mh"))
                p.setMaxIndividualHeight(Integer.valueOf(cmd.getOptionValue("mh")));

            if (cmd.hasOption("b"))
                Parameters.setProperty("dataSource", cmd.getOptionValue("b"));

            if (cmd.hasOption("ex"))
                Parameters.setProperty("executions", cmd.getOptionValue("ex"));
            if (cmd.hasOption("sp"))
                Parameters.setProperty("sampling", cmd.getOptionValue("sp"));
            if (cmd.hasOption("ss"))
                Parameters.setProperty("sampleSize", cmd.getOptionValue("ss"));
            if (cmd.hasOption("rsi"))
                Parameters.setProperty("resamplingInterval", cmd.getOptionValue("rsi"));
            if (cmd.hasOption("part"))
                Parameters.setProperty("partitions", cmd.getOptionValue("part"));


        } catch (ParseException e) {
            log.log(Level.SEVERE, "Failed to parse comand line properties", e);
            help();
        }
    }

    private void help() {
        // This prints out some help
        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("Main", options);
        System.exit(0);
    }
}