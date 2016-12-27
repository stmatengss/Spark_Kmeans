APP=./target/scala-2.10/kmeans-project_2.10-1.0.jar
CLASS=SparkKmeans
#SPARK_HOME=/usr/share/spark
spark-submit --verbose --class $CLASS --master yarn-cluster --num-executors 8 --driver-memory 4g --executor-memory 16g --executor-cores 4 $APP mytest kddcup.data 4 10 8 4 
#kddcup.data_10_percent 
