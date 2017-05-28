package beat.analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mohammad on 5/19/17.
 */
public class SignalProcessing {

    public static List<double[]> makeWindow(double[] data, int w){
        List<double[]> windows = new ArrayList<>();

        for (int i = 0; i < (int)(data.length / w); i ++){
            double[] a = Arrays.copyOfRange(data, i * w, (i + 1) * w);
            windows.add(a);
        }

        return windows;

    }

    public static int[] findRPeaks(double[] data){
        final double THRESHOLD_AMP = 0.8;
        ArrayList<Integer> rPeaks = new ArrayList<>();

        double[] sigNorm = normalizeWindow(data);
        double[] sigFilt = IIRFilter(sigNorm);
        sigNorm = normalizeWindow(sigFilt);
        double[] sigDiff1 = diff(sigNorm);


        // find all local extremums
        ArrayList<Integer> zeroCrossings = new ArrayList<>();
        for (int i = 1; i < sigDiff1.length; i++){
            if ((sigDiff1[i - 1] < 0 && sigDiff1[i] > 0)
                || (sigDiff1[i - 1] > 0 && sigDiff1[i] < 0)){
                zeroCrossings.add(i);
            }
        }
        // filter out ones with low amps
        for (int i=0; i<zeroCrossings.size(); i++){
            if (sigNorm[zeroCrossings.get(i)] > THRESHOLD_AMP)
                rPeaks.add(zeroCrossings.get(i));
        }

        // copy preprocessed window to for cascading detectors
        //data = sigNorm;

        // DEBUG DUMP FILE GENERATION
        DataParser.dumpToFile(Arrays.toString(sigNorm),
                "../run_output/window.csv");
        DataParser.dumpToFile(Arrays.toString(listIntToArray(rPeaks)),
                "../run_output/Rpeaks.csv");
        //DataParser.dumpToFile(Arrays.toString(sigFilt), "../run_output/filt.csv");
        return listIntToArray(rPeaks);
    }

    public static double[] findBPValues(double[] data){
        double[] sdVals = {Double.MIN_VALUE, Double.MAX_VALUE};
        // find max values
        for (int i=0; i<data.length; i++){
            if (data[i] < sdVals[1])
                sdVals[1] = data[i];
            if (data[i] > sdVals[0])
                sdVals[0] = data[i];
        }
        return sdVals;
    }

    public static int[] findECGPoints(double[] data){
        final double THRESHOLD_AMP = 0.8;
        final double THRESHOLD_AMP_T = 0.1;
        ArrayList<Integer> rPeaks = new ArrayList<>();
        int[] pqrstPoints = new int[5];

        double[] sigNorm = normalizeWindow(data);
        double[] sigFilt = IIRFilter(sigNorm);
        sigNorm = normalizeWindow(sigFilt);
        double[] sigDiff1 = diff(sigNorm);


        // find all local extremums
        ArrayList<Integer> zeroCrossings = new ArrayList<>();
        for (int i = 1; i < sigDiff1.length; i++){
            if ((sigDiff1[i - 1] < 0 && sigDiff1[i] > 0)
                    || (sigDiff1[i - 1] > 0 && sigDiff1[i] < 0)){
                zeroCrossings.add(i);
            }
        }
        // filter out ones with low amps
        for (int i=0; i<zeroCrossings.size(); i++){
            if (sigNorm[zeroCrossings.get(i)] > THRESHOLD_AMP)
                rPeaks.add(zeroCrossings.get(i));
        }

        // select an R-peak
        pqrstPoints[2] = rPeaks.get(rPeaks.size()/2);

        // find q point
        for (int i=0; i<zeroCrossings.size(); i++){
            // find the maximum index before the R-peak
            if (zeroCrossings.get(i) < pqrstPoints[2])
                pqrstPoints[1] = zeroCrossings.get(i);
            else
                break;
        }

        // find s point
        for (int i=zeroCrossings.size()-1; 0<i; i--){
            // find the maximum index before the R-peak
            if (zeroCrossings.get(i) > pqrstPoints[2])
                pqrstPoints[3] = zeroCrossings.get(i);
            else
                break;
        }

        // find the t point
        // it is not as easy as before!, based on the signal type
        // we need to find t as either:
        // 1) significant local maxima,
        // 2) flat line no strong exterma,
        // 3) significant local minima

        // select the segment which we expect the p
        // check if any r-peak detected
        if (rPeaks.size() < 3)
            return pqrstPoints;

        int seg_start = pqrstPoints[3];
        int seg_end = (rPeaks.get(rPeaks.size()/2) +
                rPeaks.get(rPeaks.size()/2+1)) / 2;

        // scan for significant extremas
        int ind_t_max = seg_start;
        int ind_t_min = rPeaks.get(rPeaks.size()/2); // maximum possible value
        for (int i=0; i<zeroCrossings.size(); i++){
            if ((seg_start < zeroCrossings.get(i)) && (seg_end > zeroCrossings.get(i))){
                if (sigNorm[zeroCrossings.get(i)] > sigNorm[ind_t_max])
                    ind_t_max = zeroCrossings.get(i);
                if (sigNorm[zeroCrossings.get(i)] > sigNorm[ind_t_min])
                    ind_t_min = zeroCrossings.get(i);
            }
        }

        // make decision
        if (sigNorm[ind_t_max] > THRESHOLD_AMP_T)
            pqrstPoints[4] = ind_t_max;
        else if (ind_t_min == rPeaks.get(rPeaks.size()/2))
            pqrstPoints[4] = ind_t_max;
        else if (sigNorm[ind_t_min] < -THRESHOLD_AMP_T)
            pqrstPoints[4] = ind_t_min;
        else
            pqrstPoints[4] = ind_t_max;

        // find the p point
        // it is not as easy as before!, based on the signal type
        // we need to find the p as the maximum peak before q

        // select the segment which we expect the p
        seg_start = (rPeaks.get(rPeaks.size()/2) +
                rPeaks.get(rPeaks.size()/2-1)) / 2;
        seg_end = pqrstPoints[1];

        // scan for significant extremas
        int ind_p_max = seg_end;
        for (int i=0; i<zeroCrossings.size(); i++){
            if ((seg_start < zeroCrossings.get(i)) && (seg_end > zeroCrossings.get(i))){
                if (sigNorm[zeroCrossings.get(i)] > sigNorm[ind_p_max])
                    ind_p_max = zeroCrossings.get(i);
            }
        }

        // make decision
        pqrstPoints[0] = ind_p_max;


        // copy preprocessed window to for cascading detectors
        for (int i=0; i<sigNorm.length; i++)
            data[i] = sigNorm[i];

        pqrstPoints[0] -= 1;
        pqrstPoints[1] -= 1;
        pqrstPoints[2] -= 1;
        pqrstPoints[3] -= 1;
        pqrstPoints[4] -= 1;
        // DEBUG DUMP FILE GENERATION
        DataParser.dumpToFile(Arrays.toString(sigNorm),
                "../run_output/window.csv");
        DataParser.dumpToFile(Arrays.toString(listIntToArray(zeroCrossings)),
                "../run_output/extremas.csv");
        DataParser.dumpToFile(Arrays.toString(pqrstPoints),
                "../run_output/pqrst.csv");
        //DataParser.dumpToFile(Arrays.toString(sigFilt), "../run_output/filt.csv");
        return pqrstPoints;
    }

    public static double findBase(double[] window){
        double base = 0.0;
        // calc median of y values and return it as the base level
        Arrays.sort(window);
        if (window.length %2 == 0)
            base = window[window.length/2];
        else
            base = (window[window.length/2] + window[window.length/2+1]) / 2;

        return base;
    }

    /* */
    public static double[] normalizeWindow(double[] window){
        double maxVal = Float.MIN_VALUE;
        double minVal = Float.MAX_VALUE;
        for (int i = 0; i < window.length; i++){
            if (window[i] > maxVal){
                maxVal = window[i];
            }
            if (window[i] < minVal) {
                minVal = window[i];
            }
        }
        
        for (int i = 0; i < window.length; i++){
            window[i] = (window[i] - minVal) / (maxVal - minVal);
        }
        return window;
    }


    public static double[] diff(double[] sig){
        double[] diffSig = new double[sig.length - 1];
        for (int i = 1; i < sig.length-1; i++){
            diffSig[i] = sig[i] - sig[i - 1];
        }
        return diffSig;
    }

    public static int[] diff(int[] sig){
        int[] diffSig = new int[sig.length - 1];
        for (int i = 1; i < sig.length-1; i++){
            diffSig[i] = sig[i] - sig[i - 1];
        }
        return diffSig;
    }

    public static double[] IIRFilter(double[] data){

        // y[n] = + 2 y[n - 2] - y[n - 4] + x[n] -3.144678457118267 x[n - 1 + 3.708154150310699 x[n - 2] -
        // 1.979871143227762 x[n - 3] + 0.416406129287192 x[n - 4]
        double[] y = new double[data.length];

        for (int i = 0; i < data.length; i++){
            if (i < 4){
                ;//y[i] = data[i];
            }else{
                y[i] = data[i] - 2* data[i - 2] + data[i - 4] +3.144678457118267 * y[i - 1] - 3.708154150310699 * y[i - 2] +
                1.979871143227762 * y[i - 3] - 0.416406129287192 * y[i - 4];
            }
        }
        for (int i = 0; i < data.length; i++){
            if (i < 4) {
                y[i] = data[i];
            }
        }
        return y;
    }


    public static double[] listDoubleToArray(List<Double> a){
        double[] ar = new double[a.size()];
        for (int i = 0 ; i < a.size(); i++){
            ar[i] = a.get(i);
        }
        return ar;
    }

    public static int[] listIntToArray(List<Integer> a){
        int[] ar = new int[a.size()];
        for (int i = 0 ; i < a.size(); i++){
            ar[i] = a.get(i);
        }
        return ar;
    }
}
