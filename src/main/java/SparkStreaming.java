import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import scala.Tuple2;

public class SparkStreaming {

	
	 
	 
	 public static void main(String[] args) {
		 
		 Logger.getLogger("org").setLevel(Level.OFF);
		 Logger.getLogger("akka").setLevel(Level.OFF);
		 
		// Create a local StreamingContext with two working thread and batch interval of 1 second
		 SparkConf conf = new SparkConf().setMaster("spark://sandeep-Z68M-D2H:7077").setAppName("NetworkWordCount");
		 JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
		// Create a DStream that will connect to hostname:port, like localhost:9999
		 JavaReceiverInputDStream<String> lines = jssc.socketTextStream("localhost", 9999);
		 
		// Split each line into words
		 JavaDStream<String> words = lines.flatMap(
		   new FlatMapFunction<String, String>() {
		     @Override public Iterator<String> call(String x) {
		       return Arrays.asList(x.split(" ")).iterator();
		     }
		   });
		 
		 
		// Count each word in each batch
		 JavaPairDStream<String, Integer> pairs = words.mapToPair(
		   new PairFunction<String, String, Integer>() {
		     @Override public Tuple2<String, Integer> call(String s) {
		       return new Tuple2<>(s, 1);
		     }
		   });
		 JavaPairDStream<String, Integer> wordCounts = pairs.reduceByKey(
		   new Function2<Integer, Integer, Integer>() {
		     @Override public Integer call(Integer i1, Integer i2) {
		       return i1 + i2;
		     }
		   });

		 // Print the first ten elements of each RDD generated in this DStream to the console
		 wordCounts.print();
		 
		 jssc.start();              // Start the computation
		 try {
			jssc.awaitTermination();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   // Wait for the computation to terminate
		 
		 
	 }//end main
	
}
