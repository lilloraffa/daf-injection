package it.teamDigitale.daf.ingestion

import java.io.File

import com.databricks.spark.avro.SchemaConverters
import it.gov.daf.catalogmanagerclient.model.MetaCatalog
import it.teamDigitale.daf.utils.JsonConverter
import org.apache.avro.Schema.Parser
import org.apache.logging.log4j.scala.Logging
import org.apache.spark.sql.functions.{col, expr}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SaveMode}
import org.apache.spark.{SparkConf, SparkContext}
import it.teamDigitale.daf._
import scala.util.{Failure, Success, Try}

/**
  * Created by fabiana on 23/05/17.
  */
class IngestionManager extends Logging {

  val conf = new SparkConf().setAppName("Inj-csv").setMaster("local[2]")
  val sc = new SparkContext(conf)
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)


  private def readCSV(path: String, sep: String = ",", isHeaderDefined: Boolean = true, customSchema: StructType) = Try {
    sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", isHeaderDefined) // Use first line of all files as header
      .option("delimiter", sep)
      .schema(customSchema)
      //.option("inferSchema", "true") // Automatically infer data types
      .load(path)
  }

  private def enrichDataFrame(df: DataFrame, schema: MetaCatalog) : (DataFrame, List[String]) = {
    val timestamp: Long = System.currentTimeMillis / 1000
    schema.operational.isStd match {
      case x:Integer if x==1 =>
        val newdf = df.withColumn("owner", expr("'" + schema.dcatapit.dctRightsHolder._val + "'"))
          .withColumn("ts", expr("'" + timestamp + "'"))
        val partitionList = List("owner", "ts")
        (newdf, partitionList)
      case _ =>
        val newdf = df.withColumn("ts", expr("'" + timestamp + "'"))
        val partitionList = List("ts")
        (newdf, partitionList)
//      case DatasetType.RAW => //not yet implemented
//        df
    }
  }

  private def writeDF(df:DataFrame, schema: MetaCatalog, filePath: String) = Try{

    val (enrichedDF, partitionList) = enrichDataFrame(df, schema)

    val repartitionList = partitionList.map(x => col(x))
    enrichedDF
      .repartition(repartitionList:_*)  //questa riga ha un costo rilevante
      .write
      .partitionBy(partitionList:_*)
      .mode(SaveMode.Append)
      .parquet(filePath + ".parquet")
  }

  def readAvroSchema(string: String): org.apache.avro.Schema = {
    new Parser().parse(string)
  }

  def write(schema: MetaCatalog, sep: String = ",", isHeaderDefined: Boolean = true): Boolean = {

    val datasetSchema = JsonConverter.toJson(schema.dataschema)

    val avroSchema = readAvroSchema(datasetSchema)

    val inputPath = schema.operational.inputSrc.url
    val outputPath = datastructures.convertToUriDatabase(schema).get.getUrl() // schema.convertToUriDataset().get.getUrl()

    val customSchema: StructType = SchemaConverters.toSqlType(avroSchema).dataType.asInstanceOf[StructType]

    val res = for {
      df <- readCSV(inputPath, sep, isHeaderDefined, customSchema)
      out <- writeDF(df, schema, outputPath)
    } yield out


    res match {
      case Success(_) =>
       logger.info(s"Dataframe correctly stored in $outputPath")
        true
      case Failure(ex) =>
        logger.error(s"Dataset reading failed due a Conversion Exception ${ex.getMessage} \n${ex.getStackTrace.mkString("\n\t")}")
        false
    }
  }
}
