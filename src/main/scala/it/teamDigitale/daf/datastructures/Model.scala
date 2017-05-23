package it.teamDigitale.daf.datastructures

import it.teamDigitale.daf.datastructures.uri.UriDataset

import scala.util.Try

/**
  * Created by fabiana on 15/05/17.
  */
object Model {

  case class Schema(dataschema: DatasetSchema, dcatapit: DcatapitInfo, operational: OperationalInfo) {
    //case class Schema(ds: DatasetSchema, dcatapit: DcatapitInfo, operational: OperationalInfo) {
    /**
      * Used to semplify the access to the most usefull data
      * TODO it should be removed if it is not usefull
      * @return
      */
    def convertToConvSchema() = Try{
      ConvSchema(
        uri = operational.uri,
        name = dcatapit.dct_title.`val`.getOrElse("ERROR"),
        isStd = operational.is_std,
        theme = dcatapit.dcat_theme.`val`.getOrElse("ERROR"),
        cat = dcatapit.dct_subject.map(x => x.`val`.getOrElse("ERROR")),
        groupOwn = operational.group_own,
        owner = dcatapit.dct_rightsHolder.`val`.getOrElse("ERROR"),
        src = operational.input_src,
        dataSchema = dataschema,
        stdSchemaUri = Option(operational.std_schema.get.std_uri),
        reqFields = operational.std_schema.get.fields_conv,
        custFields = Seq() // TODO da togliere o popolare
      )
    }

    def convertToStdSchema() = Try{

      StdSchema(
        name = dcatapit.dct_title.`val`.getOrElse("ERROR"),
        nameDataset = dataschema.name,
        uri = operational.uri.getOrElse(throw new RuntimeException("No uri associated to this schema")),
        theme = dcatapit.dcat_theme.`val`.getOrElse("ERROR"),
        cat = dcatapit.dct_subject.map(x => x.`val`.getOrElse("ERROR")),
        groupOwn = operational.group_own,
        owner = dcatapit.dct_rightsHolder.`val`.getOrElse("ERROR"),
        dataSchema = dataschema
      )
    }

    def convertToUriDataset() = Try {

      val typeDs = if (operational.is_std)
        DatasetType.STANDARD
      else
        DatasetType.ORDINARY

      UriDataset(
        domain = "daf",
        typeDs = typeDs,
        groupOwn = operational.group_own,
        owner = dcatapit.dct_rightsHolder.`val`.getOrElse("NO_OWNER"),
        theme = dcatapit.dcat_theme.`val`.getOrElse("NO_THEME"),
        nameDs = dataschema.name
      )

    }

  }

  case class Semantics(`@id`: String, `@context`: List[String])
  case class Metadata(
                       required: Double = 1D,
                       desc: String,
                       field_type: String,
                       cat: String,
                       tag: List[String],
                       semantics: Semantics
                     )
  case class Fields(
                     name: String,
                     `type`: String,
                     doc: String,
                     metadata: Metadata
                   )

  /**
    * Contains info about the dataset to inject
    * Automatically generated from /resources/dataschema/data-dataschema
    * @param namespace
    * @param `type`
    * @param name
    * @param aliases
    * @param fields
    */
  case class DatasetSchema(
                             namespace: String,
                             `type`: String,
                             name: String,
                             aliases: List[String],
                             fields: List[Fields]
                           )


  case class DctTitle(
                        it: Option[String]= None,
                       `val`: Option[String] = None
                     )
  case class DcatTheme(
                        `@id`: String,
                        `val`: String
                      )

  /**
    * Contains dcatapit info about a dataset
    * Automatically generated from /resources/dataschema/data-dcatapit
   */
  case class DcatapitInfo(
                             dct_identifier: String,
                             dct_title: DctTitle,
                             dct_description: DctTitle,
                             dcat_theme: DctTitle,
                             dct_rightsHolder: DctTitle,
                             dct_accrualPeriodicity: DctTitle,
                             dct_subject: List[DctTitle],
                             dct_language: DctTitle,
                             dcat_keyword: List[String],
                             dcat_spatial: String
                           )


  case class Fields_conv(
                          field_std: String,
                          formula: String
                        )
  case class Std_schema(
                         std_uri: String,
                         fields_conv: List[Fields_conv],
                         field_custom: List[String]
                       )
  case class Location(
                     lat: Double,
                     lon: Double
                   )

  case class Input_src(
                      /*
                      TODO non riesce ad analizzare un valore di default come "unknow", bisognerebbe mettere option?!?
                       */
                        name: String,
                        ing_type: String,
                        src_type: String,
                        url: String = ""
                      )



  /**
    * Contains operational information
    * @param group_own
    * @param std_schema
    * @param read_type
    * @param georef
    * @param input_src
    */
  case class OperationalInfo(
                             uri: Option[String],
                             is_std: Boolean = false,
                             group_own: String,
                             std_schema: Option[Std_schema],
                             read_type: String,
                             georef: List[Location]= List(),
                             input_src: Input_src
                           )


  object DatasetType extends Enumeration {
    val STANDARD = Value("std")
    val ORDINARY = Value("ord")
    val RAW = Value("raw")

    def withNameOpt(s: String): Option[Value] = values.find(_.toString == s)
  }
}


