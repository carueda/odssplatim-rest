package org.mbari.odss.services.timeline

import org.scalatra.swagger._

import com.mongodb.casbah.Imports._
import scala.Some
import com.typesafe.scalalogging.slf4j.Logging


/**
 * TODO: "/timelines" operations OBSOLETE --  WILL BE REMOVED
 *
 * @param app
 * @param swagger
 */
class TimelinesController(implicit val app: App,
                          implicit val swagger: Swagger) extends OdssPlatformTimelineStack with Logging {

  val platformColl = app.platformColl
  val tokenColl = app.tokenColl


  override protected val applicationName = Some("timelines")
  protected val applicationDescription = "timelines REST API"


  get("/") {
    // TODO: some search criteria, in particular min/max dates.

    def getPlatform(platform_id: String) = {
      logger.info(" getPlatform '" + platform_id + "'")
      val q = MongoDBObject("_id" -> platform_id)
      platformColl.find(q) map {e =>
        for {
          name      <- e.getAs[String]("name")
        } yield Platform(Some(platform_id), name)
      }
    }

    tokenColl.find()
    val res = tokenColl map {e =>
      logger.info("get timelines: element = " + e)
      for {
        platform_id <- e.getAs[String]("platform_id")
      } yield platform_id
    }
    val unique = res.toSet
    for {platform_id <- unique
         foundPlatform <- getPlatform(platform_id.get)
    } yield foundPlatform
  }

  get("/:platform_id") {
    val platform_id = params("platform_id")

    val q = MongoDBObject("platform_id" -> platform_id)
    val found = tokenColl.find(q)

    // map to Token's
    val res = found map {e =>
      logger.info("get timelines: element = " + e)
      for {
        id       <- e.getAs[ObjectId]("_id")
        start    <- e.getAs[String]("start")
        end      <- e.getAs[String]("end")
        state    <- e.getAs[String]("state")
      } yield Token(Some(id.toString), platform_id, start, end, state)
    }
    res.toSet
  }

}
