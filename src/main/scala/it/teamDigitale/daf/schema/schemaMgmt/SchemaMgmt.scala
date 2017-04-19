package it.teamDigitale.daf.schema.schemaMgmt

import play.api.libs.json._
import org.apache.spark.sql.functions._
import it.teamDigitale.daf.schema.ConvSchema
import it.teamDigitale.daf.schema.StdSchema
//import it.teamDigitale.daf.schema.schemaMgmt.StdSchemaGetter
import play.api.libs.json.JsValue.jsValueToJsLookup

class SchemaMgmt(convSchemaIn: ConvSchema )  extends Serializable {
  

  
  case class FinalSchema (
      name: String,
      nameDataset: String,
      cat: Seq[String],
      groupOwn: String,
      owner: String,
      src: Map[String, String],
      stdSchemaName: Option[String],
      fields: Option[JsArray],  //Put as JsArray for now
      reqStdFields: List[Map[String, String]],
      optStdFields: List[Map[String, String]]
  )
  
  val convSchema = convSchemaIn
  
  val stdSchema: Option[StdSchema] = convSchema.stdSchemaName match {
    case Some(s) => {
      val schema: StdSchema = (new StdSchemaGetter(s)).getSchema() match {
        case Some(s1: StdSchema) => s1
        case _ => {
          println("Error - Exit...")
          sys.exit(1)
        }
      }
      Some(schema)
    }
    case _ => {
      None
    }
    
    
  }
  
  val finalSchema = getFinalSchema()

  def getFinalSchema(): FinalSchema = {

    val (name, nameDataset, cat, groupOwn, stdSchemaName, src, reqFields, reqStdConv, optStdConv): (String, String, Seq[String], String, Option[String], Map[String,String], Option[JsArray], List[Map[String, String]], List[Map[String, String]]) = stdSchema match {
      case Some(stdSchema) => {
        
        val (testSchema: Boolean, reqStdConvIn: List[Map[String, String]], optStdConvIn: List[Map[String, String]]) = verGetSchema(stdSchema, convSchemaIn.reqFields)
        println("Test schema: " + testSchema)
        if(testSchema){
          val inStdFields = convSchema.reqFields.map(x => x("field_std"))
          ( 
              convSchema.name,
              convSchema.nameDataset,
              stdSchema.cat,
              stdSchema.groupOwn,
              convSchema.stdSchemaName,
              convSchema.src,
              Some(JsArray(stdSchema.fields.value.filter(x=>inStdFields.contains((x \ "name").as[String])))),
              reqStdConvIn,
              optStdConvIn
          )
              
        } else {
          ( 
              convSchema.name,
              convSchema.nameDataset,
              //convSchema.cat.getOrElse(Seq("other")),
              stdSchema.cat,
              convSchema.groupOwn,
              Some(stdSchema.stdSchemaName),
              convSchema.src,
              None,
              reqStdConvIn,
              optStdConvIn
          )
        }
      }
      case _ => {
        ( 
            convSchema.name,
            convSchema.nameDataset,
            convSchema.cat.getOrElse(Seq("other")),
            convSchema.groupOwn,
            None,
            convSchema.src,
            None,
            List(),
            List()
        )
      }
      
    }
   
    
    //if standard schema test passed, then look for custom fields
    val finalFields = (reqFields, convSchema.custFields) match {
      case (Some(sReqFields), Some(sCustFields)) => Some(sReqFields ++ sCustFields)
      case (None, Some(sCustFields)) => Some(sCustFields)
      case (Some(sReqFields), None) => Some(sReqFields)
      case _ => None
    }
    
    FinalSchema (
      name = name,
      nameDataset = nameDataset,
      cat = cat,
      groupOwn = groupOwn,
      owner = convSchema.owner,
      src = src,
      stdSchemaName = stdSchemaName,
      fields = finalFields,
      reqStdFields = reqStdConv,
      optStdFields = optStdConv
      
    )
  }
  
  
  
  def verGetSchema(stdSchema: StdSchema, convReqFields: List[Map[String, String]]): (Boolean, List[Map[String, String]], List[Map[String, String]]) = {
    
    val stdFields = stdSchema.fields.value.map(x => ((x \ "name").as[String], (x \ "required").as[Int]))
    
    val inStdFields = convReqFields.map(x => x("field_std"))
    val reqStdFields = stdFields.filter(x => x._2==1).map(x => x._1)
    val optStdFields = stdFields.filter(x => x._2==0).map(x => x._1)

    val reqStdConvSchema = convReqFields.filter(x => reqStdFields.contains(x("field_std")))
    val optStdConvSchema = convReqFields.filter(x => optStdFields.contains(x("field_std")))

    val testStdField = verifySchema(inStdFields, stdFields)

    (testStdField, reqStdConvSchema, optStdConvSchema)
  }

  def verifySchema(fieldList: Seq[String], stdFields: Seq[(String, Int)]): Boolean ={
    
    val testStdField: Boolean = stdFields.filter(x => x._2==1).map{
      x => {
        val fieldName = x._1
        val test: Boolean = fieldList.contains(fieldName)
        if(!test) println("ERROR - Check Required Fields: missing "+ fieldName)
        test
      }
    }.forall(x => x==true) & fieldList.map{
      x => {
        val test: Boolean = stdFields.map(x=> x._1).contains(x)
        if(!test) println("ERROR - Check Input Fields not found in Std Fields: "+ x)
        test
      }
    }.forall(x => x==true)
    testStdField
  }
  
  
  //TODO def Schema Retreiver Function
}