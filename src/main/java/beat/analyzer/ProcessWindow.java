package beat.analyzer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.event.globals.Globals;

public class ProcessWindow {

    public void process() {
	// write your code here
        int WINDOW_SIZE = Globals.size;//2500;
        
           Globals.ECG_values = SignalProcessing.listDoubleToArray(Globals.ECGlist);
        
            double[] wind = Globals.ECG_values;//windows.get(ind_win);
            //double[] windABP = windowsABP.get(ind_win);
            int[] rPeaks = SignalProcessing.findRPeaks(wind);
            int[] pqrstPoints = SignalProcessing.findECGPoints(wind);
         //   double[] sdPressures = SignalProcessing.findBPValues(windABP);
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

         // TODO: merge with server code
            // check validity of the window
            if (Analysis.checkECGFeatures(windowFeatures))
            {
                // measure HR
                double windowHR = Analysis.calcHR(rPeaks);
                // measure HRV
                double windowHRV = Analysis.calcHRV(rPeaks);
                //measure stress
                double stressIdx = Analysis.calcStress(windowHRV);
                
                System.out.println("Stress Idx: " + stressIdx);
                
                // predict risk
                double windowRisk = Analysis.predictRisk(windowFeatures);
                System.out.println(" HR: " + windowHR + " HRV: " + windowHRV + " Risk: " + windowRisk);
            }
            else
                System.out.println("Invalid Window, Skipped");
            
            /*
            //features.add(windowFeatures);
            // measure HRV
            double windowHRV = Analysis.calcHRV(rPeaks);

            // predict risk
            double windowRisk = Analysis.predictRisk(windowFeatures);
            System.out.println("HRV: " + windowHRV + " Risk: " + windowRisk);
            int a=0;
            */
       // }
        //DataParser.dumpToFile(Arrays.toString(windows.get(0)), "../run_output/window.csv");
        //DataParser.dumpToFile(Arrays.toString(res), "../run_output/Rpeaks.csv");
    }
}
