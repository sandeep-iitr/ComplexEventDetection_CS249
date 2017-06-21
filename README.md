# Complex Event Processing of Health Data in Real-time using Kafka and Spark
## UCLA CS 249: Current Topics in Data-structures 

Contributors:
- Sandeep Singh Sandha
- Mohammad Kachuee
- Sajad Darabi

In this project our goal was to develop a scalable system which can do real-time analytics of different health conditions. Different health conditions can be regarded as the complex events and thus this concept can be extended to other use cases easily.
Large number of users should be able to send the health data in real-time and along with receiving back the feedback and results. 
Keeping the requirements in mind we used Kafka and Spark to develop our system. Multiple users are like Kafka producers sending data in real-time. Spark streaming is used to process data of different window sizes to analyze the health conditions. In our system we have developed and tested the heart attack risk prediction and stress prediction as our sample complex events. We have simulated and tested our system with multiple health datasets.

Dependencies:
[Apache Kafka](https://kafka.apache.org/), [Apache Spark](https://spark.apache.org/), Java, [Apache Maven](https://maven.apache.org/), [Zookeeper](https://zookeeper.apache.org/).

How to run:
1. Start the zookeeper
2. Start the Apache Kafka
3. Run the Producer application
4. Run the HealthAnalytics application




# System Design
The system is designed using Kafka and Spark. Spark streaming is used to receive the data from Kafka topics. Spark streaming\footnote{https://spark.apache.org/streaming/} is also used to run the heart failure risk and stress index prediction over the windows of data streams.

## Apache Kafka
Apache Kafka is a streaming platform which enables user to publish data and also subscribe to different streams of records. Kafka stores the streams in a fault tolerant way. Kafka is used to build reliable real-time streaming data pipelines. Kafka runs as a cluster and have concept of topics or feed name to which records are published. In our current system abstraction, every user is publishing it data to a particular topic designated for the stream. Each topic internally maintains all the records for the configurable retention period. This capability can be used for the health conditions which requires to use the past data. 

## Apache Spark
Apache spark is a big data processing framework which runs on cluster. It provides the promise of better reliability and fault tolerance along with in memory process features. The different restricted transformations and queries are supported on the datasets which are inherently parallel and fault tolerable. Spark supports streaming library to build and scale streaming applications. Spark streaming allows data injection from multiple sources including Kafka. Spark streaming provides the abstraction of DStreams which are like batches of data created from the real-time streaming source. The DStream is represented as a sequence of resilient distributed dataset also called RDDs on which the basic spark operations can be performed easily.


# System Architecture
The system architecture is shown in the figure \ref{fig:SystemMain}. Different users acts as data producers and consumers in the Kafka model. Users publishes the health data. In this system the assumption is that users are aware of the different topics available for different types of sensory data. For example ECG data might be published to a topic dedicated to it, and BP data to a different topic and so-on. Kafka takes care of the scalability of the system as the number of users may grow or shrink and also serve as the data retention, storage and forwarding interface.

Different types of complex events defining the health conditions are running on the Spark cluster. For example in figure \ref{fig:SystemMain} we have shown two such events of heart failure risk and stress index detection. These events are running as separate jobs using Spark streaming on the Spark cluster. Thus this system is extensible where multiple such jobs can be created or aborted depending on the desired analytics to be performed. Since, these jobs are running on the spark cluster they are scalable and has fault tolerance. The complex events considered here are explained more later in the use cases section.
	Each of the health conditions jobs are internally subscribed to the relevant health data which is done by means of the Spark streaming. DStream are created from the multiple Kafka topics and are processed in each of these. The real-time generated output can be saved to the database and can also be communicated back to the user via Kafka. For sending results back to users, the jobs publishes back to the Kafka. Different users are also subscribed to the Kafka to listen to the relevant topics for the analytics results.
