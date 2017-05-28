package beat.analyzer;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mohammad on 5/19/17.
 */
public class DataParser {

    public static List<Double> ReadCSV(String path){
        File file = new File(path);
        List<Double> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int line_cnt = 0;
            while ((line = br.readLine()) != null) {
                // skip first 2 lines
                line_cnt ++;
                if(line_cnt<3)
                    continue;
                // read value and append
                String val = line.split(",")[1];
                data.add(Double.parseDouble(val));
            }
        } catch (Exception e){
            System.out.println("Something is wrong!");
            //TODO: do something when file not found
        }
        return data;
    }

    public static List<Double> ReadCSV_ABP(String path){
        File file = new File(path);
        List<Double> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int line_cnt = 0;
            int line_num = 2;
            while ((line = br.readLine()) != null) {
                // skip first 2 lines
                // read value and append
                line_cnt++;
                if (line_cnt == line_num){
                    String[] val = line.split(",");
                    for (String v: val){
                        data.add(Double.parseDouble(v));
                        data.add(Double.parseDouble(v));
                        data.add(Double.parseDouble(v));
                        data.add(Double.parseDouble(v));
                    }
                }
            }
        } catch (Exception e){
            System.out.println("Something is wrong!");
            //TODO: do something when file not found
        }
        return data;
    }

    public static void dumpToFile(String dump, String path){
        try{
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            writer.println(dump.replaceAll("]","").replaceAll("\\[",""));
            writer.close();
        } catch (IOException e) {
            // do something
        }

    }




}
