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

	val fileName:String = "hdfs://hadoop01.lab.pacman-thu.org:8020/user/2016310622/"
	val rand = new Random(44)
	val eps:Double = 0.01
	var kNum:Int = 4 
	var randpointNum:Int = 1000
	var Demension:Int = 42
	var iterNum:Int = 10
	var sc: SparkContext = null
	val filterSet = Set(1, 2, 3, 41)
	val conf = new SparkConf().setAppName("K-means")

	def main(args: Array[String]) = {
		sc = new SparkContext(conf)
		println("It works!")
		//workCountTest(sc)
		if (args.length <= 6) {
			kNum = args(2).toInt
			iterNum = args(3).toInt
			println("excutor = " + args(4))
			println("cores = " + args(5))
			if(args(0) == "mytest") {
				kmeansTest(args(1))
			} else if (args(0) == "mllibtest") {
				kmeansTestMllib(args(1))
			} else {
				println("Wrong argvs!")
			}
		} else {
			println("Please use SpeakKmeans [mllib/test] [file_url] [K] [iter_num] [excutor_num] [cores]")
		}
		sc.stop()
	}

	def printConf(): Unit = {
		val res = conf.getExecutorEnv
		println("len = " + res.size.toString)
		res.foreach(println(_))
	}

	def workCountTest():Unit = {
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

	def dataTestPrint(rdd: RDD[Array[Double]]):Unit = {
		rdd.take(10).foreach({
			x => {
				x.foreach(println(_))
				println()
			}
		})	
	}

	def preSolved(rdd: RDD[Array[String]], isNormalize: Boolean = false):RDD[Array[Double]] = {
		def hashString(s: String):Double = {
			s.toCharArray.map({
				_.toDouble
			}).reduce(_+_)	
		} 
		val transRdd = rdd.map({
			x => {
				x.zipWithIndex.map(
					y => {
						if (filterSet.contains(y._2)) {
							hashString(y._1)	
						} else {
							y._1.toDouble	
						}
					}
				)
			}
		})
		if (isNormalize == false) return transRdd
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
		val disRdd:Array[Double] = MaxRdd.zip(MinRdd).map(x=>{Math.max(x._1-x._2, 1e-1)})
		transRdd.map({
			x => (x zip disRdd).map{case (x,y)=>{x/y}}	
		})
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

	def kmeansTest(fileUrl: String): Unit = {
		var beginTime: Long = 0
		var endTime: Long = 0
		var allBeginTime: Long = 0
		var allEndTime: Long = 0
		var sumtTime: Long = 0
		val preBeginTime = timeNow()
		val textFileRdd = sc.textFile(fileName + fileUrl)
		val dataSetRdd = textFileRdd.map(x=>x.split(","))
		val dataSetSolvedRdd = preSolved(dataSetRdd, true)
		dataSetSolvedRdd.cache
		var kPoints = dataSetSolvedRdd.takeSample(false, kNum).toArray.zipWithIndex.map(x=>(x._2, x._1))
		val preEndTime = timeNow()
		println("presolving time is " + (preBeginTime - preEndTime).toString)
		println("cluster number is " + kPoints.size.toString)
		allBeginTime = timeNow()
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
		allEndTime = timeNow()
		println("All time is " + (allEndTime - allBeginTime).toString)
		println("res number is " + kPoints.size.toString)
		for (i<-kPoints) {
			println("Cluster " + i._1.toString)
			i._2.foreach(x=>print(x.toString + " "))
			println
		}
	}

	def kmeansTestMllib(fileUrl: String): Unit = {
		var beginTime: Long = 0
		var endTime: Long = 0
		val textFileRdd = sc.textFile(fileName + fileUrl)
		val dataSetRdd = textFileRdd.map(x=>x.split(","))
		val dataSetSolvedRdd = preSolved(dataSetRdd).map(s => Vectors.dense(s))
		println(dataSetSolvedRdd.count)
		beginTime = timeNow()
		val model = KMeans.train(dataSetSolvedRdd, kNum, 10) 
		//model.clusterCenters.toArray.foreach(_.toArray.foreach(println(_)))
		val kPoints = model.clusterCenters.toArray
		println("res number is " + kPoints.size.toString)
		kPoints.zipWithIndex.foreach({
			i => {
				println("Cluster " + i._2.toString)
				i._1.toArray.foreach(x=>print(x.toString + ""))
				println
			}
		})	
		endTime = timeNow()
		println(" Time is " + (endTime - beginTime).toString)
	}
}
