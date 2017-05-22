import com.typesafe.config.ConfigFactory
import it.teamDigitale.daf.datamanagers.DataManager
import it.teamDigitale.daf.schema.schemaMgmt.CoherenceChecker

import scala.util.Success

/**
  * Created by fabiana on 22/05/17.
  */
object TestWorkflow extends App{

  val uri = ConfigFactory.load().getString("WebServices.catalogUrl")

  val dm = new DataManager(uri)
  val tryschema = dm.getSchemaFromUri("1")

  tryschema match {
    case Success(schema) =>
      val convSchema = schema.convertToConvSchema()
      println(convSchema)

      val stdSchema = schema.convertToStdSchema()

      val isCoherence = CoherenceChecker.checkCoherenceSchemas(convSchema, stdSchema)
      println(isCoherence)

      //if isCoherence is true you can start with the storing process
  }
}
