package it.teamDigitale.daf.utils

object TxtFile {
  
  def firstLine(filePath: String): Option[String] = {
    val src = scala.io.Source.fromFile(filePath)
    try {
      src.getLines.find(_ => true)
    } finally {
      src.close()
    }
  }

  //Get separator of a csv file
  def csvGetSep(firstLine: String): String = {
	  val sepList = List(';', ',', '|')
	  //find the right separator for the file
		val sep = sepList{
		  sepList.map{
			  x => firstLine.count(_ == x)
		  }.zipWithIndex.maxBy(_._1)._2
	  }.toString
	  
	  sep
  }
 
}