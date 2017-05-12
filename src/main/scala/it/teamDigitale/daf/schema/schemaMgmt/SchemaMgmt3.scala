package it.teamDigitale.daf.schema.schemaMgmt

import play.api.libs.json._
import org.apache.spark.sql.functions._
import it.teamDigitale.daf.schema.{ConvSchema, StdSchema, DataSchema, DataSchemaField}
//import it.teamDigitale.daf.schema.schemaMgmt.StdSchemaGetter
import play.api.libs.json.JsValue.jsValueToJsLookup
import org.apache.logging.log4j.scala.Logging
import org.apache.logging.log4j.Level

class SchemaMgmt3(convSchemaIn: ConvSchema, inputSchema: Option[DataSchema]=None)  extends Serializable with Logging{
  
  val convSchema = convSchemaIn
  
  val stdSchema: Option[StdSchema] = convSchema.stdSchemaUri match {
    case Some(s) => {
      val schema: Option[StdSchema] = (new StdSchemaGetter(s)).getSchema()
      //this check is required to check if the declared StdSchema is actually found.
      schema match {
        case Some(s1: StdSchema) => Some(s1)
        case _ => {
          logger.error("Error: StdSchema declared but not found. StdSchema name: " + s)
          //sys.exit(1)
          None
        }
      }
    }
    case _ => {
      None
    }
    
    
  }
  
  val schemaReport = getSchemaReport()
  
  def getSchemaReport(): SchemaReport = {

    val (hasStdSchemaIn, checkStdSchemaIn): (Boolean, Boolean) = stdSchemaCheck()
    val (hasInputDataSchemaIn, checkInSchemaIn): (Boolean, Boolean) = inputSchemaCheck()
    
    SchemaReport (
        hasInputDataSchema = hasInputDataSchemaIn,
        hasStdSchema = hasStdSchemaIn,
        checkStdSchema = checkStdSchemaIn,  //Given StdSchema, it checks if the mapping is done correctly
        checkInSchema = true  //Checks that the input data is coherent with the declared schema
    )
    
  }
  
  /*
   * Private function that does the checks wrt the coherence of the Conv Schema and the Std Schema
   */
  //TODO A Check on the Type needs to be implemented! verifySchema() to be changed and here input to be better defined
  private def stdSchemaCheck(): (Boolean, Boolean) = {
    (stdSchema, convSchema.reqFields) match {
      //Given an associated StdSchema exists, it performs the checks related to the StdSchema
      case (Some(stdSchemaIn), Some(reqFieldsIn)) => {
        //Check if the declared schema for data input is compliant with the SdtSchema
        val stdFields = stdSchemaIn.dataSchema.fields.map(x => (x.name, (x.metadata \ "required").as[Int]))
        val inStdFields = reqFieldsIn.map(x => x("field_std"))
        (true, verifySchema(inStdFields, stdFields))
      }
      case _ => (false, false)
    }
  }
  
  /*
   * Private function that does the checks wrt the coherence of the input data Schema and the Conv Schema
   */
  //TODO A Check on the Type needs to be implemented! verifySchema() to be changed and here input to be better defined
  private def inputSchemaCheck(): (Boolean, Boolean) = {
    (inputSchema) match {
      case Some(inputSchemaIn) => {
        //Check if the declared schema for data input is compliant with the SdtSchema
        val refSchema = convSchemaIn.dataSchema.fields.map(x => (x.name, (x.metadata \ "required").as[Int]))
        val inSchema = inputSchemaIn.fields.map(x => x.name)
        (true, verifySchema(inSchema, refSchema))
      }
      case _ => (false, false)
    }
  }
  
  
  
  
  
  /*
  //TODO this should be eliminated
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
	*/
  /*
   * Function that checks if a list of fields in input complies with a defined schema.
   * It can be applied to check if a ConvSchema complies with the associated StdSchema,
   * and if the fields defined in the input file actually comply with the defined Schema.
   */
  //TODO A Check on the Type needs to be implemented! verifySchema()
  def verifySchema(fieldList: Seq[String], stdFields: Seq[(String, Int)]): Boolean ={
    
    val testStdField: Boolean = stdFields.filter(x => x._2==1).map{
      //Check if the required StdSchema fields are contained in the Conv.
      x => {
        val fieldName = x._1
        val test: Boolean = fieldList.contains(fieldName)
        if(!test) println("ERROR - Check Required Fields: missing "+ fieldName)
        test
      }
    }.forall(x => x==true) & fieldList.map{
      //Check if the fields that belongs to a StdSchema contained in Conv, are all defined in the StdSchema
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