package WeightedKnn;

import FileInfo.FileInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public abstract class kNN {
    private kNN() {
    }

    //number of neighbours to check so we can classify
    private static int k;

    //training file
    private static FileInfo trainingFile;

    //the size of the vectors
    private static final int SIZE = 14;

    public static void Setup(int _k, FileInfo fileInfo) {
        k = _k;
        trainingFile = fileInfo;
    }

    public static String classifyPoint(FileInfo fileInfo) {

        Map<Double, String> map = new TreeMap<Double, String>();

        //TO-DO the actuall algorithm of Knn
        //have to return the string of the classification for each file
        for (int i = 0; i < trainingFile.getNumberOfExperiments(); i++) {
            map.put(calculateEuclideanDistance(trainingFile.getFeatureVectorForTraining()[i],fileInfo.getFeatureVector()), trainingFile.getExperiments()[i]);
        }

        String[] labels = new String[2];
        labels[0] = "EyesOpened";
        labels[1] = "EyesClosed";

        int[] counters = new int[2];
        double[] weights = new double[2];


//        System.out.println("Lets go have some fun with " + fileInfo.getFileName());
        int countK = 0;
        for (Map.Entry<Double, String> entry : map.entrySet()) {
            String experimentName = entry.getValue();
            Double distance = entry.getKey();

//            System.out.println("We have experiment " + experimentName + " with key " + distance);

            if (experimentName.contains("EyesOpened")) {
                counters[0]++;
                weights[0] += 1 / distance;
            } else if (experimentName.contains("EyesClosed")) {
                counters[1]++;
                weights[1] += 1 / distance;
            }

            countK++;
            if (countK == k)
                break;
        }

        if (weights[0] * counters[0] > weights[1] * counters[1]) {
//            System.out.println("we have guessed eyesOpened");
            return "EyesOpened";
        } else {
//            System.out.println("We have guesssed eyesClosed");
            return "EyesClosed";
        }
    }


    private static void swap(double x, double y) {
        double temp = x;
        x = y;
        y = temp;
    }

    //calculates the euclidean distance of 2 points
    private static double calculateEuclideanDistance(double[] x, double[] y) {

        //get the distances for each one of the 14 doubles
        double[] distances = new double[SIZE];
        for (int i = 0; i < SIZE; i++) {
            distances[i] = Math.abs(x[i] - y[i]);
        }
        /////

        //calculate the sum of their squares(all 14 doubles)
        double sum = 0.0;
        for (int i = 0; i < SIZE; i++) {
            sum += distances[i] * distances[i];
        }
        /////


        //return the square root of the sum of the distances (of all 14 doubles)
        return Math.sqrt(sum);
    }


}
