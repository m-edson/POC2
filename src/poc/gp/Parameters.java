package poc.gp;

import java.io.*;
import java.util.Properties;

/**
 * Created by edson on 30/05/16
 */
public class Parameters {
    public static int map_k;
    public static int ndcg_k;
    public static int prec_k;
    public static int rec_k;




    public static String ranksPath = "";
    public static String basePath = "";
    public static String userRatingsFileName = "";
    public static String completeUserRatingsFileName = "u.data";
    public static String ratingsByItemIdFileName = "u.iid.data";

    private static Properties properties = null;

    private Properties loadDefaultGPParameters() {
        Properties p = null;
        try {
            Reader r = new BufferedReader(new FileReader("resources/parameters/default.properties"));
            p = new Properties();
            p.load(r);
            r.close();
        } catch (FileNotFoundException e) {
            System.err.println("Missing default parameters file ...");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    public static Properties getProperties() {
        return properties;
    }

    public Parameters() {
        map_k = 10;
        ndcg_k = 10;
        prec_k = 10;
        rec_k = 10;
        properties = loadDefaultGPParameters();
    }

    public void generateECJParamFile(String path, String fileName) {
        try {
            File file = new File(path + fileName);

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            properties.store(bw, null);

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object setProperty(String key, String value) {
        return properties.setProperty(key, value);
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    private void setParameter(ECJGpParams parameter, String value) {
        properties.setProperty(parameter.toString(), value);
    }

    public void setEvalThreadCount(Integer value) {
        setParameter(ECJGpParams.EVALTHREADS, value.toString());
    }

    public void setBreedThreadCount(Integer value) {
        setParameter(ECJGpParams.BREEDTHREADS, value.toString());
    }

    public void setVerboseMode(Boolean value) {

        setParameter(ECJGpParams.SILENT, Boolean.valueOf(!value.booleanValue()).toString());
    }

    public void setGenerationCount(Integer value) {
        setParameter(ECJGpParams.GENERATION, value.toString());
    }

    public void setPopulationSize(Integer value) {
        setParameter(ECJGpParams.POPULATION, value.toString());
    }

    public void setEliteSize(Integer value) {
        setParameter(ECJGpParams.ELITISM, value.toString());
    }

    public void setCrossoverProb(Double value) {
        setParameter(ECJGpParams.CROSSOVER_PROBABILITY, value.toString());
    }

    public void setMutationProb(Double value) {
        setParameter(ECJGpParams.MUTATION_PROBABILITY, value.toString());
    }

    public void setReproductionProb(Double value) {
        setParameter(ECJGpParams.REPRODUCTION_PROBABILITY, value.toString());
    }

    public void setMaxIndividualHeight(Integer value) {
        setParameter(ECJGpParams.CROSSOVER_MAX_DEPTH, value.toString());
        setParameter(ECJGpParams.MUTATION_MAX_DEPTH, value.toString());
    }

    public void setMaxInitialHeight(Integer value) {
        setParameter(ECJGpParams.BUILD_MAX_DEPTH, value.toString());
    }

    public void setMinInitialHeight(Integer value) {
        setParameter(ECJGpParams.BUILD_MIN_DEPTH, value.toString());
    }

    public void setTournamentSize(Integer value) {
        setParameter(ECJGpParams.TOURNAMENT_SIZE, value.toString());
    }

}
