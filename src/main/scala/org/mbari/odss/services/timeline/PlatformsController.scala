package org.mbari.odss.services.timeline

import org.scalatra.swagger._

import com.mongodb.casbah.Imports._
import scala.Some
import org.json4s.JsonAST.JString
import com.typesafe.scalalogging.slf4j.Logging


/**
 * Controller for the platforms route.
 *
 * @param app
 * @param swagger
 */
class PlatformsController(implicit val app: App,
                          implicit val swagger: Swagger) extends OdssPlatformTimelineStack with Logging {

  val platformColl = app.platformColl

  override protected val applicationName = Some("platforms")
  protected val applicationDescription = "Platforms REST API"

  val apiGet =
    (apiOperation[List[Platform]]("getPlatforms")
      summary "Gets platforms")

  get("/", operation(apiGet)) {
    platformColl.find()
    val res = platformColl map {e =>
      for {
        id       <- e.getAs[String]("_id")
        name     <- e.getAs[String]("name")
      } yield Platform(Some(id), name)
    }
    res.toSet
  }

  val apiFindById =
    (apiOperation[Platform]("findById")
      summary "Finds a platform by its id"
      parameter pathParam[String]("id").description("ID of desired platform"))

  get("/:id", operation(apiFindById)) {
    val _id = params("id")

    logger.info("get platform info for '" + _id + "'")

    val q = MongoDBObject("_id" -> _id)
    val found = platformColl.find(q)

    val res = found map {e =>
      for {
        name      <- e.getAs[String]("name")
      } yield Platform(Some(_id), name)
    }
    res.toSet
  }

  val apiAdd =
    (apiOperation[Platform]("addPlatform")
      summary "Add a new platform"
      parameters(
        queryParam[String]("id").  description("ID for the platform"),
        queryParam[String]("name").description("Name of the platform")))

  post("/", operation(apiAdd)) {
    val _id = params("id")
    val name = params("name")

    val q = MongoDBObject("_id" -> _id)
    platformColl.findOne(q) match {
      case Some(_) => halt(400, "'" +_id+ "' already in platform collection")
      case None    => platformColl += MongoDBObject("_id" -> _id, "name" -> name)
    }
  }

  val apiDelete =
    (apiOperation[Token]("deletePlatform")
      summary "Deletes a platform of given id"
      parameter pathParam[String]("id").description("ID of the platform to be removed"))

  delete("/:id", operation(apiDelete)) {
    val _id = params("id")
    val obj = MongoDBObject("_id" -> _id)

    val result = platformColl.remove(obj)
    val info = if (result.getN > 0) "platform '" + _id + "' removed (" + result.getN + ")"
               else "No platforms found with name '" + _id + "'"
    logger.info(info)
    JString(info)
  }

  /////////////////////////////////////////////////////
  // special operations

  /**
   * Repopulates the collection.
   */
  post("/_reload") {
    repopulate(app, platformColl, "platforms")
  }

}
