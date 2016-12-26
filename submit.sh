APP=./target/scala-2.10/kmeans-project_2.10-1.0.jar
CLASS=SparkKmeans
#SPARK_HOME=/usr/share/spark
spark-submit --verbose --class $CLASS --master yarn-cluster --num-executors 5 --driver-memory 4g --executor-memory 16g --executor-cores 4 $APP mytest kddcup.data_10_percent 
