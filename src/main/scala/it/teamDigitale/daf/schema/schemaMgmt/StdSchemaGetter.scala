package it.teamDigitale.daf.schema.schemaMgmt

import play.api.libs.json._
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.bson.codecs.Macros._
import it.teamDigitale.daf.utils.MongoMgmt
import it.teamDigitale.daf.utils.HelperMongo._
import it.teamDigitale.daf.schema.StdSchema
import it.teamDigitale.daf.utils.JsonMgmt


class StdSchemaGetter(schemaName: String) extends SchemaGetter[StdSchema]{
  def getSchema() = {
    
    //look for standard schema a StdSchema, by calling a given service.
    //TODO - All the following are temporary and will be deleted once the API is ready
    val file_operational: String = "/Users/lilloraffa/Development/teamdgt/daf/datamgmt_v2/example/stdSchema/std-operational.json"
    val file_dcatap: String = "/Users/lilloraffa/Development/teamdgt/daf/datamgmt_v2/example/stdSchema/std-dcatapit.json"
    val file_dataschema: String = "/Users/lilloraffa/Development/teamdgt/daf/datamgmt_v2/example/stdSchema/std-dataschema.json"
    
    val json_operational = JsonMgmt.getJson(file_operational)
    val json_dcatap = JsonMgmt.getJson(file_dcatap)
    val json_dataschema = JsonMgmt.getJson(file_dataschema)
    
    val json: JsValue = Json.obj(
        "ops" -> json_operational,
        "dataschema" -> json_dataschema,
        "dcatap" -> json_dcatap
    )
    
    //Put a check in case the read is not good
    val stdSchema = StdSchema (
        name = getString(json, "name").getOrElse(""),
        nameDataset = getString(json, "dataset_name").getOrElse(""),
        uri = getString(json, "uri").getOrElse(""),
        stdSchemaName = "",
        theme = getString(json, "theme").getOrElse(""), 
        cat = getCat(json, "cat"),
        groupOwn = getString(json, "group_own").getOrElse("open"),
        owner = getString(json, "owner").getOrElse(""),
        //src = getMap(json, "input_src").getOrElse(Map()),
        //fields = getFields(json, "fields").getOrElse(JsArray()),
        dataSchema = getDataSchema(json, "data_schema")
    )
    
    Some(stdSchema)
    
  }
  
  
  def getSchemaMongo() = {
    
    //val codecRegistry = fromRegistries(fromProviders(classOf[ConvSchema]), DEFAULT_CODEC_REGISTRY)
    try{
      val mongo: MongoMgmt = new MongoMgmt()
      val db = mongo.getDatabase("daf")
      //.withCodecRegistry(codecRegistry)
      val collection: MongoCollection[Document] = db.getCollection("stdSchema")
      
      
      
      val res = collection.find(equal("std_schema", schemaName)).first().results()(0)
      val json: JsValue = Json.parse(res.toJson)
      println(json)
      /*
      val stdSchema = StdSchema (
          name = getString(json, "name").getOrElse(""),
          stdSchemaName = getString(json, "std_schema").getOrElse(""),
          uri = "",
          //nameDataset = getString(json, "name_dataset", normalize=true).getOrElse(""),
          cat = getCat(json, "cat").getOrElse(Seq()),
          groupOwn = getGroupOwn(json, "group_own"),
          owner = getString(json, "owner").getOrElse(""),
          src = getSrc(json, "src").getOrElse(Map()),
          fields = getFields(json, "fields").getOrElse(JsArray())
      )
      
      Some(stdSchema)
      * 
      */
      None
    } catch {
      case _: Throwable => {
        println("Exception in ConvSchemaGetter!")
        None
      }
    }
    
  }

}