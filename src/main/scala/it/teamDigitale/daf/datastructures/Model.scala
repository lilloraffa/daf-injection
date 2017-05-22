package it.teamDigitale.daf.datastructures

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
    def convertToConvSchema() = {
      ConvSchema(
        uri = operational.uri,
        name = dcatapit.`dct:title`.`val`,
        isStd = operational.is_std,
        theme = dcatapit.`dcat:theme`.`val`,
        cat = dcatapit.`dct:subject`.map(x => x.`val`),
        groupOwn = operational.group_own,
        owner = dcatapit.`dct:rightsHolder`.`val`,
        src = operational.input_src,
        dataSchema = dataschema,
        stdSchemaUri = Option(operational.std_schema.std_uri),
        reqFields = operational.std_schema.fields_conv,
        custFields = Seq() // TODO da togliere o popolare
      )
    }

    def convertToStdSchema() = {

      StdSchema(
        name = dcatapit.`dct:title`.`val`,
        nameDataset = dataschema.name,
        uri = operational.uri.getOrElse(throw new RuntimeException("No uri associated to this schema")),
        theme = dcatapit.`dcat:theme`.`val`,
        cat = dcatapit.`dct:subject`.map(x => x.`val`),
        groupOwn = operational.group_own,
        owner = dcatapit.`dct:rightsHolder`.`val`,
        dataSchema = dataschema
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
                       `val`: String
                     )
  case class DcatTheme(
                        `@id`: String,
                        `val`: String
                      )

  /**
    * Contains dcatapit info about a dataset
    * Automatically generated from /resources/dataschema/data-dcatapit
    * @param `dct:identifier`
    * @param `dct:title`
    * @param `dct:description`
    * @param `dcat:theme`
    * @param `dct:rightsHolder`
    * @param `dct:accrualPeriodicity`
    * @param `dct:subject`
    * @param `dct:language`
    * @param `dcat:keyword`
    * @param `dcat:spatial`
    */
  case class DcatapitInfo(
                             `dct:identifier`: String,
                             `dct:title`: DctTitle,
                             `dct:description`: DctTitle,
                             `dcat:theme`: DctTitle,
                             `dct:rightsHolder`: DctTitle,
                             `dct:accrualPeriodicity`: DctTitle,
                             `dct:subject`: List[DctTitle],
                             `dct:language`: DctTitle,
                             `dcat:keyword`: List[String],
                             `dcat:spatial`: String
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
    * @param location
    * @param input_src
    */
  case class OperationalInfo(
                             uri: Option[String],
                             is_std: Boolean = false,
                             group_own: String,
                             std_schema: Std_schema,
                             read_type: String,
                             location: List[Location],
                             input_src: Input_src
                           )



}


