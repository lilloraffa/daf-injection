package it.teamDigitale.daf

import it.gov.daf.catalogmanagerclient.model.MetaCatalog
import it.teamDigitale.daf.datastructures.Model.DatasetType
import it.teamDigitale.daf.datastructures.uri.UriDataset

import scala.util.Try

/**
  * Created by fabiana on 29/05/17.
  */
package object datastructures {

  def convertToUriDatabase(schema: MetaCatalog) = Try {

    val typeDs = if (schema.operational.isStd)
      DatasetType.STANDARD
    else
      DatasetType.ORDINARY

    UriDataset(
      domain = "daf",
      typeDs = typeDs,
      groupOwn = schema.operational.groupOwn,
      owner = schema.dcatapit.dctRightsHolder._val,
      theme = schema.dcatapit.dcatTheme._val,
      nameDs = schema.dataschema.name
    )

  }

  def convertToConvSchema(schema: MetaCatalog) = Try{
    ConvSchema(
      logicalUri = Option(schema.operational.logicalUri),
      name = schema.dcatapit.dctTitle._val,
      isStd = schema.operational.isStd ,
      theme = schema.dcatapit.dcatTheme._val,
      cat = schema.dcatapit.dctSubject.map(x => x._val),
      groupOwn = schema.operational.groupOwn,
      owner = schema.dcatapit.dctRightsHolder._val,
      src = schema.operational.inputSrc,
      dataSchema = schema.dataschema,
      stdSchemaUri = Option(schema.operational.stdSchema.stdUri),
      reqFields = schema.operational.stdSchema.fieldsConv,
      custFields = Seq() // TODO da togliere o popolare
    )
  }

  def convertToStdSchema(schema: MetaCatalog) = Try{

    if(schema.operational.logicalUri.size < 1) throw new RuntimeException("No uri associated to this schema")

    StdSchema(
      name = schema.dcatapit.dctTitle._val,
      nameDataset = schema.dataschema.name,
      uri = schema.operational.logicalUri,
      theme = schema.dcatapit.dcatTheme._val,
      cat = schema.dcatapit.dctSubject.map(x => x._val),
      groupOwn =  schema.operational.groupOwn,
      owner = schema.dcatapit.dctRightsHolder._val,
      dataSchema = schema.dataschema
    )
  }


}
