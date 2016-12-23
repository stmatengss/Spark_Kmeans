/*
 * Designed by mateng
*/
import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.util.Vector
import java.util.Random
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel} 
import org.apache.spark.mllib.linalg.Vector  
import org.apache.spark.mllib.linalg.Vectors 
import org.apache.spark.rdd._
import org.apache.spark.util.Vector

object SparkKmeans {

	val fileName = "hdfs://hadoop01.lab.pacman-thu.org:8020/user/2016310622/"
	val rand = new Random(44)
	val eps = 0.01
	var K = 4 
	var randpointNum = 1000
	var Demension = 42
	var iterNum = 10
	var sc: SparkContext = null
	val filterSet = Set(1, 2, 3, 41)

	def main(args: Array[String]) = {
		val conf = new SparkConf().setAppName("Simple Application")
		sc = new SparkContext(conf)
		println("Hello")
		//workCountTest(sc)
		//kmeansTest()
		kmeansTestMllib()
		sc.stop()
	}

	def workCountTest() = {
		val textFileRdd = sc.textFile(fileName + "kddcup.data_10_percent")		
		//val textFileRdd = sc.textFile(fileName + "kddcup.data")	
		println("Begin Kmeans")
		val wordRdd = textFileRdd.flatMap(_.split(","))
		val wordCountRdd = wordRdd.map((_, 1)).reduceByKey(_+_)
		val sample = wordCountRdd.top(20)
		sample.foreach(println(_))
		println("sum = " + wordCountRdd.count.toString)
		println("finish All")
	}

	def timeNow() = {
		System.currentTimeMillis
	}

	def averageData() {
		
	}

	def dataTestPrint(rdd: RDD[Array[Double]]) = {
		rdd.take(10).foreach({
			x => {
				x.foreach(println(_))
				println()
			}
		})	
	}

	def preSolved(rdd: RDD[Array[String]]):RDD[Array[Double]] = {
		val transRdd = rdd.map({
			x => {
				x.zipWithIndex.map(
					y => {
						if (filterSet.contains(y._2)) {
							0.0
						} else {
							y._1.toDouble	
						}
					}
				)
			}
		})
		val MaxRdd = transRdd.reduce(
			(x, y) => {
				for (s <- (x zip y)) yield Math.max(s._1, s._2)
			}
		)
		val MinRdd = transRdd.reduce(
			(x, y) => {
				for (s <- (x zip y)) yield Math.min(s._1, s._2)
			}
		)
		val disRdd = MaxRdd.zip(MinRdd).map(x=>{x._1-x._2})
		disRdd.foreach(println(_))
		transRdd
	}

	def pendingCluster(p: Array[Double], centers: Array[(Int, Array[Double])]):Int = {
		val dis: Array[(Int, Double)] = centers.map({ 
				item => {
				val sum: Double = item._2.zip(p).map{
						case (x, y) => (x - y) * (x - y)
				}.reduce({ (x, y) => {x + y} })
				(item._1, sum)
			}
		})	
		dis.reduce({
			(x, y) => x._2 < y._2 match {
				case true => x
				case false => y
			} 
		})._1
	}

	def kmeansTest() = {
		val textFileRdd = sc.textFile(fileName + "kddcup.data_10_percent")
		val dataSetRdd = textFileRdd.map(x=>x.split(","))
		val dataSetSolvedRdd = preSolved(dataSetRdd)
		var beginTime: Long = 0
		var endTime: Long = 0
		var kPoints = dataSetSolvedRdd.takeSample(false, K).toArray.zipWithIndex.map(x=>(x._2, x._1))
		println("cluster number is " + kPoints.size.toString)
		for (i<-0 to iterNum) {
			beginTime = timeNow()
			val closeSet = dataSetSolvedRdd.map(x=>(pendingCluster(x.toArray, kPoints), (x, 1)))
			kPoints = closeSet.reduceByKey{
				case ((x, xx), (y, yy)) => {
					((x zip y).map(m=>{m._1+m._2}), xx + yy)	
				} 
			}.collect.map{
				case x => (x._1, x._2._1.map(_/x._2._2))
			}
			endTime = timeNow()
			println("Iter round " + i.toString + " Time is " + (endTime - beginTime).toString)
		}
		println("res number is " + kPoints.size.toString)
		for (i<-kPoints) {
			println("Cluster " + i._1.toString)
			i._2.foreach(x=>print(x.toString + " "))
			println
		}
	}

	def kmeansTestMllib() = {
		var beginTime: Long = 0
		var endTime: Long = 0
		val textFileRdd = sc.textFile(fileName + "kddcup.data_10_percent")
		//val lineRdd = textFileRdd.map(s => Vectors.dense(s.split(",").map(_.toDouble)))
		val dataSetRdd = textFileRdd.map(x=>x.split(","))
		val dataSetSolvedRdd = preSolved(dataSetRdd).map(s => Vectors.dense(s))
		println(dataSetSolvedRdd.count)
		beginTime = timeNow()
		val model = KMeans.train(dataSetSolvedRdd, K, 10) 
		model.clusterCenters.toArray.foreach(_.toArray.foreach(println(_)))
		endTime = timeNow()
		println(" Time is " + (endTime - beginTime).toString)

	}
}

class SparkKmeans {
	private var k: Int = 0
	private var seed: Long = 0
	private var eps: Double = 0.0
}
