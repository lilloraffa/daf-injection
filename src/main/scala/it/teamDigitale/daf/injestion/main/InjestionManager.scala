package it.teamDigitale.daf.injestion.main

import com.databricks.spark.avro.SchemaConverters
import com.databricks.spark.avro.SchemaConverters.SchemaType
import it.teamDigitale.daf.datastructures.ConvSchema
import it.teamDigitale.daf.datastructures.Model.Schema
import it.teamDigitale.daf.utils.TxtFile
import org.apache.logging.log4j.scala.Logging
import org.apache.spark.sql.{Column, DataFrame, SaveMode}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.StructType
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.{Failure, Success, Try}

/**
  * Created by fabiana on 23/05/17.
  */
class InjestionManager extends Logging {

  val conf = new SparkConf().setAppName("Inj-csv").setMaster("local[2]")
  val sc = new SparkContext(conf)
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)

  private def getPartitionList(schema: Schema): List[String] = {
    //Standard case
    if(schema.operational.is_std)
      List("owner", "ts")
    else List("ts")
  }


  private def readCSV(path: String, sep: String = ",", isHeaderDefined: Boolean = true, customSchema: StructType) = Try {
    sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", isHeaderDefined) // Use first line of all files as header
      .option("delimiter", sep)
      .schema(customSchema)
      //.option("inferSchema", "true") // Automatically infer data types
      .load(path)
  }

  private def writeDF(df:DataFrame, partitionList: List[String], filePath: String) = Try{

    val repartitionList = partitionList.map(x => col(x))

    df
      .repartition(repartitionList:_*)  //questa riga ha un costo rilevante
      .write
      .partitionBy(partitionList:_*)
      .mode(SaveMode.Append)
      .parquet(filePath + ".parquet")
  }

  def write(avroSchema: org.apache.avro.Schema, schema: Schema, sep: String = ",", isHeaderDefined: Boolean = true) = {

    val inputPath = schema.operational.input_src.url
    val outputPath = schema.convertToUriDataset().get.getUrl()

    val timestamp: Long = System.currentTimeMillis / 1000

    val customSchema: StructType = SchemaConverters.toSqlType(avroSchema).dataType.asInstanceOf[StructType]
    val partitionList = getPartitionList(schema)

    val res = for {
      df <- readCSV(inputPath, sep, isHeaderDefined, customSchema)
      out <- writeDF(df, partitionList, outputPath)
    } yield out


    res match {
      case Success(_) =>
       logger.info(s"Dataframe correctly stored in $outputPath")
        true
      case Failure(ex) =>
        logger.error(s"Dataset reading failed due a Conversion Exception ${ex.getMessage}")
        false
    }
  }
}
