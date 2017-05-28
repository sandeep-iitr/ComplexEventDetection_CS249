import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

public class SimpleApp {

	//String sparkpath="/home/sandeep/metroinsight/installations/spark/spark-2.1.0-bin-hadoop2.7/bin/spark-shell";
	//String masterurl="--master spark://sandeep-Z68M-D2H:7077";
	 public static void main(String[] args) {
		    String logFile = "/home/sandeep/metroinsight/installations/spark/spark-2.1.0-bin-hadoop2.7/README.md"; // Should be some file on your system
		    SparkConf conf = new SparkConf().setAppName("Simple Application");
		    JavaSparkContext sc = new JavaSparkContext(conf);
		    JavaRDD<String> logData = sc.textFile(logFile).cache();

		    long numAs = logData.filter(new Function<String, Boolean>() {
		      public Boolean call(String s) { return s.contains("a"); }
		    }).count();

		    long numBs = logData.filter(new Function<String, Boolean>() {
		      public Boolean call(String s) { return s.contains("b"); }
		    }).count();

		    System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);
		    
		    sc.stop();
		  }
	 
}
