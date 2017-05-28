
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;

import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;

import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;

import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import scala.Tuple2;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;


public final class SparkStreamingKafka {

	static Map<String, Object> kafkaParams = new HashMap<>();
	
	static final Pattern SPACE = Pattern.compile(" ");

	public static void main(String[] args) {
		 
		
		
		kafkaParams.put("bootstrap.servers", "localhost:9092");
		kafkaParams.put("key.deserializer",  StringSerializer.class);
		kafkaParams.put("value.deserializer",StringSerializer.class);
		kafkaParams.put("group.id", "GroupId1");
		kafkaParams.put("auto.offset.reset", "latest");
		kafkaParams.put("enable.auto.commit", false);

		/*
		Collection<String> topics = Arrays.asList("test");//list of topics
		*/
		
		 //Logger.getLogger("org").setLevel(Level.OFF);
		 //Logger.getLogger("akka").setLevel(Level.OFF);
		 
		// Create a local StreamingContext with two working thread and batch interval of 1 second
		 //SparkConf conf = new SparkConf().setAppName("Simple Application");
		 // SparkConf conf = new SparkConf().setMaster("spark://sandeep-Z68M-D2H:7077").setAppName("NetworkWordCount");
		 SparkConf conf = new SparkConf().setMaster("local[4]").setAppName("NetworkWordCount");
		 JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(2));
		// Create a DStream that will connect to hostname:port, like localhost:9999
		
	/*
		 final JavaInputDStream<ConsumerRecord<String, String>> stream =
				  KafkaUtils.createDirectStream(
						  jssc,
				    LocationStrategies.PreferConsistent(),
				    ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
				  );
		 */
		 
		 Map<String, Integer> topicMap = new HashMap<>();
		 topicMap.put("my-replicated-topic", 1);//topic
		 
		 //bin/run-example org.apache.spark.examples.streaming.JavaKafkaWordCount zoo01,zoo02, \
		 //*    zoo03 my-consumer-group topic1,topic2 1`
		 
		 JavaPairReceiverInputDStream<String, String> messages =
				 KafkaUtils.createStream(jssc,"localhost:2181","test",topicMap);
	
		 
		 JavaDStream<String> lines = messages.map(new Function<Tuple2<String, String>, String>() {
		      @Override
		      public String call(Tuple2<String, String> tuple2) {
		        return tuple2._2();
		      }
		});

		 
		 //lines.print();
		 
		 JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
		      @Override
		      public Iterator<String> call(String x) {
		        return Arrays.asList(SPACE.split(x)).iterator();
		      }
		});
		 
		 /*
		 
		// Count each word in each batch
				 JavaPairDStream<String, String> pairs2= stream.mapToPair(
				  new PairFunction<ConsumerRecord<String, String>, String, String>() {
				    @Override
				    public Tuple2<String, String> call(ConsumerRecord<String, String> record) {
				      return new Tuple2<>(record.key(), record.value());
				    }
				  });
		 
		 pairs2.print();
		*/
				 
		 JavaPairDStream<String, Integer> wordCounts = words.mapToPair(
			      new PairFunction<String, String, Integer>() {
			        @Override
			        public Tuple2<String, Integer> call(String s) {
			          return new Tuple2<>(s, 1);
			        }
			      }).reduceByKey(new Function2<Integer, Integer, Integer>() {
			        @Override
			        public Integer call(Integer i1, Integer i2) {
			          return i1 + i2;
			        }
			      });

			wordCounts.print();
			
		 jssc.start();              // Start the computation
		 try {
			jssc.awaitTermination();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   // Wait for the computation to terminate
		 
		 
	 }//end main
	
	
}//end class
