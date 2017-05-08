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
import it.teamDigitale.daf.schema.ConvSchema
import org.apache.log4j.Logger
import org.apache.log4j.Level
import com.typesafe.config.ConfigFactory

class DataInjCsv(convSchema: ConvSchema) extends Serializable {
  
  var isStdData = false  //change the logic!!!
  var config = ConfigFactory.load()
  
  def doInj(): Boolean = {
    
    //Build the spark context
    val conf = new SparkConf().setAppName("Inj-csv").setMaster("local[2]")
    val sc = new SparkContext(conf)
  	val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    
    val test = try{
      val schemaMgmt = new SchemaMgmt(convSchema)
	    //Get the column names
  		val dataFilePath = schemaMgmt.convSchema.src.get("url").getOrElse("")
  		val firstLine = TxtFile.firstLine(dataFilePath).getOrElse("-1")
  		val sep = TxtFile.csvGetSep(firstLine)
  		val colNames = firstLine.split(sep)
  
  		//Do some kind of checks on the csv file
  		
  
  		//Get Dataset
  		val dfIn = sqlContext.read
  			.format("com.databricks.spark.csv")
  			.option("header", "true") // Use first line of all files as header
  			.option("delimiter", sep)
  			.option("inferSchema", "true") // Automatically infer data types
  			.load(dataFilePath)
  
  			
  		//Do all the checks before returing the DF
  			val newDf = getData(dfIn, schemaMgmt)
  
  
  			//add Col Owner and timestamp
  			val timestamp: Long = System.currentTimeMillis / 1000
  			println(newDf.printSchema())
  			println(schemaMgmt.convSchema.owner)
  			
  			val newDfOTCol = newDf
  				.withColumn("owner", expr("'" + schemaMgmt.convSchema.owner + "'"))
  				.withColumn("genre", expr("'" + schemaMgmt.finalSchema.cat(0) + "'"))
  				.withColumn("ts", expr("'" + timestamp + "'"))
  
  
  			println(newDfOTCol.printSchema)
  			println(newDfOTCol.show())
  			Some(newDfOTCol)
  			
  			saveData(newDfOTCol, schemaMgmt)
  			//saveData(newDfOTCol, schemaMgmt)
  			
  			
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


	private def getData(df: DataFrame, schemaMgmt: SchemaMgmt): DataFrame ={
			println(schemaMgmt.finalSchema)
			println(schemaMgmt.convSchema)
			println(schemaMgmt.stdSchema)
			
			
			def toJsonFunc(colNames: Seq[String], str: Seq[Any]): String = {
					val fields = (colNames zip str).map{
						x => """ """" + x._1 + """": """" + x._2 + """""""
					}
					("{" + fields.mkString(",") + "}")
			}        
			def toJson(colNames: Seq[String]) = udf((x: Seq[Any]) => toJsonFunc(colNames, x))

			/*
			 * 3 possible fields:
			 * 1. stdReq -> list of std required fields
			 * 2. stdOpt -> list of std optional fields as json
			 * 3, custom -> list of custom fields as json
			 */
			
			println("Entered into getData")
			
			
			//Check if a finalSchema has been correctly built, and implement the list of required columns for the dataframe
			val listFields: List[Column] = (schemaMgmt.finalSchema.fields, schemaMgmt.convSchema.stdSchemaName) match {

			case (Some(fields), Some(stdSchemaName)) => {
			  
			  //1. get stdReq Fields
			  val reqStdFields = schemaMgmt.finalSchema.reqStdFields.map(x => expr(x("formula")).as(x("field_std")))
			  val (optStdFields, optStdFieldNames): (Seq[Column], Seq[String]) = (
			      schemaMgmt.finalSchema.optStdFields.map(x => expr(x("formula")).as(x("field_std"))),
			      schemaMgmt.finalSchema.optStdFields.map(x => x("field_std"))
			  )
			  
			  //Check if custom fields needs to be added
				val (custFields, custFieldNames) = schemaMgmt.convSchema.custFields match {
					case Some(sCust) => {

						val listArgs: Seq[(Column, String)] = sCust.value.map{
							x => {
								val fieldName = (x \ "name").as[String]
								val formula = (x \ "formula").asOpt[String]
								(expr(formula.getOrElse(fieldName)).as(fieldName), fieldName)
							}

						}
						(listArgs.map(_._1), listArgs.map(_._2))  
					}

					case _ => (Seq(), Seq())
				}
			  
						//reqFields ++ List(toJson(custFieldNames)(array(custFields:_*)).as("custom")) ++ List(struct(custFields: _*).as("raw_data"))
						reqStdFields ++ List(toJson(optStdFieldNames)(array(optStdFields:_*)).as("opt_std")) ++ List(toJson(custFieldNames)(array(custFields:_*)).as("custom")) ++ List(struct(custFields: _*).as("raw_data"))
			}
			case (None, Some(stdSchemaName)) =>{
			  schemaMgmt.stdSchema match {
			    case Some(stdSchema) =>{
			      val stdFields = stdSchema.fields.value.map(x => ((x \ "name").as[String], (x \ "required").as[Int]))
			      
			      
			      val fieldsIn = df.columns.toList.map(x => Map("field_std" -> x, "formula" -> x))
			      
			      val (testStd, reqStdFieldsList, optStdFieldsList) = schemaMgmt.verGetSchema(stdSchema, fieldsIn)
			      
			      if(testStd) {
			        val reqStdFields = reqStdFieldsList.map(x => expr(x("formula")).as(x("field_std")))
			        val (optStdFieldsCol, optStdFieldNames): (Seq[Column], Seq[String]) = (
      			      optStdFieldsList.map(x => expr(x("formula")).as(x("field_std"))),
      			      optStdFieldsList.map(x => x("field_std"))
      			  )
      			  isStdData = true
			         reqStdFields ++ List(toJson(optStdFieldNames)(array(optStdFieldsCol:_*)).as("opt_std")) ++ List(toJson(Seq())(array(Seq():_*)).as("custom")) ++ List(struct(Seq(): _*).as("raw_data"))
			      } else {
			        List()
			      }
			      
			    }
			    case _ => List()
			  }

			}
			case _ => List()
			}
			df.select(listFields: _*)
			
			

	}

	def getStdFieldsFromData() = {
	  
	}


	private def saveData(df: DataFrame, schemaMgmt: SchemaMgmt): Boolean = {
			//1. Understand which type of data needs to be stored: (Vip/Elaborated vs Raw)

			(schemaMgmt.finalSchema.stdSchemaName, isStdData) match {
			case (Some(s), _)    => {
				//the standard schema exists
				saveStdData(df, schemaMgmt)
			}
			case (None, true)    => {
				//the standard schema exists
				saveStdData(df, schemaMgmt)
			}
			case (None, false) => {
				//save raw data
				saveRawData(df, schemaMgmt) &
				saveRawData2(df, schemaMgmt)
			}
			}
	}

	private def saveStdData(df: DataFrame, schemaMgmt: SchemaMgmt): Boolean = {
	  
	  try {
	    val basePath = config.getString("Inj-properties.hdfsBasePath")
	    //val basePath = "/Users/lilloraffa/Development/data/daf_test/"
			//val nameData = schemaMgmt.finalSchema.nameDataset
			val nameData = schemaMgmt.finalSchema.stdSchemaName.getOrElse("__noStdName")
			val typePath = "stdData/"
			val genrePath = schemaMgmt.finalSchema.cat(0)  //make stringent check here
			val ownGrpPath = schemaMgmt.finalSchema.groupOwn
			val savePath = basePath + typePath + genrePath + "/" + ownGrpPath + "/"

			println("1 - " +  savePath)
			println(savePath + nameData + ".parquet")
			println(df.printSchema())
			println(df.show())

			df
			.drop(df.col("raw_data"))
			.repartition(col("owner"))
			.write
			.partitionBy("owner")
			.mode(SaveMode.Append)
			.parquet(savePath + nameData + ".parquet")
			//.parquet(savePath + "std.parquet")
			true
	  } catch {
	    case unknown: Throwable => {
        println("Exception in DataInjCsv -> saveStdData() - " + unknown)
        false
      }
	  }

	}

	private def saveRawData(df: DataFrame, schemaMgmt: SchemaMgmt): Boolean = {
	  try{
	    val basePath = config.getString("Inj-properties.hdfsBasePath")
	    //val basePath = "/Users/lilloraffa/Development/data/daf_test/"
			val nameData = schemaMgmt.finalSchema.nameDataset
			val typePath = "ordData/"
			val ownerPath = schemaMgmt.finalSchema.owner
			val genrePath = schemaMgmt.finalSchema.cat(0)  //make stringent check here
			val ownGrpPath = schemaMgmt.finalSchema.groupOwn
			val savePath = basePath + typePath + ownerPath + "/" + genrePath + "/" + ownGrpPath + "/"

			println("2 - " +  savePath)

			df
			.drop(df.col("custom"))
			.drop(df.col("opt_std"))
			.write
			.mode(SaveMode.Append)
			.parquet(savePath + nameData + ".parquet")
			true
	  } catch {
	    case unknown: Throwable => {
        println("Exception in DataInjCsv -> saveRawData() - " + unknown)
        false
      }
	  }
			
	}

	//This saves into a unique parquet file
	private def saveRawData2(df: DataFrame, schemaMgmt: SchemaMgmt): Boolean = {
	  try {
	    val basePath = config.getString("Inj-properties.hdfsBasePath")
	    //val basePath = "/Users/lilloraffa/Development/data/daf_test/"
			val typePath = "ordData/"
			val ownerPath = schemaMgmt.finalSchema.owner
			val genrePath = schemaMgmt.finalSchema.cat(0)  //make stringent check here
			val ownGrpPath = schemaMgmt.finalSchema.groupOwn
			val savePath = basePath + typePath + ownGrpPath

			println("2 - " +  savePath)

			df
			.drop(df.col("opt_std"))
			.drop(df.col("raw_data"))
			.repartition(col("owner"), col("genre"))
			.write
			.partitionBy("owner", "genre")
			.mode(SaveMode.Append)
			.parquet(savePath + "/data.parquet")
			true
	  } catch {
	    case unknown: Throwable => {
        println("Exception in DataInjCsv -> saveRawData2() - " + unknown)
        false
      }
	  }

	}
}