package org.mbari.odss.services.timeline

import org.scalatra.swagger._

import com.mongodb.casbah.Imports._
import scala.Some
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.TypeImports.ObjectId


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
    def getPlatform(platform_id: String) = {
      logger.info(s" getPlatform '$platform_id'")
      val q = MongoDBObject("_id" -> new ObjectId(platform_id))
      platformColl.find(q) map {e =>
        for {
          name          <- e.getAs[String]("name")
          abbreviation  <- e.getAs[String]("abbreviation")
          typeName      <- e.getAs[String]("typeName")
          color         <- e.getAs[String]("color")
        } yield Platform(Some(platform_id), name=name,
          Some(abbreviation),
          Some(typeName),
          Some(color))
      }
    }

    tokenColl.find()
    val onlyIds = tokenColl map {e => e.getAs[String]("platform_id").getOrElse(null)}
    val validIds = onlyIds filter ObjectId.isValid
    val uniqueIds = validIds.toSet

    logger.info(s"uniqueIds = $uniqueIds" )
    for {platform_id <- uniqueIds
         foundPlatform <- getPlatform(platform_id)
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
        id           <- e.getAs[ObjectId]("_id")
        start        <- e.getAs[String]("start")
        end          <- e.getAs[String]("end")
        state        <- e.getAs[String]("state")
      } yield Token(Some(id.toString), platform_id, start, end, state, e.getAsOrElse[String]("description", ""))
    }
    res.toSet
  }

}
