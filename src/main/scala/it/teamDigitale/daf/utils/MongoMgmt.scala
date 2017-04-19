package it.teamDigitale.daf.utils

import org.mongodb.scala._

import org.mongodb.scala.result._

case class MongoConf(host: String)

class MongoMgmt(conf: Option[MongoConf] = None) {
  
  val mongoClient: MongoClient = conf match {
    case Some(s) => MongoClient()
    case None => MongoClient()
  }
  
  def getDatabase(dbName: String): MongoDatabase = {
    mongoClient.getDatabase(dbName)
  }
  
  
}