package it.teamDigitale.daf.ingestion.main

import it.teamDigitale.daf.ingestion.DataInjCsv
import it.teamDigitale.daf.utils.FileOps
import java.io.File

import com.typesafe.config.ConfigFactory
import it.teamDigitale.daf.datamanagers.examples.ConvSchemaGetter
import it.teamDigitale.daf.schemamanager.SchemaMgmt

object TestInjestion extends App {
  var config = ConfigFactory.load()
  val dataDir = config.getString("Inj-properties.sftpBasePath")
  val dataDirDone = config.getString("Inj-properties.sftpBasePathDone")
  val dataDirNoproc = config.getString("Inj-properties.sftpBasePathNoProc")

  val fileOps = new FileOps
  val dirList = fileOps.getListOfDir(dataDir)


  for (dir <- dirList) {
    val owner = dir.getName
    val csvList: List[File] = fileOps.getListOfFiles(dir.getPath)

    println(dir.getPath)

    csvList.foreach{

        file => {

          val fileName = file.getName

          val nameDataset = fileName.split('.')(0)
          println("Processing " + fileName + " ...")
          println("NameDataset " + nameDataset + " ...")

          ConvSchemaGetter.getSchema() match {
            case Some(convSchema) =>
              val injestion = new DataInjCsv(new SchemaMgmt(convSchema))
              injestion.doInj()
              //Spostare il file in folder .done

            case _ =>
              println("Error - Exit")
              //Spostare il file in folder .NoProc
              //sys.exit(1)

          }
        }

    }

  }

}