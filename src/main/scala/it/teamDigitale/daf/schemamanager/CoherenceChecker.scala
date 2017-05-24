package it.teamDigitale.daf.schemamanager

import it.teamDigitale.daf.datastructures.Model.{DatasetSchema, Schema}
import it.teamDigitale.daf.datastructures.{ConvSchema, StdSchema}

/**
  * Created by fabiana on 19/05/17.
  */
object CoherenceChecker {



  /**
    * Check the coherence between a convSchema and a stdSchema
    * In particuar, for each required filed in the standard schema check whther the field is contained into the convSchema
    * @param convSchema
    * @param stdSchema
    */
  def checkCoherenceSchemas(convSchema: ConvSchema, stdSchema: StdSchema): Boolean = {
    val reqStdFields: Seq[String] = stdSchema.dataSchema
      .fields
      .filter(_.metadata.required == 1D)
      .map(x => x.name)

    val convFields = convSchema.reqFields.map(x => x.field_std).toSet

    reqStdFields.forall(x => convFields.contains(x))
  }

  /**
    * This check verifies the coherence between the datasetSchema and the schema stored within the CatalogManager
    * Not yet implemented
    * @param datasetSchema
    * @param schema
    * @return
    */
  def checkCoherenceDataSchema(datasetSchema: DatasetSchema, schema: Schema): Boolean = true



}
