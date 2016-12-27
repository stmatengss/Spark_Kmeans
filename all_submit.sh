APP=./target/scala-2.10/kmeans-project_2.10-1.0.jar
CLASS=SparkKmeans
#SPARK_HOME=/usr/share/spark
for((i=1;i<=8;i=i*2))
do
	for((j=1;j<=8;j=j*2))
	do
		spark-submit  \
		--verbose \
		--class $CLASS \
		--master yarn-cluster \
		--num-executors $i  \
		--driver-memory 4g \
		--executor-memory 16g \
		--executor-cores $j \
		$APP mytest kddcup.data 4 10 $i $j 
	done
done
#kddcup.data_10_percent 
