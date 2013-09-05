package org.mbari.odss.services.timeline

import com.typesafe.scalalogging.slf4j.Logging

import com.mongodb.casbah.Imports._
import com.mongodb.ServerAddress
import com.typesafe.config.ConfigFactory
import java.util.ServiceConfigurationError
import java.io.File


/**
 *
 */
class Setup(configFilename: String) extends AnyRef with Logging {

  private[this] var mcOpt: Option[MongoClient] = None

  logger.info(s"Loading configuration from $configFilename")
  val configFile = new File(configFilename)
  if (!configFile.canRead) {
    throw new ServiceConfigurationError("Could not read configuration file " + configFile)
  }

  val config = ConfigFactory.parseFile(configFile)
  val mongoConfig = config.getConfig("mongo")

  // log the mongoConfig; mask password:
  logger.info(s"mongoConfig = ${ConfigFactory.parseString("pw=\"*\"").withFallback(mongoConfig)}")

  val host = mongoConfig.getString("host")
  val port = mongoConfig.getInt(   "port")
  val user = mongoConfig.getString("user")
  val pw   = mongoConfig.getString("pw")
  val db   = mongoConfig.getString("db")

  logger.info(s"connecting to $host:$port/$db ...")
  val serverAddress = new ServerAddress(host, port)
  val credential = MongoCredential(user, db, pw.toCharArray)
  val mongoClient = MongoClient(serverAddress, List(credential))

  val mongoClientDb = mongoClient(db)
  val platformColl = mongoClientDb("platforms")
  val tokenColl    = mongoClientDb("tokens")
  val periodColl   = mongoClientDb("periods")

  implicit val app = new App(configFile, platformColl, tokenColl, periodColl)
  implicit val swagger = new OdssPlatformTimelineSwagger

  mcOpt = Some(mongoClient)

  def destroy() {
    logger.info("Closing MongoClient ...")
    mcOpt foreach { _.close() }
  }
}
