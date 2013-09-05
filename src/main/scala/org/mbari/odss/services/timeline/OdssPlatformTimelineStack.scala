package org.mbari.odss.services.timeline

import org.scalatra._
import org.scalatra.json.NativeJsonSupport
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.swagger.SwaggerSupport
import com.mongodb.casbah.Imports._
import java.util.Collections
import com.typesafe.scalalogging.slf4j.Logging
import com.typesafe.config.ConfigFactory


/**
 * Base class for the various controllers.
 */
trait OdssPlatformTimelineStack extends ScalatraServlet with NativeJsonSupport with SwaggerSupport with Logging
{
  // all responses in JSON (except for the static files)
  protected implicit val jsonFormats: Formats = DefaultFormats

  val dateTimeNotes =  """Dates must be strings parsable according to Joda's
                         | <a href="http://joda-time.sourceforge.net/apidocs/org/joda/time/format/ISODateTimeFormat.html#dateOptionalTimeParser()">
                         |dateOptionalTimeParser</a>.""".stripMargin

  before() {
    contentType = formats("json")
  }

  /**
   * Repopulates a collection with any elements indicated in the corresp configuration
   * @param app    application info
   * @param coll   collection to repopulate
   * @param path   path in config
   */
  def repopulate(app: App, coll: MongoCollection, path: String) = {
    logger.info("repopulating " + path + " from file " +app.configFile)
    val config = ConfigFactory.parseFile(app.configFile)
    import collection.JavaConversions._
    val elements = if (config.hasPath(path)) config.getList(path).unwrapped else Collections.emptyList()
    if (elements.length > 0) {
      val result = coll.remove(MongoDBObject())
      logger.debug(coll.name + ": docs removed: " + result.getN)
      elements foreach { element =>
        val map = element.asInstanceOf[java.util.Map[String, String]]
        val obj = MongoDBObject(map.toList)
        logger.info(coll.name + " += " + obj)
        coll += obj
      }
    }
  }
}
