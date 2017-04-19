package it.teamDigitale.daf.schema.schemaMgmt

import play.api.libs.json._
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.bson.codecs.Macros._
import it.teamDigitale.daf.utils.MongoMgmt
import it.teamDigitale.daf.utils.HelperMongo._
import it.teamDigitale.daf.schema.ConvSchema
import play.api.libs.json.JsValue.jsValueToJsLookup
//import it.teamDigitale.daf.schema.schemaMgmt.SchemaGetter



class ConvSchemaGetter(nameDataset: Option[String] = None, convSchemaPath: Option[String] = None, dataPath: Option[String] = None, owner: Option[String] = None, infoData: Option[Map[String, String]] = None) extends SchemaGetter[ConvSchema]{
  
  //val infoQuery: Map[String,String] = getInfoQuery()
  
   def getSchema() ={
    //TODO change this to implement the new process
	  /*
	   * 1. Mi dai nome del file dati e cerco il convSchema nel datastore
	   * 1.1 Se convSchema non e' definito, lo si crea di default, con indicazione di dataset inserito senza schema
	   * 2. Mi dai il nome del convSchema, cerco il file dei dati
	   *
	   */
	  
	  (nameDataset, convSchemaPath, dataPath, owner, infoData) match {
		
	    // 1. Prendo nome di convSchema e Owner
  		case (Some(nameDatasetIn), None, None, Some(ownerIn), None) => {
  		  val infoQuery = Map("name_dataset" -> nameDatasetIn, "owner"-> ownerIn)
  		  retrieveSchema(infoQuery)
  		}
    	// 2. Prendo nome del file e Owner
  		case (None, None, Some(dataPathIn), Some(ownerIn), None) => {
  		  val infoQuery:Map[String,String] = Map("src.url" -> dataPathIn, "owner"-> ownerIn)
  		  val schema = retrieveSchema(infoQuery)
  		  // Se esiste, allora ritorno quello, altrimenti devo crearlo
  		  schema match {
  		    case Some(s) => Some(s)
  		    case _ => {
  		      //Se non trovo il convSchema, ne creo uno nuovo sulla base delle informazioni estratte direttamente dal dataset. Taggo il dataset come "diverso"
  		      //TODO Implement autonomous injestion + convSchema search based on dataset in input
  		      println("Creation of new convSchema not yet implemented")
  		      None
  		    }
  		  }
  		}
    	// 3. Solo pathFile + other info dei dati
  		case (None, None, Some(dataPathIn), Some(ownerIn), Some(infoDataIn)) => {
    		val infoQuery = Map("src.url" -> dataPathIn, "owner"-> ownerIn)
  		  val schema: Option[ConvSchema] = retrieveSchema(infoQuery)
  		  // Se esiste, allora ritorno quello, altrimenti devo crearlo
  		  schema match {
  		    case Some(s) => Some(s)
  		    case _ => {
  		      //Use infoDataIn to improve the creation of the new schema
  		      //Se non trovo il convSchema, ne creo uno nuovo sulla base delle informazioni estratte direttamente dal dataset. Taggo il dataset come "diverso"
  		      //TODO Implement autonomous injestion + convSchema search based on dataset in input
  		      println("Creation of new convSchema not yet implemented")
  		      None
  		    }
  		  }
  		}
  		// 4. NameDataset + dataPath + Owner --> needs to change the dataPath
  		case ((Some(nameDatasetIn), None, Some(dataPathIn), Some(ownerIn), None)) => {
    		val infoQuery = Map("name_dataset" -> nameDatasetIn, "owner"-> ownerIn, "other_src.url" ->dataPathIn)
  		  retrieveSchema(infoQuery)
  		}
  	}
  }
  
  private def findQueryBuilder(infoQuery: Map[String, String]) = {
    val args = infoQuery.filterKeys(_ != "other_src.url").map {
      case (key, value) => equal(key, value)
    }
    and(args.toSeq:_*)
  }
  
  private def retrieveSchema(infoQuery: Map[String, String]): Option[ConvSchema] = {
    //val codecRegistry = fromRegistries(fromProviders(classOf[ConvSchema]), DEFAULT_CODEC_REGISTRY)
    //TODO complete the following code with query mechanism using the map
    println(infoQuery)
    try {
      val mongo: MongoMgmt = new MongoMgmt()
      val db = mongo.getDatabase("daf")
      //.withCodecRegistry(codecRegistry)
      val collection: MongoCollection[Document] = db.getCollection("convSchema")
      //val res = collection.find(and(equal("owner", "Torino"), equal("name_dataset", "GTFS_agency"))).first().results()(0)
      val res = collection.find(findQueryBuilder(infoQuery)).first().results()(0)
      val json: JsValue = Json.parse(res.toJson)
      

      println(json)
      
      val srcVal:Map[String, String] = infoQuery.get("other_src.url") match {
        case Some(s) => {
          println(s)
          println(getSrc(json, "src").getOrElse(Map()) - "url" + ("url" -> s))
          getSrc(json, "src").getOrElse(Map()) - "url" + ("url" -> s)
        }
        case _ => getSrc(json, "src").getOrElse(Map())
      }
      val convSchema = ConvSchema(
          name = getString(json, "name").getOrElse(""),
          nameDataset = getString(json, "name_dataset", normalize=true).getOrElse(""),
          cat = getCat(json, "cat"),
          groupOwn = getGroupOwn(json, "group_own"),
          owner = getString(json, "owner").getOrElse(""),
          src = srcVal,
          stdSchemaName = getString(json, "std_schema"),
          reqFields = getReqFields(json, "fields_conv"),
          custFields = getFields(json, "fields_custom")
          
      )
  
      Some(convSchema)
    } catch {
      case unknown: Throwable => {
        println("Exception in ConvSchemaGetter! - " + unknown)
        None
      }
    }
    
  }
  def redoMap(mapIn: Map[String, String], changeFieldName: String, changeFieldValue: String): Map[String, String] ={
    mapIn - "url" + (changeFieldName -> changeFieldValue)
  }
  
  def getReqFields(json: JsValue, name: String): List[Map[String, String]]  = {
    val fromJson = (json \ name).validate[List[Map[String, String]]]
    
    val listField: Option[List[Map[String, String]]] = fromJson match {
      case e: JsError => {
        println("Error in SchemaMgmt --> listReqField: " + JsError.toJson(e).toString())
        None
      }
      
      //Check if JsSuccess and if the required fields are defined
      case s: JsSuccess[List[Map[String, String]]] if (s.get.map{
          x => x.contains("field_std") & x.contains("formula")
        }.forall(x => x==true)) => Some(s.get)
      
      //None in all other cases
      case _ => { println("getConvSchema - getReqFields - errore: " + name)
        None
      }
    }
    
    listField match {
      case Some(s) => s
      case _ =>{
        println("errore")
        List()
      }
    }
  }
  
  
}