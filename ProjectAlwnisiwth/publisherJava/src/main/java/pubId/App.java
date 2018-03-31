package pubId;

import FileInfo.FileInfo;
import WeightedKnn.kNN;
import sender.Sender;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * Publisher in MqttApp
 */
public class App {
    final static int COLUMNS = 14;

    public static void main(String[] args) {

        //get a list of all the test files
        File folder = new File("../DataFinalforSoftwareDevelopment");
        File[] listOfFiles = folder.listFiles();

        //strings to store the classes that the kNN will return for each file
        String[] fileClass = null;

        fileClass = classification(listOfFiles);

        //create a buffer to store the strings from the classification
        //to sent to the android app
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(listOfFiles.length);

        //create the thread that gets the head of the queue and sends it to the android app
        Sender sender = new Sender(blockingQueue);
        //start the sender thread
        new Thread(sender).start();

        for (int i = 0; i < listOfFiles.length; i++) {
            try {
                blockingQueue.put(fileClass[i]);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Interrupted while putting elements");
            }
        }
    }

    private static String[] classification(File[] listOfFiles) {
        String[] fileClass = new String[listOfFiles.length];

        //get the info from the training file
        File trainingFile = new File("../TrainingSetforSoftwareDevelopment/TrainingSet.csv");
        FileInfo traingingFileInfo = new FileInfo();
        traingingFileInfo.CalculateFeatureVectorTrainingFile(trainingFile);
        //

        //Setup Knn parameters
        kNN.Setup(11, traingingFileInfo);

        //custom Class for holding information about each csv
        FileInfo[] testFileInfos = new FileInfo[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++)
            testFileInfos[i] = new FileInfo();


        int hitCounter = 0;
        //get the actuall Info from the files and store them in
        //our objects
        for (int w = 0; w < listOfFiles.length; w++) {
            if (listOfFiles[w].isFile()) {
                testFileInfos[w].CalculateFeatureVectorTestFiles(listOfFiles[w]);
                //classify each file
                fileClass[w] = kNN.classifyPoint(testFileInfos[w]);
                if (fileClass[w].equals("EyesOpened") && testFileInfos[w].getFileName().contains("EyesOpened")) {
//                        System.out.println("Eyes opened CORRECT");
                    fileClass[w] = fileClass[w].concat(" Success-- Classification class : EyesOpened , Real Class(filename) : ");
                    String[] parts = testFileInfos[w].getFileName().split("/");

                    fileClass[w] = fileClass[w].concat(parts[2]);
                    hitCounter++;
//                        System.out.println(hitCounter);
                } else if (fileClass[w].equals("EyesClosed") && testFileInfos[w].getFileName().contains("EyesClosed")) {
//                        System.out.println("Eyes closed CORRECT");
                    fileClass[w] = fileClass[w].concat(" Success-- Classification class : EyesClosed , Real Class(filename) : ");

                    String[] parts = testFileInfos[w].getFileName().split("/");
                    fileClass[w] = fileClass[w].concat(parts[2]);
                    hitCounter++;
//                        System.out.println(hitCounter);
                } else {
                    String temp = new String(fileClass[w]);
                    fileClass[w] = fileClass[w].concat(" Failed -- Classification class :");
                    fileClass[w] = fileClass[w].concat(temp);
                    fileClass[w] = fileClass[w].concat(" , Real Class(filename) : ");
                    String[] parts = testFileInfos[w].getFileName().split("/");
                    fileClass[w] = fileClass[w].concat(parts[2]);
//                        System.out.println("We have failed");
                }
                ////
            }
        }

        System.out.println("Percent we guessed correctly : " + (int) (((double) hitCounter / (double) listOfFiles.length) * 100));
        ////
        return fileClass;
    }
}