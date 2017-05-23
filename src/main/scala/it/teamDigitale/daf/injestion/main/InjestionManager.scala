package it.teamDigitale.daf.injestion.main

import it.teamDigitale.daf.datastructures.ConvSchema
import it.teamDigitale.daf.datastructures.Model.Schema
import it.teamDigitale.daf.utils.TxtFile
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by fabiana on 23/05/17.
  */
class InjestionManager {

  val conf = new SparkConf().setAppName("Inj-csv").setMaster("local[2]")
  val sc = new SparkContext(conf)
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)


  private def readDataFrame(path: String, sep: String = ",", isHeaderDefined: Boolean = true) = {
    sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", isHeaderDefined) // Use first line of all files as header
      .option("delimiter", sep)
      .option("inferSchema", "true") // Automatically infer data types
      .load(path)
  }

  def write(schema: Schema, sep: String = ",", isHeaderDefined: Boolean = true) = {

    val pathDf = schema.operational.input_src.url
    val df = readDataFrame(pathDf, sep, isHeaderDefined)
    val path = schema.convertToUriDataset().get.getUrl()

    val timestamp: Long = System.currentTimeMillis / 1000

    ???





  }
}
