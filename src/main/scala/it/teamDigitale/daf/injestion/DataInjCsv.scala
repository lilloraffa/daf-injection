package it.teamDigitale.daf.injestion

import it.teamDigitale.daf.utils.{TxtFile, JsonMgmt}
import play.api.libs.json._
import it.teamDigitale.daf.schema.schemaMgmt.SchemaMgmt
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

class DataInjCsv(convSchema: ConvSchema) extends Serializable with Logging {
  
  var isStdData = false  //change the logic!!!
  var config = ConfigFactory.load()
  
  def doInj(): Boolean = {

    //Build the spark context
    val conf = new SparkConf().setAppName("Inj-csv").setMaster("local[2]")
    val sc = new SparkContext(conf)
  	val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    
    val test = try{
      
	    //Get the column names and the spark dataframe
  		val dataFilePath = convSchema.src.get("url").getOrElse("")
  		val firstLine = TxtFile.firstLine(dataFilePath).getOrElse("-1")
  		val sep = TxtFile.csvGetSep(firstLine)
  		val colNames = firstLine.split(sep)
  		val dfIn = sqlContext.read
      			.format("com.databricks.spark.csv")
      			.option("header", "true") // Use first line of all files as header
      			.option("delimiter", sep)
      			.option("inferSchema", "true") // Automatically infer data types
      			.load(dataFilePath)
      //TODO put here the inference on the input schema (name and type) and pass it to SchemaMgmt
      val schemaMgmt = new SchemaMgmt(convSchema)
  		
  		//check if a convSchema has been correctly formed, otherwise it does an ingestion of a Raw dataset
  		
		  //A ConvSchema exists, proceed for Standard and/or Ordinary Dataset ingestion
  		
  			
  		val timestamp: Long = System.currentTimeMillis / 1000
  		
  		//Code to save Ordinary Dataset to be used in the pattern matching below
  		val saveOrd = {
  		  //Create and save the Ordinary DataFrame
    		val ordinaryDf = dfIn
    		  .withColumn("ts", expr("'" + timestamp + "'"))
    		val testSavedOrdinary: Boolean = saveOrdDs(ordinaryDf, schemaMgmt)
  		}
  		//Code to save Std Dataset to be used in the pattern matching below
  		val saveStd = {
  		  //If StdSchema is defined, create the StdDF and save it
    		val stdDf = getDataStd(dfIn, convSchema, stdSchema)
    		  .withColumn("ts", expr("'" + timestamp + "'"))
    		  .withColumn("owner", expr("'" + convSchema.owner + "'"))
    		val testSavedStd: Boolean = saveStdDs(stdDf, schemaMgmt)
  		}
  		
  		//Get the dataset uri
  		val uriDs = getUriDs() 
  		
  		schemaMgmt.schemaReport match {
  		  
    		//StdSchema Exists and all the rest of the tests went ok
    		case SchemaReport(
    		      hasInputDataSchema = true,
    		      hasStdSchema = true,
    		      checkStdSchema = true,
    		      checkInSchema = true      
    		) =>{
	        //TODO do the checks based on the content of the df
	        saveOrd
	        saveStd
	        true
  		  }
    		//
    		case SchemaReport(
    		      hasInputDataSchema = true,
    		      hasStdSchema = false,
    		      checkStdSchema = _,
    		      checkInSchema = true      
    		) =>{
	        //TODO do the checks based on the content of the df
	        saveOrd
	        true
  		  }
    		case _ => {
    		  
    		  val uriMgmt = UriDataset(
  		        domain = "daf",
              typeDs = "raw",
              groupOwn = convSchema.groupOwn,
              owner = convSchema.owner,
              theme = convSchema.theme,
              nameDs = )
            
  		    saveRawDs(dfIn, uri)
        }
  		}
  		

  			
	  } catch {
	    case unknown: Throwable => {
        println("Exception in DataInjCsv -> doInj() - " + unknown)
        println("Nothing to injest")
        false
      }
	  }
	  sc.stop()
	  test
    
    
  }
  
  private def getUriDs(convSchema: ConvSchema): String {
    
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
		  val resLUConv = convSchema.reqFields.get.filter(x=>x("field_std")==nameStd)
		  resLUConv match {
		    //The StdSchema Field is not present in the input list --> then it becomes empty (it has been already checked that this field is nullable and not required)
		    case Nil => lit(null).as(nameStd)
		    
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
  
  private def saveOrdDs(df: DataFrame, convSchema: ConvSchema): Boolean = {
    
    try {
	    
	    val uriMgmt = UriDataset(convSchema.uri)
	    /*
	    val uriMgmt = UriDataset(convSchema.uri)
			val nameData = uriMgmt.nameDs
			val typePath = uriMgmt.typeDs
			val genrePath = convSchema.theme
			val ownGrpPath = convSchema.groupOwn
			val savePath = basePath + typePath + genrePath + "/" + ownGrpPath + "/" + nameData
			* 
			*/
			val savePath = uriMgmt.getUrl
			
			df
			.repartition(col("ts"))
			.write
			.partitionBy("ts")
			.mode(SaveMode.Append)
			.parquet(savePath + ".parquet")
			true
	  } catch {
	    case unknown: Throwable => {
        logger.error("Exception in DataInjCsv -> saveOrdData() - " + unknown)
        false
      }
	  }
  }
  
  private def saveStdDs(df: DataFrame, convSchema: ConvSchema): Boolean = {
    
    try {
	    val uriMgmt = UriDataset(convSchema.stdSchemaUri)
			val savePath = uriMgmt.getUrl
			
			df
			.repartition(col("owner"), col("ts"))
			.write
			.partitionBy("owner", "ts")
			.mode(SaveMode.Append)
			.parquet(savePath + ".parquet")
			true
	  } catch {
	    case unknown: Throwable => {
        logger.error("Exception in DataInjCsv -> saveStdData() - " + unknown)
        false
      }
	  }
  }
  
  private def saveRawDs(df: DataFrame, convSchema: ConvSchema): Boolean = {
    
    try {
	    convSchema.uri match {
	      case 
	    }
	    val uriMgmt = UriDataset(convSchema.uri)
	    /*
	    val uriMgmt = UriDataset(convSchema.uri)
			val nameData = uriMgmt.nameDs
			val typePath = uriMgmt.typeDs
			val genrePath = convSchema.theme
			val ownGrpPath = convSchema.groupOwn
			val savePath = basePath + typePath + genrePath + "/" + ownGrpPath + "/" + nameData
			* 
			*/
			val savePath = uriMgmt.getUrl
			
			df
			.repartition(col("ts"))
			.write
			.partitionBy("ts")
			.mode(SaveMode.Append)
			.parquet(savePath + ".parquet")
			true
	  } catch {
	    case unknown: Throwable => {
        logger.error("Exception in DataInjCsv -> saveOrdData() - " + unknown)
        false
      }
	  }
  }

}