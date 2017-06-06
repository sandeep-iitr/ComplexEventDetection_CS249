package beat.analyzer;

import sun.misc.Signal;


import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.collections4.queue.CircularFifoQueue;

/**
 * Created by mohammad on 5/27/17.
 */

public class Analysis {
    // TODO: merge with the server code
    public static double predictRisk(HashMap<String, Double> features){

        // measure risk using Naive Bayes algorithm
        double risk = 1.0;


        // hear-rate > 80bpm, 12.9, 11.9
        if (features.get("ECG_HR") > 80.0) {
            risk *= (12.9) / (12.9 + 11.9);
        }
        else{
            risk *= (11.9) / (12.9 + 11.9);
        }

        // QRS > 100 ms, 12.9, 10.0
        if (samples2ms(features.get("ECG_S_X")-features.get("ECG_Q_X")) > 100.0) {
            risk *= (12.9) / (12.9 + 10.0);
        }
        else{
            risk *= (10.0) / (12.9 + 10.0);
        }

        // QRS > 120 ms, 13.3, 10.6
        if (samples2ms(features.get("ECG_S_X")-features.get("ECG_Q_X")) > 120.0) {
            risk *= (13.3) / (13.3 + 10.6);
        }
        else{
            risk *= (10.6) / (13.3 + 10.6);
        }

        // QT > 410 ms, 10.8, 9.3
        if (samples2ms(features.get("ECG_Q_X")-features.get("ECG_T_X")) > 410.0) {
            risk *= (10.8) / (10.8 + 9.3);
        }
        else{
            risk *= (9.3) / (10.8 + 9.3);
        }

        /*
        // ST depression, 11.6, 12.7
        if (samples2ms(features.get("ECG_Q_X")-features.get("ECG_T_X")) > 410.0) {
            risk *= (11.6) / (11.6 + 12.7);
        }
        else{
            risk *= (12.7) / (11.6 + 12.7);
        }
        */

        // ST elevation, 18.9, 10.7
        if ((features.get("ECG_S_Y") > features.get("ECG_BASE"))) {
            risk *= (18.9) / (18.9 + 10.7);
        }
        else{
            risk *= (10.7) / (18.9 + 10.7);
        }

        // inverted T-wave, 8.8, 3.8
        if ((features.get("ECG_T_Y")<features.get("ECG_BASE"))) {
            risk *= (8.8) / (8.8 + 3.8);
        }
        else{
            risk *= (3.8) / (8.8 + 3.8);
        }


        double risk_max = ((12.9) / (12.9 + 11.9)) *
                ((12.9) / (12.9 + 10.0)) *
                ((13.3) / (13.3 + 10.6)) *
                ((10.8) / (10.8 + 9.3)) *
                ((18.9) / (18.9 + 10.7)) *
                ((8.8) / (8.8 + 3.8));

        double risk_min = ((11.9) / (12.9 + 11.9)) *
                ((10.0) / (12.9 + 10.0)) *
                ((10.6) / (13.3 + 10.6)) *
                ((9.3) / (10.8 + 9.3)) *
                ((10.7) / (18.9 + 10.7)) *
                ((3.8) / (8.8 + 3.8));

        risk = (risk - risk_min) / (risk_max - risk_min);

        return risk;
    }

    // TODO: merge with the server code
    public static boolean checkECGFeatures(HashMap<String, Double> features) {

        if (features.get("ECG_HR") > 200 || features.get("ECG_HR") < 40)
            return false;

        if (features.get("ECG_P_X") < 0.0 ||
                features.get("ECG_Q_X") < 0.0 ||
                features.get("ECG_R_X") < 0.0 ||
                features.get("ECG_S_X") < 0.0 ||
                features.get("ECG_T_X") < 0.0)
            return false;


        return true;
    }

    public static double samples2ms(double dx){
        return 1000.0 * dx * (1/500.0);
    }

    private static final int HRV_BUFFER_SIZE = 50;
    static CircularFifoQueue<Integer> buf = new CircularFifoQueue<Integer>(HRV_BUFFER_SIZE);
    public static void storeRRs(int[] rrs){
    	for (int i = 0; i < rrs.length; i++){
    		buf.add(rrs[i]);
    	}
    }
    private static int stressIncreaseCount = 0;
    private static double prevHRV = 0.0;
    
    public static double calcStress(double hrv){
    	if (hrv < prevHRV * 0.95){
    		stressIncreaseCount += 1;
    	}else{
    		stressIncreaseCount -= 1;
    	}
    	
    	if (stressIncreaseCount <= 0){
    		stressIncreaseCount = 1;
    	}
    	
    	prevHRV = hrv;
    	return (stressIncreaseCount % 10) / 10.0;
    	
    }
    
    public static double calcHRV(int[] rs){
        double rmssd = 0.0;
        int[] rrs = SignalProcessing.diff(rs);
        storeRRs(rrs);
        
        
        for (int i = buf.size() - 1; i > 0; i--){
            rmssd += (buf.get(i) - buf.get(i - 1)) * (buf.get(i) - buf.get(i - 1));
        }

        // RMSSD = sqrt ( \sum_{i = 1} ^ N (I[n] - I[n - 1]) ^ 2 / (N - 1))
        
        if (buf.size() > 2){
        	rmssd = Math.sqrt(rmssd / (buf.size() - 1));
        }
        return rmssd;

    }
    public static double calcHR(int[] rs){
        double hr = 0.0;
        int[] rrs = SignalProcessing.diff(rs);

        for (int i=0; i<rrs.length; i++)
            hr += rrs[i];

        hr /= rrs.length;

        return 60000.0 / samples2ms(hr);
    }
}