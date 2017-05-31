import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import kafka.serializer.StringDecoder;

public class SparkKafka2 {

	public static void main(String[] args) {
		
		 Logger.getLogger("org").setLevel(Level.OFF);
		 Logger.getLogger("akka").setLevel(Level.OFF);
		
		 SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("Kafka-sandbox");
		 JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(2));
		 
		 //Todo Process pipleine
		 
		 
		 Map<String, String> kafkaParams = new HashMap<>();
		 kafkaParams.put("bootstrap.servers", "localhost:9092");
		 
		 Set<String> topics = Collections.singleton("test");//list of topics
		 
		 JavaPairInputDStream <String, String> directKafkaStream =KafkaUtils.createDirectStream(jssc, String.class, String.class, StringDecoder.class, StringDecoder.class, kafkaParams, topics);
		 
		 directKafkaStream.foreachRDD(rdd->{
			 System.out.println("----New RDD with "+rdd.partitions().size()+" Paritions and "+rdd.count()+" records");
			 rdd.foreach(record->System.out.println(record._2));
		 });
		 
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
