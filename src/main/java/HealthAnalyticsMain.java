/*
 * Contributors:
 * Sandeep Singh Sandha, Mohammad Kachuee and Sajad Darabi
 * Project: CS249
 * University of California, Los Angeles
 * Date: 21 June, 2017
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.event.globals.Globals;

import beat.analyzer.Analysis;
import beat.analyzer.ProcessWindow;
import beat.analyzer.SignalProcessing;
import kafka.serializer.StringDecoder;
import scala.Tuple2;

public class HealthAnalyticsMain {

	public static void main(String[] args) {
		
		 Logger.getLogger("org").setLevel(Level.OFF);
		 Logger.getLogger("akka").setLevel(Level.OFF);
		
		 SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("Kafka-sandbox");
		 JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
		 
		 //Todo Process pipleine
		 
		 Map<String, String> kafkaParams = new HashMap<>();
		 kafkaParams.put("bootstrap.servers", "localhost:9092");
		 
		 Set<String> topics = Collections.singleton("test");//list of topics
		 
		 JavaPairInputDStream <String, String> directKafkaStream =KafkaUtils.createDirectStream(jssc, String.class, String.class, StringDecoder.class, StringDecoder.class, kafkaParams, topics);
		 
		 directKafkaStream.foreachRDD(rdd->{
			 
			 
			 System.out.println("----New RDD with "+rdd.partitions().size()+" Paritions and "+rdd.count()+" records");
			 
			 JavaRDD<int []> rPeaksRDD = rdd.map(new Function<Tuple2<String,String>, int[]>() {
			

				@Override
				public int [] call(Tuple2<String, String> arg0) throws Exception {
					// TODO Auto-generated method stub
					 int[] rPeaks=null;
					 double [] ECG_values = new double[3000];
					 
					try{
						
						 Globals.size=0;
						 
					  
					
					    JSONParser parser = new JSONParser();
					    JSONArray jsonarray=(JSONArray)parser.parse(arg0._2);
					    Iterator i = jsonarray.iterator();
                        int siz=0;
				        while (i.hasNext()) { //arg0._2 is a json array of size 2500
				            
							 JSONObject obj= (JSONObject) i.next();
							 //Globals.ECG_values[Globals.size] = (double) obj2.get("DataValueDouble");
							 ECG_values[siz]=(double) obj.get("DataValueDouble");
						     siz=(siz+1)%3000;//cannot go more than the allocated array size 
				        }//end while
					    
				       
				        rPeaks = SignalProcessing.findRPeaks(ECG_values);
						
				        
					 
					}//end try
					catch(Exception e)
					{
						e.printStackTrace();
					}
					 
					
					return rPeaks;
				}
				  
			 });
			 double [] ECG_values = new double[2500];
			 
			 JavaRDD<int []> pqrstPointsRDD = rdd.map(new Function<Tuple2<String,String>, int[]>() {
					

				@Override
				public int [] call(Tuple2<String, String> arg0) throws Exception {
					// TODO Auto-generated method stub
					 int[] pqrstPoints=null;
					
					 
					try{
						
						 Globals.size=0;
						 
					   // System.out.println("pqrstPointsRDD in Map:"+arg0._2.length());
					
					    JSONParser parser = new JSONParser();
					    JSONArray jsonarray=(JSONArray)parser.parse(arg0._2);
					    Iterator i = jsonarray.iterator();
                        int siz=0;
				        while (i.hasNext()) { //arg0._2 is a json array of size 2500
				            
							 JSONObject obj= (JSONObject) i.next();
							 //Globals.ECG_values[Globals.size] = (double) obj2.get("DataValueDouble");
							 ECG_values[siz]=(double) obj.get("DataValueDouble");
						     siz=(siz+1)%2500;//cannot go more than the allocated array size 
				        }//end while
					    
				       
				        pqrstPoints = SignalProcessing.findECGPoints(ECG_values);
						
				        
					 
					}//end try
					catch(Exception e)
					{
						e.printStackTrace();
					}
					 
					
					return pqrstPoints;
				}
				  
			 });
			 
			
			 
			 
			 JavaRDD<Double> windowHRRDD = rPeaksRDD.map(new Function<int[], Double>() {

				@Override
				public Double call(int[] rPeaks) throws Exception {
					// TODO Auto-generated method stub
					
					double windowHRR=0;
					
					try{
					
						windowHRR=Analysis.calcHR(rPeaks);
						
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					
					return windowHRR;
				}
			   });
			 
			 
			 
			 JavaRDD<Double> windowHRVRDD = rPeaksRDD.map(new Function<int[], Double>() {

					@Override
					public Double call(int[] rPeaks) throws Exception {
						// TODO Auto-generated method stub
						
						double windowHRRV=0;
						
						try{
						
							windowHRRV=Analysis.calcHRV(rPeaks);
							
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						
						return windowHRRV;
					}
				   });
				 
				
				 
				 JavaRDD<Double> stressIdxRDD = windowHRVRDD.map(new Function<Double, Double>() {

						@Override
						public Double call(Double windowHRV) throws Exception {
							// TODO Auto-generated method stub
							
							double stressIdx=0;
							
							try{
							
								stressIdx=Analysis.calcStress(windowHRV);
								
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
							
							return stressIdx;
						}
					   });
				
				/*
				 windowHRRDD.foreach(record->
				 {
					  //record._2 is the json string
					
					 System.out.println(" HR:"+record);
					
				 });//end foreach
				 
				 windowHRVRDD.foreach(record->
				 {
					  //record._2 is the json string
					
					 System.out.println(" HRV :"+record);
					
				 });//end foreach
			*/
				 
				 stressIdxRDD.foreach(record->
					 {
						  //record._2 is the json string
						
						 System.out.println("Stress Index is:"+record);
						
					 });//end foreach
					 
			 
				 
				 /* Predicting the Heart Risk */
				 HashMap<String, Double> windowFeatures = new HashMap<>();
				 try{
					 
					 if(rPeaksRDD.count()>0)
					 {
						 int [] rPeaks=rPeaksRDD.first();
						 windowFeatures.put("ECG_HR", Analysis.calcHR(rPeaks));// FIXME: add real hear-rate value
						 windowFeatures.put("ECG_BASE", SignalProcessing.findBase(ECG_values));// FIXME: add real hear-rate value
				         windowFeatures.put("ECG_P_X", (double) (pqrstPointsRDD.first()[0]));
				         windowFeatures.put("ECG_P_Y", ECG_values[pqrstPointsRDD.first()[0]]);
				         windowFeatures.put("ECG_Q_X", (double) (pqrstPointsRDD.first()[1]));
				         windowFeatures.put("ECG_Q_Y", ECG_values[pqrstPointsRDD.first()[1]]);
				         windowFeatures.put("ECG_R_X", (double) (pqrstPointsRDD.first()[2]));
				         windowFeatures.put("ECG_R_Y", ECG_values[pqrstPointsRDD.first()[2]]);
				         windowFeatures.put("ECG_S_X", (double) (pqrstPointsRDD.first()[3]));
				         windowFeatures.put("ECG_S_Y", ECG_values[pqrstPointsRDD.first()[3]]);
				         windowFeatures.put("ECG_T_X", (double) (pqrstPointsRDD.first()[4]));
				         windowFeatures.put("ECG_T_Y", ECG_values[pqrstPointsRDD.first()[4]]);
				         // predict risk
			                double windowRisk = Analysis.predictRisk(windowFeatures);
			                
			                System.out.println("Heart Risk is:"+windowRisk);
					 }//end if
	                
				 }//end try
				 catch(Exception e)
				 {
					 e.printStackTrace();
				 }
	                
				 /*End Predicting the Heart Risk*/
				 
			
					
			 
		 });//end directKafkaStream
		 
		 //end Todo Process pipeline
		 
		 
		
		 
		 
		 
		 jssc.start();
		 
		 try {
			jssc.awaitTermination();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
	}
	
}
