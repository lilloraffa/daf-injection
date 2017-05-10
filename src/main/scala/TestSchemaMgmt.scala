import it.teamDigitale.daf.schema.schemaMgmt.{ConvSchemaGetter, StdSchemaGetter, MetadataLU}
import it.teamDigitale.daf.schema.schemaMgmt.SchemaMgmt
import java.io.FileInputStream
import play.api.libs.json._
import scala.annotation.tailrec
import it.teamDigitale.daf.utils.JsonMgmt
import org.apache.logging.log4j.scala.Logging
import org.apache.logging.log4j.Level
import com.typesafe.config.ConfigFactory
import scala.util.{Try, Success, Failure}

object TestSchemaMgmt extends App with Logging {
  val file_operational: String = "/Users/lilloraffa/Development/teamdgt/daf/datamgmt_v2/example/data-operational.json"
  val file_dcatap: String = "/Users/lilloraffa/Development/teamdgt/daf/datamgmt_v2/example/data-dcatapit.json"
  val file_dataschema: String = "/Users/lilloraffa/Development/teamdgt/daf/datamgmt_v2/example/data-dataschema.json"
  
  val stream_operational = new FileInputStream(file_operational)
  val json_operational = try {  Json.parse(stream_operational) } finally { stream_operational.close() }
  
  val stream_dcatap = new FileInputStream(file_dcatap)
  val json_dcatap = try {  Json.parse(stream_dcatap) } finally { stream_dcatap.close() }
  
  val stream_dataschema = new FileInputStream(file_dataschema)
  val json_dataschema = try {  Json.parse(stream_dataschema) } finally { stream_dataschema.close() }
  
  val json: JsValue = Json.obj(
      "ops" -> json_operational,
      "dataschema" -> json_dataschema,
      "dcatap" -> json_dcatap
  )
  
  
  
  val fields = Seq("dcatap", "dct:title", "val")
  
  
  
  
  val convSchema = (new ConvSchemaGetter(
      metadataJson = Some(json)
      )
  ).getSchema()
  
  println(convSchema)
  
  val stdSchema = (new StdSchemaGetter("daf://dataset/vid/mobility/gtfs_agency")).getSchema()
  println(stdSchema)
  
  convSchema match {
    case Some(s) => {
      val schemaMgmt = new SchemaMgmt(s)
      println(schemaMgmt.schemaReport)
    }
    case _ => println("ERROR")
  }

}