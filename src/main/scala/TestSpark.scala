import it.teamDigitale.daf.utils.{TxtFile, JsonMgmt}
import play.api.libs.json._
import it.teamDigitale.daf.schema.schemaMgmt.{SchemaMgmt, SchemaReport}
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.{Column, Row}
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import it.teamDigitale.daf.schema.{ConvSchema, StdSchema}
import it.teamDigitale.daf.uri.UriDataset
import org.apache.logging.log4j.scala.Logging
import org.apache.logging.log4j.Level
import com.typesafe.config.ConfigFactory

object TestSpark extends App with Logging {
  val dataFilePath = "/Users/lilloraffa/Development/data/daf_injftp/it_torino/agency.txt"
  val conf = new SparkConf().setAppName("Inj-csv").setMaster("local[2]")
  val sc = new SparkContext(conf)
	val sqlContext = new org.apache.spark.sql.SQLContext(sc)
  val dfIn = sqlContext.read
  			.format("com.databricks.spark.csv")
  			.option("header", "true") // Use first line of all files as header
  			.option("delimiter", ",")
  			.option("inferSchema", "true") // Automatically infer data types
  			.load(dataFilePath)
  
  //val schemaMgmt = new SchemaMgmt(convSchema)
  
}