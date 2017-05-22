import it.teamDigitale.daf.datamanagers.examples.{ConvSchemaGetter, StdSchemaGetter}
import it.teamDigitale.daf.schema.schemaMgmt.SchemaMgmt
import it.teamDigitale.daf.injestion.DataInjCsv
import org.apache.logging.log4j.scala.Logging

object TestSchemaMgmt extends App with Logging {



  val convSchema = ConvSchemaGetter.getSchema()

  println(convSchema)

  val stdSchema = new StdSchemaGetter("daf://dataset/vid/mobility/gtfs_agency").getSchema()
  println(stdSchema)

  convSchema match {
    case Some(s) =>
      val dataInj = new DataInjCsv(new SchemaMgmt(s))
      println(dataInj.doInj())
    case _ => println("ERROR")
  }

}