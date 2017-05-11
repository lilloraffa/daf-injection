package it.teamDigitale.daf.schema.schemaMgmt

import org.apache.spark.sql.types._

object FieldTypeMgmt {
  def convAvro2Spark(typeCol: String): DataType = {
    
	  val isStruct = typeCol.contains("{")
	  
	  val typeAdj: String = typeCol.toLowerCase() match {
	    case x if x.startsWith("{") => "struct"
	    case x if x.startsWith("[") => {
	      val listType = x.replace("[", "")
	                       .replace("]", "")
	                       .replace("\"", "")
	                       .split(",")
	                       .map(_.trim)
	                       .toList
	      val isNullable = listType.contains("null")
	      listType.filter(x => !x.toLowerCase().equals("null"))(0)
	    }
	    case x => x
	  }
	  
	  
	  typeAdj.replace("\"", "") match {
	    case "string" => StringType
	    case "integer" => IntegerType
	    case "double" => DoubleType
	    case "float" => FloatType
	    case "boolean" => BooleanType
	    case "struct" => StringType  //Workaround for now
	    case x => {
	      println("problem: " + x)
	      StringType
	    }
	  }
	}
}