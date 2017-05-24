package it.teamDigitale.daf.schemamanager

import it.teamDigitale.daf.datamanagers.examples.StdSchemaGetter
import it.teamDigitale.daf.datastructures.Model.DatasetSchema
import it.teamDigitale.daf.datastructures.{ConvSchema, StdSchema}
import org.apache.logging.log4j.scala.Logging

/**
  * Verifica che un dataset sia ordinary, standard, o raw
  * @param convSchema metadatazione dello schema
  * @param inputSchema None se non vengono aggiunti i dati, Some se vengono aggiunti i dati e in quel caso bisogna confrontare convSchemaIn.dataSchema con inputSchema.get
  */
//class SchemaMgmt(convSchemaIn: ConvSchema, inputSchema: Option[DataSchema]= None)  extends Serializable with Logging {
@Deprecated
class SchemaMgmt(val convSchema: ConvSchema, inputSchema: Option[DatasetSchema]= None)  extends Serializable with Logging {

  val stdSchema: Option[StdSchema]  = extractSchema(convSchema.stdSchemaUri)

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

  def extractSchema(uri: Option[String]): Option[StdSchema]= {
    uri match {
      case Some(s) =>
        val schema: Option[StdSchema] = new StdSchemaGetter(s).getSchema()
        schema

      case _ =>
        None
    }
  }


    //  /*
    //   * Private function that does the checks wrt the coherence of the Conv Schema and the Std Schema
    //   */
    //  //TODO A Check on the Type needs to be implemented! verifySchema() to be changed and here input to be better defined
    //  private def stdSchemaCheck(): (Boolean, Boolean) = {
    //    (stdSchema, convSchema.reqFields) match {
    //      //Given an associated StdSchema exists, it performs the checks related to the StdSchema
    //      case (Some(stdSchemaIn), Some(reqFieldsIn)) => {
    //        //Check if the declared schema for data input is compliant with the SdtSchema
    //        val stdFields = stdSchemaIn.dataSchema.fields.map(x => (x.name, (x.metadata \ "required").as[Int]))
    //        val inStdFields = reqFieldsIn.map(x => x("field_std"))
    //        (true, verifySchema(inStdFields, stdFields))
    //      }
    //      case _ => (false, false)
    //    }
    //  }

    /*
    * Private function that does the checks wrt the coherence of the Conv Schema and the Std Schema
    */
    //TODO A Check on the Type needs to be implemented! verifySchema() to be changed and here input to be better defined
    private def stdSchemaCheck(): (Boolean, Boolean) = {
      val stdFields: Seq[(String, Int)] = stdSchema match {
        case Some(schema) => schema.dataSchema.fields.map(x => (x.name, x.metadata.required.asInstanceOf[Int]))
        case None => Seq()
      }
      val inStdFields = convSchema.reqFields.map(x => x.field_std)

      val stdFieldsNonEmpty = stdFields.nonEmpty
      val inStdFieldsNonEmpty = convSchema.reqFields.nonEmpty && verifySchema(inStdFields, stdFields)

      (stdFieldsNonEmpty, inStdFieldsNonEmpty)
    }


    /*
     * Private function that does the checks wrt the coherence of the input data Schema and the Conv Schema
     */
    //TODO A Check on the Type needs to be implemented! verifySchema() to be changed and here input to be better defined
    private def inputSchemaCheck(): (Boolean, Boolean) = {
      val inputSchemaNonEmpty = inputSchema.nonEmpty
      val refSchema = convSchema.dataSchema.fields.map(x => (x.name, x.metadata.required.asInstanceOf[Int]))

      val inSchema = inputSchema match {
        case Some(schema) =>
         schema.fields.map(x => x.name)
        case None => Seq()
      }

      val inputSchemaNonEmptyANDverifySchema  = inSchema.nonEmpty && verifySchema(inSchema, refSchema)

      (inputSchemaNonEmpty, inputSchemaNonEmptyANDverifySchema)
    }





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