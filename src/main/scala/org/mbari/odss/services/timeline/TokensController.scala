package org.mbari.odss.services.timeline

import org.scalatra.swagger._

import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.TypeImports.ObjectId
import scala.Some
import org.json4s.JsonAST.JString


/**
 * Controller for the tokens route.
 *
 * @param app
 * @param swagger
 */
class TokensController(implicit val app: App,
                       implicit val swagger: Swagger) extends OdssPlatformTimelineStack with Logging {

  val platformColl = app.platformColl
  val tokenColl = app.tokenColl


  override protected val applicationName = Some("tokens")
  protected val applicationDescription = "Tokens REST API"


  val apiGet =
    (apiOperation[List[Token]]("getTokens")
      summary "Gets tokens"
      notes "Gets the tokens intersecting a time period as indicated by optional parameters."
      parameters(
      queryParam[Option[String]]("from").description("Lower date-time limit"),
      queryParam[Option[String]]("to").  description("Upper date-time limit")))

  get("/", operation(apiGet)) {
    logger.info("GET /tokens  params=" + params)

    val query = MongoDBObject()
    params.get("from") foreach {from => query.putAll("start" $gte from)}
    params.get("to")   foreach {to   => query.putAll("end"   $lte to)}
    logger.info("query = " + query)

    val found = tokenColl.find(query)
    val res = found map {e =>
      for {
        id           <- e.getAs[ObjectId]("_id")
        platform_id  <- e.getAs[String]("platform_id")
        start        <- e.getAs[String]("start")
        end          <- e.getAs[String]("end")
        state        <- e.getAs[String]("state")
        description  <- e.getAs[String]("description")
      } yield Token(Some(id.toString), platform_id, start, end, state, description)
    }
    // sort by start date (mainly for debugging purposes)
    res.toList.filter(_ != None) sortWith (_.get.start < _.get.start)
  }

  val apiFindById =
    (apiOperation[Token]("findById")
      summary "Finds a token by its id"
      parameter pathParam[String]("id").description("ID of desired token"))

  get("/:id", operation(apiFindById)) {
    val id: String = params.getOrElse("id", halt(400, "id"))
    val _id = if (ObjectId.isValid(id)) new ObjectId(id) else halt(404, s"'$id' is an invalid id")
    val query = MongoDBObject("_id" -> _id)
    logger.info("GET /tokens/" + id)
    val found = tokenColl.find(query)
    val res = found map {e =>
      for {
        platform     <- e.getAs[String]("platform_id")
        start        <- e.getAs[String]("start")
        end          <- e.getAs[String]("end")
        state        <- e.getAs[String]("state")
        description  <- e.getAs[String]("description")
      } yield Token(Some(id), platform, start, end, state, description)
    }
    res.toSet
  }

  val apiAdd =
    (apiOperation[Token]("addToken")
      summary "Add a new token"
      notes dateTimeNotes
      parameters(
        queryParam[String]("platform_id").description("ID of associated platform"),
        queryParam[String]("start").      description("Start date for the token"),
        queryParam[String]("end").        description("End date for the token"),
        queryParam[String]("state").      description("State or activity")))

  post("/", operation(apiAdd)) {
    logger.debug("POST token params = " + params)

//    val token = parsedBody.extract[Token]
//    logger.debug("POST token = " + token)
//
//    val newObj = MongoDBObject("platform_id" -> token.platform_id,
//                               "start"       -> token.start,
//                               "end"         -> token.end,
//                               "state"       -> token.state)

    val platform_id  = params.get("platform_id").get
    val start        = params.get("start").get
    val end          = params.get("end").get
    val state        = params.get("state").get
    val description  = params.get("description").get

    val newObj = MongoDBObject("platform_id" -> platform_id,
                               "start"       -> start,
                               "end"         -> end,
                               "state"       -> state,
                               "description" -> description)
    logger.debug("POST token = " + newObj)

    tokenColl += newObj

    val id = for (x <- newObj.getAs[ObjectId]("_id")) yield x.toString

    logger.info("Token posted: " + newObj + "   id = " + id)

    //token.copy(id=id)
    Token(id, platform_id, start, end, state, description)
  }


  val apiUpdate =
    (apiOperation[Token]("updateToken")
      summary "Updates a token"
      notes dateTimeNotes +
         "<br /> TODO: all parameters are required at the moment but they should be optional (except for the id)."
      parameters(
        pathParam[String]("id").            description("ID of the token to be updated"),
        queryParam[String]("platform_id").  description("ID of associated platform"),
        queryParam[String]("start").        description("Start date for the token"),
        queryParam[String]("end").          description("End date for the token"),
        queryParam[String]("state").        description("State or activity")))

  put("/:id", operation(apiUpdate)) {
    val id: String = params.getOrElse("id", halt(400, "id"))

    logger.info("PUT params = " + params)
//    logger.info("PUT content-type = " + request.header("content-type"))
    logger.info("PUT body = " + request.body)

    val json = parse(request.body)
    val map = json.extract[Map[String, String]]

    val platform_id:   String = map.getOrElse("platform_id", halt(400, "platform_id"))
    val start:         String = map.getOrElse("start",       halt(400, "start"     ))
    val end:           String = map.getOrElse("end",         halt(400, "end"       ))
    val state:         String = map.getOrElse("state",       halt(400, "state"     ))
    val description:   String = map.getOrElse("description", halt(400, "description"))

    val _id = if (ObjectId.isValid(id)) new ObjectId(id) else halt(404, s"'$id' is an invalid id")

    val obj = MongoDBObject("_id" -> _id)
    tokenColl.findOne(obj) match {
      case None    => halt(404, "document with id = '" +id+ "' does not exist in collection")

      case Some(found) => {
        logger.info("BBB " + found)
        val update = $set("platform_id" -> platform_id,
                          "start"       -> start,
                          "end"         -> end,
                          "description" -> description,
                          "state"       -> state)
        val result = tokenColl.update(obj, update)
        val info = "Token '" + id + "' updated (" + result.getN + "): " + update
        logger.info(info)
        JString(info)
      }
    }
  }

  val apiDelete =
    (apiOperation[Token]("deleteToken")
      summary "Deletes a token of given id"
      parameter pathParam[String]("id").description("ID of the token to be removed"))

  delete("/:id", operation(apiDelete)) {
    val id: String = params.getOrElse("id", halt(400, "id"))
    val _id = if (ObjectId.isValid(id)) new ObjectId(id) else halt(404, s"'$id' is an invalid id")
    val obj = MongoDBObject("_id" -> _id)

    val result = tokenColl.remove(obj)
    val info = "Token '" + id + "' removed (" + result.getN + ")"
    logger.info(info)
    JString(info)
  }

  ////////////////////////////////////////////////////
  // TODO version operations

  val apiGetVersionInfo =
    (apiOperation[String]("getVersions")
      summary "Gets info about the timeline versions")

  get("/versions", operation(apiGetVersionInfo)) {

    halt(501, "version operations not implemented")

/*
    val obj = MongoDBObject("versions" -> MongoDBObject("$exists" -> true))
    tokenColl.findOne(obj) match {
      case None    => halt(404, "no version info defined")
      case Some(e) => {
        e.getAs[MongoDBList]("versions") match {
          case None             => halt(404, "no version info defined")
          case Some(versions)   =>
            val l = versions map { v => v.toString}
            ...
        }
      }
    }
*/
  }

  /////////////////////////////////////////////////////
  // special operations

  /**
   * Repopulates the collection.
   */
  post("/_reload") {
    repopulate(app, tokenColl, "tokens")
  }

}
