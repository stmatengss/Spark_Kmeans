APP=./target/scala-2.10/kmeans-project_2.10-1.0.jar
CLASS=SparkKmeans
#SPARK_HOME=/usr/share/spark
for((i=1;i<=10;i++))
do
	spark-submit  \
		--verbose \
		--class $CLASS \
		--master yarn-cluster \
		--num-executors 4  \
		--driver-memory 4g \
		--executor-memory 32g \
		--executor-cores 4 \
		$APP mytest kddcup.data $i 10 4 6 
done
#kddcup.data_10_percent 
