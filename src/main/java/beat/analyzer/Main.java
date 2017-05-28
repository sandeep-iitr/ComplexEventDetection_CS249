package beat.analyzer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) {
	// write your code here
        int WINDOW_SIZE = 2500;
        String path = "Data/twa01.csv";
        System.out.println("Processing: " + path);
        List<Double> data = DataParser.ReadCSV(path);
        double[] da = new double[data.size()];
        for (int i = 0; i < da.length; i++){
            da[i] = data.get(i);
        }
        /* Read ABP data*/
        String pathABP = "Data/rec_1.csv";
        System.out.println("Processing: " + pathABP);
        List<Double> dataABP = DataParser.ReadCSV_ABP(pathABP);
        double[] daABP = new double[dataABP.size()];
        for (int i = 0; i < daABP.length; i++){
            daABP[i] = dataABP.get(i);
        }

        // create processing windows
        List<double[]> windows = SignalProcessing.makeWindow(da, WINDOW_SIZE);
        List<double[]> windowsABP = SignalProcessing.makeWindow(daABP, WINDOW_SIZE);
        List<HashMap> features = new ArrayList<>();
        for (int ind_win=0; ind_win<windows.size(); ind_win++) {
            System.out.println("Window #: " + ind_win);
            // find R-peaks
            double[] wind = windows.get(ind_win);
            double[] windABP = windowsABP.get(ind_win);
            int[] rPeaks = SignalProcessing.findRPeaks(wind);
            int[] pqrstPoints = SignalProcessing.findECGPoints(wind);
            double[] sdPressures = SignalProcessing.findBPValues(windABP);
            // features to stream
            HashMap<String, Double> windowFeatures
                    = new HashMap<>();
            windowFeatures.put("ECG_HR", Analysis.calcHR(rPeaks));// FIXME: add real hear-rate value
            windowFeatures.put("ECG_BASE", SignalProcessing.findBase(wind));// FIXME: add real hear-rate value
            windowFeatures.put("ECG_P_X", (double) (pqrstPoints[0]));
            windowFeatures.put("ECG_P_Y", wind[pqrstPoints[0]]);
            windowFeatures.put("ECG_Q_X", (double) (pqrstPoints[1]));
            windowFeatures.put("ECG_Q_Y", wind[pqrstPoints[1]]);
            windowFeatures.put("ECG_R_X", (double) (pqrstPoints[2]));
            windowFeatures.put("ECG_R_Y", wind[pqrstPoints[2]]);
            windowFeatures.put("ECG_S_X", (double) (pqrstPoints[3]));
            windowFeatures.put("ECG_S_Y", wind[pqrstPoints[3]]);
            windowFeatures.put("ECG_T_X", (double) (pqrstPoints[4]));
            windowFeatures.put("ECG_T_Y", wind[pqrstPoints[4]]);

            features.add(windowFeatures);
            // measure HRV
            double windowHRV = Analysis.calcHRV(rPeaks);

            // predict risk
            double windowRisk = Analysis.predictRisk(windowFeatures);
            System.out.println("HRV: " + windowHRV + " Risk: " + windowRisk);
            int a=0;
        }
        //DataParser.dumpToFile(Arrays.toString(windows.get(0)), "../run_output/window.csv");
        //DataParser.dumpToFile(Arrays.toString(res), "../run_output/Rpeaks.csv");
    }
}
