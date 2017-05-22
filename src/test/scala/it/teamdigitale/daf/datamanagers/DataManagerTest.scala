package it.teamdigitale.daf.datamanagers

import com.typesafe.config.ConfigFactory
import it.teamDigitale.daf.datamanagers.DataManager
import org.apache.logging.log4j.scala.Logging
import org.mockito.stubbing.OngoingStubbing
import org.specs2.mock.Mockito
import org.specs2.mutable._

import scala.util.{Success, Try}
/**
  * Created by fabiana on 22/05/17.
  */
class DataManagerTest extends Specification with Mockito with Logging {

  val uri = ConfigFactory.load().getString("WebServices.catalogUrl")
  val idDataset = "1"
  val httpResponse = """
      |{
      |  "dataschema": {
      |    "namespace": "it.gov.daf.dataset.open.torino",
      |    "type": "record",
      |    "name": "GTFS Agency",
      |    "fields": [
      |      {
      |        "name": "agency_id",
      |        "type": "string",
      |        "doc": "",
      |        "metadata": {
      |          "semantics": {
      |            "id": null,
      |            "context": null
      |          },
      |          "desc": "Uniquely identifies a transit agency. A transit feed may represent data from more than one agency. The agency_id is dataset unique. This field is optional for transit feeds that only contain data for a single agency.",
      |          "tag": [
      |            "agenzia trasporto pubblico",
      |            "public transportation agency",
      |            "id"
      |          ],
      |          "field_type": "dimension",
      |          "cat": "mobility/transportation/agency/name"
      |        }
      |      },
      |      {
      |        "name": "agency_name",
      |        "type": "string",
      |        "doc": "",
      |        "metadata": {
      |          "semantics": {
      |            "id": null,
      |            "context": null
      |          },
      |          "desc": "Agency Name. The agency_name field contains the full name of the transit agency. Google Maps will display this name.",
      |          "tag": [
      |            "agenzia trasporto pubblico",
      |            "public transportation agency",
      |            "name"
      |          ],
      |          "field_type": "textval",
      |          "cat": "mobility/transportation/agency/name"
      |        }
      |      },
      |      {
      |        "name": "agency_url",
      |        "type": "string",
      |        "doc": "",
      |        "metadata": {
      |          "semantics": {
      |            "id": null,
      |            "context": null
      |          },
      |          "desc": "Agency URL. Contains the URL of the transit agency. The value must be a fully qualified URL that includes http:// or https://, and any special characters in the URL must be correctly escaped. See http://www.w3.org/Addressing/URL/4_URI_Recommentations.html for a description of how to create fully qualified URL values.",
      |          "tag": [
      |            "agenzia trasporto pubblico",
      |            "public transportation agency",
      |            "url"
      |          ],
      |          "field_type": "textval",
      |          "cat": "mobility/transportation/agency/info/"
      |        }
      |      },
      |      {
      |        "name": "agency_timezone",
      |        "type": "string",
      |        "doc": "",
      |        "metadata": {
      |          "semantics": {
      |            "id": "http://vocab.gtfs.org/terms#Agency_timezone",
      |            "context": [
      |              "http://vocab.gtfs.org/terms#Agency"
      |            ]
      |          },
      |          "desc": "Contains the timezone where the transit agency is located. Timezone names never contain the space character but may contain an underscore. Please refer to http://en.wikipedia.org/wiki/List_of_tz_zones for a list of valid values. If multiple agencies are specified in the feed, each must have the same agency_timezone.",
      |          "tag": [
      |            "agenzia trasporto pubblico",
      |            "public transportation agency",
      |            "timezone"
      |          ],
      |          "field_type": "textval",
      |          "cat": "mobility/transportation/agency/info/"
      |        }
      |      },
      |      {
      |        "name": "agency_lang",
      |        "type": "string",
      |        "doc": "",
      |        "metadata": {
      |          "semantics": {
      |            "id": "http://vocab.gtfs.org/terms#Agency_language",
      |            "context": [
      |              "http://vocab.gtfs.org/terms#Agency"
      |            ]
      |          },
      |          "desc": "Language. Contains a two-letter ISO 639-1 code for the primary language used by this transit agency. The language code is case-insensitive (both en and EN are accepted). This setting defines capitalization rules and other language-specific settings for all text contained in this transit agency's feed. Please refer to http://www.loc.gov/standards/iso639-2/php/code_list.php for a list of valid values.",
      |          "tag": [
      |            "agenzia trasporto pubblico",
      |            "public transportation agency",
      |            "language"
      |          ],
      |          "field_type": "textval",
      |          "cat": "mobility/transportation/agency/info/"
      |        }
      |      },
      |      {
      |        "name": "agency_phone",
      |        "type": "string",
      |        "doc": "",
      |        "metadata": {
      |          "semantics": {
      |            "id": "http://vocab.gtfs.org/terms#Agency_phone",
      |            "context": [
      |              "http://vocab.gtfs.org/terms#Agency"
      |            ]
      |          },
      |          "desc": "Contains a single voice telephone number for the specified agency. This field is a string value that presents the telephone number as typical for the agency's service area. It can and should contain punctuation marks to group the digits of the number. Dialable text (for example, TriMet's 503-238-RIDE) is permitted, but the field must not contain any other descriptive text.",
      |          "tag": [
      |            "agenzia trasporto pubblico",
      |            "public transportation agency",
      |            "phone"
      |          ],
      |          "field_type": "textval",
      |          "cat": "mobility/transportation/agency/info/"
      |        }
      |      }
      |    ]
      |  },
      |  "operational": {
      |    "georef": null,
      |    "uri": null,
      |    "read_type": "update",
      |    "std_schema": {
      |      "std_uri": "daf://dataset/vid/mobility/gtfs_agency",
      |      "fields_conv": [
      |        {
      |          "field_std": "agency_id",
      |          "formula": "agency_id"
      |        },
      |        {
      |          "field_std": "agency_name",
      |          "formula": "agency_name"
      |        },
      |        {
      |          "field_std": "agency_url",
      |          "formula": "agency_url"
      |        },
      |        {
      |          "field_std": "agency_timezone",
      |          "formula": "agency_timezone"
      |        },
      |        {
      |          "field_std": "agency_lang",
      |          "formula": "agency_lang"
      |        },
      |        {
      |          "field_std": "agency_phone",
      |          "formula": "agency_phone"
      |        }
      |      ]
      |    },
      |    "id_std": null,
      |    "group_own": "open"
      |  },
      |  "dcatapit": {
      |    "dct_title": {
      |      "it": null,
      |      "val": null
      |    },
      |    "dcat_keyword": [
      |      "gtfs",
      |      "public transportation",
      |      "agency",
      |      "trasporto pubblico",
      |      "aziende trasporto"
      |    ],
      |    "dct_subject": [
      |      {
      |        "id": "http://eurovoc.europa.eu/4512",
      |        "val": null,
      |        "it": null
      |      }
      |    ],
      |    "dcat_landingPage": null,
      |    "dct_identifier": "ABC1234",
      |    "dcat_spatial": "http://www.geonames.org/3165524",
      |    "dct_description": {
      |      "it": null,
      |      "val": null
      |    },
      |    "dct_language": {
      |      "id": "http://publications.europa.eu/resource/authority/language/ITA",
      |      "val": null
      |    },
      |    "dct_accrualPeriodicity": {
      |      "id": "http://publications.europa.eu/resource/authority/frequency/BIMONTHLY",
      |      "val": null,
      |      "it": null
      |    },
      |    "dcat_theme": {
      |      "id": "http://publications.europa.eu/resource/authority/data-theme/TRAN",
      |      "val": null,
      |      "it": null
      |    },
      |    "dct_rightsHolder": {
      |      "id": "http://www.comune.torino.it/",
      |      "val": null,
      |      "it": null
      |    }
      |  }
      |}
    """.stripMargin

//  "A CatalogService" should {
//    "be an example" in {
//      val mockedDataManager = mock[DataManager]
//
//      when(mockedDataManager.getDatafromHttp(idDataset)).thenReturn(Try(httpResponse))
//      val res = mockedDataManager.getSchemaFromUri(idDataset)
//      //assert(res.isSuccess)
//      val s = mockedDataManager.getDatafromHttp(idDataset)
//      val s2 = mockedDataManager.getSchemaFromJson(s.get)
//      println(s)
//    }
//  }
//
//  test("do real request") {
//    val dm = new DataManager(uri)
//    val schema = dm.getSchemaFromUri(idDataset)
//    assert(schema.isSuccess)
//  }

 "A DataManager" should  {
   "returns a right schema mocking the httpRequest to the catalog manager" in {
     val mockedDataManager = mock[DataManager]
     val pippo: OngoingStubbing[Try[String]] = mockedDataManager.getDatafromHttp(idDataset) returns Success(httpResponse)
     val s = mockedDataManager.getSchemaFromUri(idDataset)
     println(pippo)
     println(s)
     s.isSuccess must be equals true

   }
 }
}
