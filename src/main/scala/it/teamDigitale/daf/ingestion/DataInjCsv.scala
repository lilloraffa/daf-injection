package it.teamDigitale.daf.ingestion

import it.teamDigitale.daf.utils.TxtFile
import it.teamDigitale.daf.schemamanager.{SchemaMgmt, SchemaReport}
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.{Column, Row}
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import it.teamDigitale.daf.datastructures.uri.UriDataset
import org.apache.logging.log4j.scala.Logging
import org.apache.logging.log4j.Level
import com.typesafe.config.ConfigFactory
import it.teamDigitale.daf.datastructures.Model.DatasetType
import it.teamDigitale.daf.datastructures.{ConvSchema, StdSchema}
import it.teamDigitale.daf.schemamanager.{FieldTypeMgmt, SchemaMgmt}
@Deprecated
class DataInjCsv(schemaMgmt: SchemaMgmt) extends Serializable with Logging {

  var isStdData = false  //change the logic!!!
  var config = ConfigFactory.load()

  def doInj(): InjReport = {

    //Build the spark context
    val conf = new SparkConf().setAppName("Inj-csv").setMaster("local[2]")
    val sc = new SparkContext(conf)
  	val sqlContext = new org.apache.spark.sql.SQLContext(sc)

    val resultReport: InjReport = try{

	    //Get the column names and the spark dataframe
  		val dataFilePath = schemaMgmt.convSchema.src.url
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

  		val timestamp: Long = System.currentTimeMillis / 1000

  		//Code to save Ordinary Dataset to be used in the pattern matching below
  		def saveOrd(uri: UriDataset): Boolean = {
  		  //Create and save the Ordinary DataFrame
    		val ordinaryDf = dfIn
    		  .withColumn("ts", expr("'" + timestamp + "'"))
    		saveOrdDs(ordinaryDf, schemaMgmt.convSchema, uri)
  		}

  		//Code to save Std Dataset to be used in the pattern matching below
  		def saveStd(stdSchemaIn: StdSchema): Boolean = {
  		  //If StdSchema is defined, create the StdDF and save it
    		val stdDf = getDataStd(dfIn, schemaMgmt.convSchema, stdSchemaIn)
    		  .withColumn("owner", expr("'" + schemaMgmt.convSchema.owner + "'"))
    		  .withColumn("ts", expr("'" + timestamp + "'"))
    		println(stdDf.schema)

    		saveStdDs(stdDf, schemaMgmt.convSchema, UriDataset(stdSchemaIn.uri))
  		}

  		schemaMgmt.schemaReport match {


    		//StdSchema Exists and all the rest of the tests went ok
    		case SchemaReport(
    		    _,  //hasInputDataSchema
    		    true,  //hasStdSchema verifica se ha uno standard schema associato
    		    true,  //checkStdSchema  verifica coerenza tra stdschema e ordschema
    		    true   //checkInSchema verifica coerenza tra lo schema del convSchema e lo schema del dataset da inserire
    		) =>
	        //TODO do the checks based on the content of the df
    		  val uriDs = getUriDs(schemaMgmt.convSchema, "ord")

	        val statusOrd = saveOrd(uriDs)
	        val statusStd = saveStd(schemaMgmt.stdSchema.get)

	        InjReport(
	            uri = uriDs.getUri(),
              url = uriDs.getUrl(),
              stdSchemaUri = schemaMgmt.convSchema.stdSchemaUri,
              isStd = schemaMgmt.convSchema.isStd,
              isInStd = true,
              isOrd = true,
              statusOrd = Some(statusOrd),
              statusStd = Some(statusStd),
              statusRaw = None
	        )

    		//It is an ordinary dataset, so it has no StdSchema associated with
    		case SchemaReport(
    		    _,  //hasInputDataSchema
    		    false,  //hasStdSchema
    		    _,  //checkStdSchema
    		    true   //checkInSchema
    		) =>{

	        //TODO do the checks based on the content of the df
    		  val uriDs = getUriDs(schemaMgmt.convSchema, "ord")
	        val statusOrd = saveOrd(uriDs)

	        InjReport(
	            uri = uriDs.getUri(),
              url = uriDs.getUrl(),
              stdSchemaUri = schemaMgmt.convSchema.stdSchemaUri,
              isStd = schemaMgmt.convSchema.isStd,
              isOrd = true,
              statusOrd = Some(statusOrd),
              statusStd = None,
              statusRaw = None
	        )
  		  }
    		//Dataset could not be associated with neither Std or Ordinary, so it is treated as Raw
    		case _ => {

    		  val uriDs = getUriDs(schemaMgmt.convSchema, "raw")
  		    val statusRaw = saveRawDs(dfIn, uriDs)

  		    InjReport(
	            uri = uriDs.getUri(),
              url = uriDs.getUrl(),
              stdSchemaUri = schemaMgmt.convSchema.stdSchemaUri,
              isStd = schemaMgmt.convSchema.isStd,
              isRaw = true,
              statusOrd = None,
              statusStd = None,
              statusRaw = Some(statusRaw)
	        )
        }
  		}

	  } catch {
	    case unknown: Throwable => {
        logger.error("Error in Injestion. No injestion performed.")
        val uriDs = getUriDs(schemaMgmt.convSchema)
        InjReport(
	            uri = uriDs.getUri(),
              url = uriDs.getUrl(),
              stdSchemaUri = schemaMgmt.convSchema.stdSchemaUri,
              isStd = schemaMgmt.convSchema.isStd,
              statusOrd = None,
              statusStd = None,
              statusRaw = None
	        )
      }
	  }

    sc.stop()
    resultReport

  }
  /*
   * This function is needed to get the uri of the incoming dataset, it manages the construction of uri for new dataset.
   */
  private def getUriDs(convSchema: ConvSchema, typeDsIn: String = ""): UriDataset = {
    (convSchema.uri, convSchema.isStd) match {
      //URI already exists, so the dataset is already present in the catalogue
      case (Some(s), _) => UriDataset(s)

      case (None, true) => UriDataset(
            domain = "daf",
            typeDs = DatasetType.withNameOpt("std").get,
            groupOwn = convSchema.groupOwn,
            owner = convSchema.owner,
            theme = convSchema.theme,
            nameDs = convSchema.dataSchema.name
        )
      //URI does not exists, so the dataset is new -> URI will be created
      case _ =>{
        //Get the Dataset Type on the basis of the available info
        val typeDs = if (typeDsIn.equals("ord") || typeDsIn.equals("raw")) typeDsIn else "raw"
        UriDataset(
            domain = "daf",
            typeDs = DatasetType.withNameOpt(typeDs).get,
            groupOwn = convSchema.groupOwn,
            owner = convSchema.owner,
            theme = convSchema.theme,
            nameDs = convSchema.dataSchema.name
        )
      }
    }

  }


  private def getDataStd(df: DataFrame, convSchema: ConvSchema, stdSchema: StdSchema): DataFrame ={
    def toJsonFunc(colNames: Seq[String], str: Seq[Any]): String = {
			val fields = (colNames zip str).map{
				x => """ """" + x._1 + """": """" + x._2 + """""""
			}
			"{" + fields.mkString(",") + "}"
		}
		def toJson(colNames: Seq[String]) = udf((x: Seq[Any]) => toJsonFunc(colNames, x))

		//Define the fields that belongs to the StdSchema.
		val stdFields = stdSchema.dataSchema.fields.map{ schemaField =>

		  val nameStd = schemaField.name
		  val typeStd = schemaField._type
		  val resLUConv = convSchema.reqFields.filter(x=> x.fieldStd==nameStd)
		  resLUConv match {
		    //The StdSchema Field is not present in the input list --> then it becomes empty (it has been already checked that this field is nullable and not required)
		    case Nil => lit(null).as(nameStd).cast(FieldTypeMgmt.convAvro2Spark(typeStd.toString))

		    //A conversion has been identified and it is unique
		    case x::Nil => expr(x.formula).as(x.fieldStd)

		    //More than one conversion has been identified, this should not happen... I'm taking the first element for now and log
		    case x::rest =>
					logger.warn(s"Multiple conversions identified for the attribute $nameStd. Only one will be taken")
					expr(x.formula).as(x.fieldStd)

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

  private def saveOrdDs(df: DataFrame, convSchema: ConvSchema, uriMgmt: UriDataset): Boolean = {

    try {

			val savePath = uriMgmt.getUrl()

			df
			.repartition(col("ts"))
			.write
			.partitionBy("ts")
			.mode(SaveMode.Append)
			.parquet(savePath + ".parquet")

			true
	  } catch {
	    case unknown: Throwable =>
        logger.error("Exception in DataInjCsv -> saveOrdData() - " + unknown)
        false

	  }
  }

  private def saveStdDs(df: DataFrame, convSchema: ConvSchema, uriMgmt: UriDataset): Boolean = {

    try {
      //Here you need to manage if the dataset is a new StdSchema or an existing one.
			val savePath = uriMgmt.getUrl()

			df
			.repartition(col("owner"), col("ts"))
			.write
			.partitionBy("owner", "ts")
			.mode(SaveMode.Append)
			.parquet(savePath + ".parquet")


			true
	  } catch {
	    case unknown: Throwable =>
        logger.error("Exception in DataInjCsv -> saveStdData() - " + unknown)
        false

	  }
  }

  private def saveRawDs(df: DataFrame, uriMgmt: UriDataset): Boolean = {

    try {


			val savePath = uriMgmt.getUrl()

			df
			//.repartition(col("ts"))
			.write
			//.partitionBy("ts")
			.mode(SaveMode.Append)
			.parquet(savePath + ".parquet")
			true
	  } catch {
	    case unknown: Throwable =>
        logger.error("Exception in DataInjCsv -> saveOrdData() - " + unknown)
        false
	  }
  }

//  def cleanDf(df: DataFrame) = {
//    df.schema
//  }

}