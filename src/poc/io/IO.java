package poc.io;

import poc.dataObjects.*;
import poc.gp.Parameters;

import java.io.*;
import java.util.*;

/**
 * Created by edson on 11/05/16
 */
public class IO {

    private static Ranking[] samplingBase = null;

    public static ArrayList<Integer> retain = null;

    public static Integer currentPartition = null;

    public static void resetData() {
        samplingBase = null;
        retain = null;
        currentPartition = null;
    }


    public static Ranking[] readInputRanks(String dirPath, String sampling, Integer partition) {
        if (samplingBase != null && (sampling.equals("sr") || sampling.equals("m3"))) {
            return getSample();
        } else {
            if (samplingBase != null) {
                return samplingBase;
            } else {
                File folder = new File(dirPath);
                File[] listOfFiles = folder.listFiles();

                ArrayList<File> files = new ArrayList<>();

                for (File file : listOfFiles) {
                    if (file.getName().contains("u" + partition))
                        files.add(file);
                }

                List<Ranking> rankings = new ArrayList<>();
                files.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));

                for (File file : files) {
                    if (file.isFile() && !file.isHidden()) {
                        rankings.add(readRanks(dirPath, file.getName(), sampling));
                    }
                }
                Ranking[] r = new Ranking[rankings.size()];
                r = rankings.toArray(r);
                samplingBase = r;
                return r;
            }
        }
    }

    private static Ranking readRanks(String path, String fileName, String sampling) {
        BufferedReader br = null;

        List<UserData> ranks = new ArrayList<>();

        try {
            String sCurrentLine;

            br = new BufferedReader(new FileReader(path + fileName));

            while ((sCurrentLine = br.readLine()) != null) {
                int i = 1;
                Integer userId;
//                List<Rating> scores = new ArrayList<>();
                CustomHashMap<Integer, Pair<Integer, Double>> hUserData = new CustomHashMap<>();

                String[] split = sCurrentLine.split("\n|\t| ");
                userId = new Integer(split[0]);
                split[1] = split[1].replaceAll("\\[|\\]", "");
                StringTokenizer st = new StringTokenizer(split[1], ",");
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    String[] values = s.split(":");
                    Integer id = new Integer(values[0]);
                    double score = new Double(values[1]);

                    hUserData.put(id, new Pair<>(i, score));
//                    scores.add(new Rating(id, score));
                    i++;
                }
                ranks.add(new UserData(userId, hUserData));

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        String rankName = fileName.substring(3, fileName.length() - 4);
        return new Ranking(ranks, rankName);
    }

    public static UserRatings readTitleRatings(String path, String fileName) {
        HashMap<Integer, HashMap<Integer, Integer>> userRatings = new HashMap<>();


        BufferedReader br = null;

        try {
            String sCurrentLine;

            br = new BufferedReader(new FileReader(path + fileName));
            Integer userId, itemID, rating;
            Integer actualItem = -1;
            HashMap<Integer, Integer> ratings = null;

            while ((sCurrentLine = br.readLine()) != null) {
                String[] tokens = sCurrentLine.split("\n|\t| ");
                userId = Integer.parseInt(tokens[0]);
                itemID = Integer.parseInt(tokens[1]);
                try {
                    rating = Integer.parseInt(tokens[2]);
                } catch (NumberFormatException e) {
                    rating = new Double(Double.parseDouble(tokens[2])).intValue();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new UserRatings(userRatings);
    }

    public static UserRatings readUserRatings(String path, String fileName) {
        HashMap<Integer, HashMap<Integer, Integer>> userRatings = new HashMap<>();


        BufferedReader br = null;

        try {
            String sCurrentLine;

            br = new BufferedReader(new FileReader(path + fileName));
            Integer userId, itemID, rating;
            Integer actualUser = -1;
            HashMap<Integer, Integer> ratings = null;

            while ((sCurrentLine = br.readLine()) != null) {

                String[] tokens = sCurrentLine.split("\n|\t| ");

                userId = Integer.parseInt(tokens[0]);
                itemID = Integer.parseInt(tokens[1]);
                try {
                    rating = Integer.parseInt(tokens[2]);
                } catch (NumberFormatException e) {
                    rating = new Double(Double.parseDouble(tokens[2])).intValue();
                }
                if (actualUser.intValue() != userId.intValue()) {
                    if (actualUser > 0) {
                        userRatings.put(actualUser, ratings);
                    }
                    actualUser = userId;
                    ratings = new HashMap<>();

                }
                ratings.put(itemID, rating);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return new UserRatings(userRatings);
    }

    public static UserRatings readRatingsByItem(String path, String fileName) {
        HashMap<Integer, HashMap<Integer, Integer>> userRatings = new HashMap<>();


        BufferedReader br = null;

        try {
            String sCurrentLine;

            br = new BufferedReader(new FileReader(path + fileName));
            Integer userId, itemID, rating;
            HashMap<Integer, Integer> ratings = null;

            while ((sCurrentLine = br.readLine()) != null) {

                String[] tokens = sCurrentLine.split("\n|\t| ");

                userId = Integer.parseInt(tokens[0]);
                itemID = Integer.parseInt(tokens[1]);
                try {
                    rating = Integer.parseInt(tokens[2]);
                } catch (NumberFormatException e) {
                    rating = new Double(Double.parseDouble(tokens[2])).intValue();
                }
                HashMap<Integer, Integer> user_RatingTuple = userRatings.get(itemID);
                if (user_RatingTuple == null) {
                    HashMap<Integer, Integer> hm = new HashMap<>();
                    hm.put(userId, rating);
                    userRatings.put(itemID, hm);
                } else {
                    user_RatingTuple.put(userId, rating);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new UserRatings(userRatings);
    }


    private static Ranking[] getSample() {
        ArrayList<Ranking> sample = new ArrayList<>(samplingBase.length);
        List<Integer> ids = new ArrayList<>();
        for (UserData ud : samplingBase[0].getRankings()) {
            ids.add(ud.getUserId());
        }

        int sampleSize = Integer.parseInt(Parameters.getProperty("sampleSize"));

        Collections.shuffle(ids);
        ids = ids.subList(0, sampleSize);

        if (retain != null && retain.size() > 0) {
            for (Integer id : retain) {
                if (!ids.contains(id)) {
                    ids.add(0, id);
                }
            }
        }

        ids = ids.subList(0, sampleSize);


        for (Ranking r : samplingBase) {
            List<UserData> dataSample = new ArrayList<>();
            for (UserData ud : r.getRankings()) {
                if (ids.contains(ud.getUserId()))
                    dataSample.add(ud);
            }
            sample.add(new Ranking(dataSample, r.getRankName()));
        }
        Ranking[] r = new Ranking[sample.size()];
        r = sample.toArray(r);
        return r;
    }

}
