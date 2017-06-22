# Complex Event Processing of Health Data in Real-time using Kafka and Spark
## UCLA CS 249: Current Topics in Data-structures 

Contributors:
- [Sandeep Singh Sandha](https://sites.google.com/view/sandeep-/home)
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
The system is designed using Kafka and Spark. Spark streaming is used to receive the data from Kafka topics. [Spark streaming](https://spark.apache.org/streaming/) is also used to run the heart failure risk and stress index prediction over the windows of data streams.

## Apache Kafka
Apache Kafka is a streaming platform which enables user to publish data and also subscribe to different streams of records. Kafka stores the streams in a fault tolerant way. Kafka is used to build reliable real-time streaming data pipelines. Kafka runs as a cluster and have concept of topics or feed name to which records are published. In our current system abstraction, every user is publishing it data to a particular topic designated for the stream. Each topic internally maintains all the records for the configurable retention period. This capability can be used for the health conditions which requires to use the past data. 

## Apache Spark
Apache spark is a big data processing framework which runs on cluster. It provides the promise of better reliability and fault tolerance along with in memory process features. The different restricted transformations and queries are supported on the datasets which are inherently parallel and fault tolerable. Spark supports streaming library to build and scale streaming applications. Spark streaming allows data injection from multiple sources including Kafka. Spark streaming provides the abstraction of DStreams which are like batches of data created from the real-time streaming source. The DStream is represented as a sequence of resilient distributed dataset also called RDDs on which the basic spark operations can be performed easily.


# System Architecture
![System Architecture](https://github.com/sandeep-iitr/ComplexEventDetection_CS249/blob/master/figs/system.jpg)

The system architecture is shown in the figure. Different users acts as data producers and consumers in the Kafka model. Users publishes the health data. In this system the assumption is that users are aware of the different topics available for different types of sensory data. For example ECG data might be published to a topic dedicated to it, and BP data to a different topic and so-on. Kafka takes care of the scalability of the system as the number of users may grow or shrink and also serve as the data retention, storage and forwarding interface.

Different types of complex events defining the health conditions are running on the Spark cluster. For example in figure we have shown two such events of heart failure risk and stress index detection. These events are running as separate jobs using Spark streaming on the Spark cluster. Thus this system is extensible where multiple such jobs can be created or aborted depending on the desired analytics to be performed. Since, these jobs are running on the spark cluster they are scalable and has fault tolerance. The complex events considered here are explained more later in the use cases section.
	Each of the health conditions jobs are internally subscribed to the relevant health data which is done by means of the Spark streaming. DStream are created from the multiple Kafka topics and are processed in each of these. The real-time generated output can be saved to the database and can also be communicated back to the user via Kafka. For sending results back to users, the jobs publishes back to the Kafka. Different users are also subscribed to the Kafka to listen to the relevant topics for the analytics results.

# Data Pipeline
![Data Processing Pipeline for Heart Risk Prediction](https://github.com/sandeep-iitr/ComplexEventDetection_CS249/blob/master/figs/HR_DP.jpg)

The simplified data pipeline for the heart risk failure is shown in figure \ref{fig:HR_DP}. The algorithms used in the prediction for each use case are discussed in more detail in the next section. The user publishes the ECG and BP data to the Kafka. Heart Risk predictor is running as spark job, and uses spark streaming to continuously listen the Kafka topic for the user data. It is subscribed to two topics, one for ECG and one for BP. The Heart Risk Predictor publishes its output to the Kafka topic to which the user is listening.

# Use Cases Operations in Spark RDDs
In this section we will discuss how to define the use cases in the spark map reduce framework and the other transformations and operations supported by it. The goals behind these definitions is that, these operations are highly parallel and fault tolerable. Also in future these operations may be individually moved to different places for the reason of faster processing, reliability and security. 

Kafka producer is sending the data in the Json array format using the windows of sizes required for the heart risk measurement and stress index measurement. We created severals RDDs from the data received using the spark transformations. Logically each RDD is a different stages in the signal processing and are equivalent to the peaks in the ECG and BP signals, along with different operations on it.

# Experiments and Results
The prototype system was implemented using Java. The code of the implementation is available on the git. We simulated the multiple users by sending real-time data via different Java applications to the Kafka cluster. The implementation of both use cases is using Spark streaming in Java, and each is being run as a separate job on the local Spark cluster. The jobs do the real-time health analytics and produce the heart risk and stress index as output.

# Future Work
In the prototype implementation we have implemented the two health use cases. These implementations prove that a general platform is capable of processing other applications of health inference and behavioral patterns. For instance, real-time prediction of smoking can be easily done using the proposed system architecture. We would also like to test this system on real scenarios, as in our current prototype implementation we simulated the user data using real datasets by running separate Java client applications.

More work can also be done to define the complex events in generalized way for the health applications, so that the different signal processing capabilities can be reused. A simple and expressive grammar can be proposed for the same which achieves the job of events semantic definitions, the required inputs, outputs and logic. However such a goal requires effort to understand the nature of different health analytics to develop a generalization.

# Conclusion
In this project we worked on developing a framework to do health analytics in real-time.
By developing the framework using Kafka and Spark, we can handle large amounts of health data reliably and efficiently in a fault tolerable fashion. Individual components of system are designed to be highly scalable. Further, the system can be easily extended to support new use cases via running each as a separate spark job. We implemented a prototype system for running two use cases in real-time. Our prototype shows the capabilities of the proposed design. The design can be improved by adding the  data security and access control features so as to deploy with real users or to conduct studies.
