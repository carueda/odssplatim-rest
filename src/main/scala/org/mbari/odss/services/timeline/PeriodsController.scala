package org.mbari.odss.services.timeline

import org.scalatra.swagger._

import com.mongodb.casbah.Imports._
import scala.Some
import org.json4s.JsonAST.{JField, JObject, JString, JArray}
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.TypeImports.ObjectId
import com.typesafe.config.ConfigFactory
import java.util.Collections


/**
 * Controller for the periods route.
 *
 * @param app
 * @param swagger
 */
class PeriodsController(implicit val app: App,
                        implicit val swagger: Swagger) extends OdssPlatformTimelineStack with Logging {

  val periodColl = app.periodColl

  override protected val applicationName = Some("periods")
  protected val applicationDescription = "Periods REST API"


  val apiGet =
    (apiOperation[List[Period]]("getPeriods")
      summary "Gets all periods"
      notes "Gets all periods"
      )

  get("/", operation(apiGet)) {
    periodColl.find()
    val res = periodColl map {e =>
      logger.info("get periods: element = " + e)
      for {
        id      <- e.getAs[ObjectId]("_id")
        name    <- e.getAs[String]("period")
        start   <- e.getAs[String]("start")
        end     <- e.getAs[String]("end")
      } yield Period(Some(id.toString), name, start, end)
    }
    res.toSet
  }

  val apiAdd =
    (apiOperation[Period]("addPeriod")
      summary "Adds a new period"
      notes dateTimeNotes
      parameters(
        queryParam[String]("name").     description("Name of the period"),
        queryParam[String]("start").    description("Start date for the period"),
        queryParam[String]("end").      description("End date for the period")))

  post("/", operation(apiAdd)) {
    val name     = params("name")
    val start    = params("start")
    val end      = params("end")

    val newObj = MongoDBObject(
      "period"   -> name,
      "start"    -> start,
      "end"      -> end)

    periodColl += newObj

    val id = for (x <- newObj.getAs[ObjectId]("_id")) yield x.toString

    logger.info("period posted: " + newObj + "   id = " + id)

    Period(id, name, start, end)
  }

  val apiUpdate =
    (apiOperation[Period]("updatePeriod")
      summary "Updates a period"
      notes dateTimeNotes +
        "<br /> TODO: all parameters are required at the moment but they should be optional (except for the id)."
      parameters(
        pathParam[String]("id").          description("ID of the period to be updated"),
        queryParam[String]("name").       description("Name of the period"),
        queryParam[String]("start").      description("Start date for the period"),
        queryParam[String]("end").        description("End date for the period")))

  put("/:id", operation(apiUpdate)) {
    logger.info("PUT: " + params)
    val id: String = params.getOrElse("id", halt(400, "id"))
    val _id = if (ObjectId.isValid(id)) new ObjectId(id) else halt(400, "id is invalid")
    val obj = MongoDBObject("_id" -> _id)

    periodColl.findOne(obj) match {
      case None    => halt(400, "document with id = '" +id+ "' does not exist in collection")

      case Some(_) => {
        val update = $set(
          "period"   -> params("name"),
          "start"    -> params("start"),
          "end"      -> params("end"))
        val result = periodColl.update(obj, update)
        val info = "period '" + id + "' updated (" + result.getN + "): " + update
        logger.info(info)
        JString(info)
      }
    }
  }

  val apiDelete =
    (apiOperation[Period]("deletePeriod")
      summary "Deletes a period of given id"
      parameter pathParam[String]("id").description("ID of the period to be removed"))

  delete("/:id", operation(apiDelete)) {
    val id: String = params.getOrElse("id", halt(400, "id"))
    val _id = if (ObjectId.isValid(id)) new ObjectId(id) else halt(400, "id is invalid")
    val obj = MongoDBObject("_id" -> _id)
    val result = periodColl.remove(obj)
    val info = "period '" + id + "' removed (" + result.getN + ")"
    logger.info(info)
    JString(info)
  }

  ////////////////////////////////////////////////////
  // default period operations

  val apiGetDefaultId =
    (apiOperation[String]("getDefaultPeriodId")
      summary "Gets the ID of the default period")

  get("/default", operation(apiGetDefaultId)) {
    val obj = MongoDBObject("defaultPeriodId" -> MongoDBObject("$exists" -> true))

    periodColl.findOne(obj) match {
      case None    => halt(404, "no default period defined")

      case Some(e) => {
        val defaultPeriodId = e.getAs[String]("defaultPeriodId").getOrElse("no default period defined")
        JObject(JField("defaultPeriodId", JString(defaultPeriodId)))
      }
    }
  }

  val apiSetDefaultPeriodId =
    (apiOperation[String]("setDefaultPeriodId")
      summary "Sets the ID of the default period")

  put("/default/:id", operation(apiSetDefaultPeriodId)) {
    logger.info("PUT: " + params)
    val id: String = params.getOrElse("id", halt(400, "id"))

    if (!ObjectId.isValid(id)) halt(400, "id is invalid")

    val obj = MongoDBObject("defaultPeriodId" -> MongoDBObject("$exists" -> true))

    periodColl.findOne(obj) match {
      case None    => {
        val newObj = MongoDBObject("defaultPeriodId" -> id)
        periodColl += newObj
        val info = "default period set to '" + id + "'"
        logger.info(info)
        JString(info)
      }

      case Some(e) => {
        val update = $set("defaultPeriodId" -> id)
        val result = periodColl.update(obj, update)
        val info = "default period updated to '" + id + "'"
        logger.info(info)
        JString(info)
      }
    }
  }

  val apiDeleteDefaultId =
    (apiOperation[String]("deleteDefaultPeriodId")
      summary "Deletes the indication of the default period")

  delete("/default", operation(apiDeleteDefaultId)) {
    logger.info("DELETE: " + params)
    val obj = MongoDBObject("defaultPeriodId" -> MongoDBObject("$exists" -> true))

    val result = periodColl.remove(obj)
    val info = "Default period ID removed (" + result.getN + ")"
    logger.info(info)
    JString(info)
  }

  ////////////////////////////////////////////////////
  // holidays

  val apiGetHolidays =
    (apiOperation[String]("getHolidays")
      summary "Gets the holidays")

  get("/holidays", operation(apiGetHolidays)) {
    val obj = MongoDBObject("holidays" -> MongoDBObject("$exists" -> true))

    periodColl.findOne(obj) match {
      case None    => halt(404, "no holidays defined")

      case Some(e) => {
        // http://stackoverflow.com/a/11200484/830737
        val holidays = (List() ++ e("holidays").asInstanceOf[BasicDBList]) map {x => JString(x.asInstanceOf[String]) }

        JObject(JField("holidays", JArray(holidays)))
      }
    }
  }

  /////////////////////////////////////////////////////
  // special operations

  /**
   * Repopulates the collection.
   */
  post("/_reload") {
    repopulate(app, periodColl, "periods")
    resetHolidays()

    def resetHolidays() = {
      val path: String = "holidays"
      logger.info("resetting " + path + " from file " +app.configFile)
      val config = ConfigFactory.parseFile(app.configFile)
      import collection.JavaConversions._
      val elements = if (config.hasPath(path)) config.getList(path).unwrapped else Collections.emptyList()
      if (elements.length > 0) {
        val obj = MongoDBObject("holidays" -> MongoDBObject("$exists" -> true))

        periodColl.findOne(obj) match {
          case None    => {
            val newObj = MongoDBObject("holidays" -> elements)
            periodColl += newObj
            val info = "holidays set to " + elements
            logger.info(info)
            JString(info)
          }

          case Some(e) => {
            val update = $set("holidays" -> elements)
            val result = periodColl.update(obj, update)
            val info = "holidays updated to " + elements
            logger.info(info)
            JString(info)
          }
        }
      }
    }
  }

}
