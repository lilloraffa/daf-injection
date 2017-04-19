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


class StdSchemaGetter(schemaName: String) extends SchemaGetter[StdSchema]{
  
  def getSchema() = {
    
    //val codecRegistry = fromRegistries(fromProviders(classOf[ConvSchema]), DEFAULT_CODEC_REGISTRY)
    try{
      val mongo: MongoMgmt = new MongoMgmt()
      val db = mongo.getDatabase("daf")
      //.withCodecRegistry(codecRegistry)
      val collection: MongoCollection[Document] = db.getCollection("stdSchema")
      
      
      
      val res = collection.find(equal("std_schema", schemaName)).first().results()(0)
      val json: JsValue = Json.parse(res.toJson)
      println(json)
      
      val stdSchema = StdSchema (
          name = getString(json, "name").getOrElse(""),
          stdSchemaName = getString(json, "std_schema").getOrElse(""),
          //nameDataset = getString(json, "name_dataset", normalize=true).getOrElse(""),
          cat = getCat(json, "cat").getOrElse(Seq()),
          groupOwn = getGroupOwn(json, "group_own"),
          owner = getString(json, "owner").getOrElse(""),
          src = getSrc(json, "src").getOrElse(Map()),
          fields = getFields(json, "fields").getOrElse(JsArray())
      )
      
      Some(stdSchema)
    } catch {
      case _: Throwable => {
        println("Exception in ConvSchemaGetter!")
        None
      }
    }
    
  }

}