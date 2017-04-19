

//import org.apache.spark.SparkContext
//import org.apache.spark.SparkConf
import play.api.libs.json._
import java.io.FileInputStream

object Test extends App {
	println("Hello, world")

	val json: JsValue = Json.parse("""
			{
			"name" : "Watership Down",
			"location" : {
			"lat" : 51.235685,
			"long" : -1.309197
			},
			"residents" : [ {
			"name" : "Fiver",
			"age" : 4,
			"role" : null
			}, {
			"name" : "Bigwig",
			"age" : 6,
			"role" : "Owsla"
			} ]
			}
			""")
	val test = (json.validate[Map[String, JsValue]]).get
	println(((json \ "residents").get)(0))
	println((json \ "residents").as[Array[JsValue]])
	println((((json \ "residents").as[JsArray]).value(0) \ "name").get)
	println((json \ "name1").asOpt[String].getOrElse(-1))
	
	
	
	val filePath = "/Users/lilloraffa/Development/teamdgt/daf/dataschema/mobility/conv-gtfs_agency.json"
	val stream = new FileInputStream(filePath)
  val json2 = try {  Json.parse(stream) } finally { stream.close() }
  println(json2)
	
}
