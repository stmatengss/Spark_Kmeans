APP=./target/scala-2.10/kmeans-project_2.10-1.0.jar
CLASS=SparkKmeans
#SPARK_HOME=/usr/share/spark
for((i=1;i<=32;i=i*2))
do
	spark-submit  \
		--verbose \
		--class $CLASS \
		--master yarn-cluster \
		--num-executors 1  \
		--driver-memory 4g \
		--executor-memory 32g \
		--executor-cores $i \
		$APP mytest kddcup.data 4 10 
done
#kddcup.data_10_percent 
