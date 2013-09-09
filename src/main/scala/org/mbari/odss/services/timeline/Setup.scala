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

  private def cfgString(path: String, default: String = null): String = {
    if (mongoConfig.hasPath(path)) mongoConfig.getString(path) else default
  }

  val host = mongoConfig.getString("host")
  val port = mongoConfig.getInt(   "port")
  val user = cfgString("user")
  val pw   = cfgString("pw")
  val db   = mongoConfig.getString("db")

  val serverAddress = new ServerAddress(host, port)
  val mongoClient: MongoClient = if (user != null && pw != null) {
      logger.info(s"connecting to $host:$port/$db using credentials ...")
      val credential = MongoCredential(user, db, pw.toCharArray)
      MongoClient(serverAddress, List(credential))
    }
    else {
      logger.info(s"connecting to $host:$port/$db with no credentials ...")
      MongoClient(serverAddress)
    }

  val mongoClientDb = mongoClient(db)
  val platformColl  = mongoClientDb(cfgString("platforms", "platforms"))
  val periodColl    = mongoClientDb(cfgString("periods",   "periods"))
  val tokenColl     = mongoClientDb(cfgString("tokens",    "tokens"))

  implicit val app = new App(configFile, platformColl, tokenColl, periodColl)
  implicit val swagger = new OdssPlatformTimelineSwagger

  mcOpt = Some(mongoClient)

  def destroy() {
    logger.info("Closing MongoClient ...")
    mcOpt foreach { _.close() }
  }
}
