import it.teamDigitale.daf.schema.schemaMgmt.{ConvSchemaGetter, StdSchemaGetter, MetadataLU, SchemaMgmt}
import it.teamDigitale.daf.schema.{ConvSchema, StdSchema}
import it.teamDigitale.daf.injestion.DataInjCsv
import java.io.FileInputStream
import play.api.libs.json._
import scala.annotation.tailrec
import it.teamDigitale.daf.utils.JsonMgmt
import org.apache.logging.log4j.scala.Logging
import org.apache.logging.log4j.Level
import com.typesafe.config.ConfigFactory
import scala.util.{Try, Success, Failure}
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.{Column, Row}
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._


object TestSchemaMgmt extends App with Logging {
  /*
  def typeMgmt(typeCol: String): DataType = {
    
	  val isStruct = typeCol.contains("{")
	  
	  val typeAdj: String = typeCol.toLowerCase() match {
	    case x if x.startsWith("{") => "struct"
	    case x if x.startsWith("[") => {
	      val listType = x.replace("[", "")
	                       .replace("]", "")
	                       .replace("\"", "")
	                       .split(",")
	                       .map(_.trim)
	                       .toList
	      val isNullable = listType.contains("null")
	      listType.filter(x => !x.toLowerCase().equals("null"))(0)
	    }
	    case x => x
	  }
	  
	  
	  typeAdj.replace("\"", "") match {
	    case "string" => StringType
	    case "integer" => IntegerType
	    case "double" => DoubleType
	    case "float" => FloatType
	    case "boolean" => BooleanType
	    case "struct" => StringType  //Workaround for now
	    case x => {
	      println("problem: " + x)
	      StringType
	    }
	  }
	}
  private def getDataStd(df: DataFrame, convSchema: ConvSchema, stdSchema: StdSchema): DataFrame ={
    def toJsonFunc(colNames: Seq[String], str: Seq[Any]): String = {
			val fields = (colNames zip str).map{
				x => """ """" + x._1 + """": """" + x._2 + """""""
			}
			("{" + fields.mkString(",") + "}")
		}        
		def toJson(colNames: Seq[String]) = udf((x: Seq[Any]) => toJsonFunc(colNames, x))
		
		//Define the fields that belongs to the StdSchema. 
		val stdFields = stdSchema.dataSchema.fields.map{ schemaField =>
		  
		  val nameStd = schemaField.name
		  val typeStd = schemaField.typeField
		  val resLUConv = convSchema.reqFields.get.filter(x=>x("field_std")==nameStd)
		  resLUConv match {
		    //The StdSchema Field is not present in the input list --> then it becomes empty (it has been already checked that this field is nullable and not required)
		    case Nil => {
		      lit(null).as(nameStd).cast(typeMgmt(typeStd.toString()))
		    }
		    
		    //A conversion has been identified and it is unique
		    case x::Nil => expr(x("formula")).as(x("field_std"))
		    
		    //More than one conversion has been identified, this should not happen... I'm taking the first element for now and log
		    case x::rest => expr(x("formula")).as(x("field_std"))
		      
		  }

		}
		
		
		
		//Define the fields that do not belongs to the StdSchema and will be added as an additional blob (json) column
		val stdFieldList = convSchema.dataSchema.fields.map(x => x.name)
		val custFieldsList: Seq[(Column, String)] = convSchema.dataSchema.fields
		  .filter(x => !stdFieldList.contains(x.name))
		  .map{x => (expr(x.name).as(x.name), x.name)	}
		val (custFields, custFieldNames) = (custFieldsList.map(_._1), custFieldsList.map(_._2))
		val listFields = stdFields ++ List(toJson(custFieldNames)(array(custFields:_*)).as("custom"))
		df.select(listFields: _*)
		
  }
  */
  
  
  /*
  val file_operational: String = "/Users/lilloraffa/Development/teamdgt/daf/datamgmt_v2/example/data-operational.json"
  val file_dcatap: String = "/Users/lilloraffa/Development/teamdgt/daf/datamgmt_v2/example/data-dcatapit.json"
  val file_dataschema: String = "/Users/lilloraffa/Development/teamdgt/daf/datamgmt_v2/example/data-dataschema.json"
  */
  
  //Vedi in resources/dataschema
  
  //val file_operational: String = "/Users/lilloraffa/Development/teamdgt/daf/datamgmt_v2/example/data-operational.json"
  //val file_dcatap: String = "/Users/lilloraffa/Development/teamdgt/daf/datamgmt_v2/example/data-dcatapit.json"
  //val file_dataschema: String = "/Users/lilloraffa/Development/teamdgt/daf/datamgmt_v2/example/data-dataschema.json"



  val stream_operational = getClass.getResourceAsStream("/dataschema/data-operational.json")
  val stream_dcatap = getClass.getResourceAsStream("/dataschema/data-dcatapit.json")
  val stream_dataschema = getClass.getResourceAsStream("/dataschema/data-dataschema.json")
  
 // val stream_operational = new FileInputStream(file_operational)
  val json_operational = try {  Json.parse(stream_operational) } finally { stream_operational.close() }
  
 // val stream_dcatap = new FileInputStream(file_dcatap)
  val json_dcatap = try {  Json.parse(stream_dcatap) } finally { stream_dcatap.close() }
  
  //val stream_dataschema = new FileInputStream(file_dataschema)
  val json_dataschema = try {  Json.parse(stream_dataschema) } finally { stream_dataschema.close() }
  
  val json: JsValue = Json.obj(
      "ops" -> json_operational,
      "dataschema" -> json_dataschema,
      "dcatap" -> json_dcatap
  )
  

  
  
  val convSchema = (new ConvSchemaGetter(
      metadataJson = Some(json)
      )
  ).getSchema()
  
  println(convSchema)
  
  val stdSchema = (new StdSchemaGetter("daf://dataset/vid/mobility/gtfs_agency")).getSchema()
  println(stdSchema)
  
  convSchema match {
    case Some(s) => {
      val dataInj = new DataInjCsv(new SchemaMgmt(s))
      println(dataInj.doInj())
    }
    case _ => println("ERROR")
  }
  
  /*
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
  convSchema match {
    case Some(s) => {
      val schemaMgmt = new SchemaMgmt(s)
      val timestamp: Long = System.currentTimeMillis / 1000
      val stdDf = getDataStd(dfIn, s, schemaMgmt.stdSchema.get)
    		  .withColumn("owner", expr("'" + s.owner + "'"))
    		  .withColumn("ts", expr("'" + timestamp + "'"))
    	stdDf.show()
    	println(stdDf.schema)
    	stdDf.write
			//.partitionBy("ts")
			.mode(SaveMode.Append)
			.parquet("/Users/lilloraffa/test" + ".parquet")
    }
    case _ => println("ERROR")
  }
  * 
  */
  

}