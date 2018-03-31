package FileInfo;

import featureSelectionMetricsPackage.Entropy;

import java.io.*;

public class FileInfo {
    private final int COLUMNS = 14;
    private String fileName;
    private double[] featureVector;

    //variable that holds the name of each experiment in the training file
    private String[] experiments;
    private int numberOfExperiments;
    private double[][] featureVectorForTraining;

    public FileInfo() {
        fileName = "";
        featureVector = new double[COLUMNS];
        experiments = null;
        numberOfExperiments = 0;
        featureVectorForTraining = null;
    }

    public void CalculateFeatureVectorTestFiles(File file) {
        fileName = "../DataFinalforSoftwareDevelopment/" + file.getName();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(fileName));
            String line = "";
            String cvsSplitBy = ",";

            double[] sensorValues = new double[COLUMNS];
            int countLines = countFileLines();

            //matrix to hold all the values of all sensors
            double[][] allSensors = new double[countLines][COLUMNS];

            //get the values for each sensor
            br = new BufferedReader(new FileReader(fileName));
            br.readLine();
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                //get the sensor numbers for each line ---14 values
                String[] sensors = line.split(cvsSplitBy);

                for (int i = 0; i < COLUMNS; i++) {
                    sensorValues[i] = Double.parseDouble(sensors[i]);
                    allSensors[lineNumber][i] = sensorValues[i];
                }
                lineNumber++;
            }
            /////

            //get a matrix with column values to pass to the entropy
            //each column 1 sensor
            double[][] columnMatrix = new double[COLUMNS][countLines];
            for (int j = 0; j < COLUMNS; j++) {
                for (int i = 0; i < countLines; i++) {
                    columnMatrix[j][i] = allSensors[i][j];
                }
            }
            ////

//                        System.out.println("Entropy for file " + csvCurrentTestFile + " is ");
            //calculate the featureVecyor from the columnMatrix(entropies)
            for (int i = 0; i < COLUMNS; i++) {
                featureVector[i] = Entropy.calculateEntropy(columnMatrix[i]);
//                            System.out.println(entropy[i]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void CalculateFeatureVectorTrainingFile(File file) {
        fileName = "../TrainingSetforSoftwareDevelopment/" + file.getName();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = "";
            String csvSplitBy = ",";

            //count lines of training file
            int countLines = countFileLines();

            //lines of file are the number of experiments
            experiments = new String[countLines];

            //store the countlines variable
            numberOfExperiments = countLines;


            //current experiment number
            int experimentNumber = 0;

            //create the feature vector matrix for all the experiments
            //in the test file
            featureVectorForTraining = new double[numberOfExperiments][COLUMNS];


            //skip header
            br.readLine();

            //start reading file
            while ((line = br.readLine()) != null) {
                String[] entropies = line.split(csvSplitBy);

                //get the experiment number
                experiments[experimentNumber] = entropies[0];

                //get the entropies from the file
                for (int i = 1; i < COLUMNS + 1; i++) {
                    featureVectorForTraining[experimentNumber][i-1] = Double.parseDouble(entropies[i]);
                }

                experimentNumber++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int countFileLines() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));

        //skip firstline cause it is header -- not actuall data
        br.readLine();

        //start counting the lines of file that contain data
        int countLines = 0;
        while (br.readLine() != null) {
            countLines++;
        }
        return countLines;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public double[] getFeatureVector() {
        return featureVector;
    }

    public void setFeatureVector(double[] featureVector) {
        this.featureVector = featureVector;
    }

    public String[] getExperiments() {
        return experiments;
    }

    public void setExperiments(String[] experiments) {
        this.experiments = experiments;
    }

    public int getNumberOfExperiments() {
        return numberOfExperiments;
    }

    public void setNumberOfExperiments(int numberOfExperiments) {
        this.numberOfExperiments = numberOfExperiments;
    }

    public double[][] getFeatureVectorForTraining() {
        return featureVectorForTraining;
    }

    public void setFeatureVectorForTraining(double[][] featureVectorForTraining) {
        this.featureVectorForTraining = featureVectorForTraining;
    }
}
