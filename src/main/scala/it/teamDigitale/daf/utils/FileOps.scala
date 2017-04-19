package it.teamDigitale.daf.utils

import java.io.File
import util.Try

class FileOps {
  
  def getListOfFiles(dir: String, hidden: Boolean = false):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      val listFile: List[File] = d.listFiles.filter(_.isFile).toList
      
      if (!hidden) listFile.filter(!_.getName().startsWith(".")) else listFile
      
    } else {
      List[File]()
    }
  }
  
  def getListOfDir(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isDirectory).toList
    } else {
      List[File]()
    }
  }
  
  def mvFile(oldName: String, newName: String) = 
    Try(new File(oldName).renameTo(new File(newName))).getOrElse(false)
}