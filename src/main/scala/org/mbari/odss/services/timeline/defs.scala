package org.mbari.odss.services.timeline

import com.typesafe.config.Config
import com.mongodb.casbah.Imports._
import java.io.File


case class Platform(id:        Option[String],
                    name:       String)

/*
in ODSS
{
    "__v" : 0,
    "_id" : ObjectId("52203575a0b9d4224800001f"),
    "abbreviation" : "RC",
    "color" : "#FF6600",
    "name" : "R_CARSON",
    "trackingDBID" : 1,
    "typeName" : "ship"
}
 */
case class OdssPlatform(id:        Option[String],
                        name:      String,
                        color:     Option[String]
                       )

case class Token(id:            Option[String],
                 platform_id:   String,
                 start:         String,
                 end:           String,
                 state:         String)

case class Period(id:     Option[String],
                  name:   String,
                  start:  String,
                  end:    String)


class App(
  val configFile:   File,
  val platformColl: MongoCollection,
  val tokenColl:    MongoCollection,
  val periodColl:   MongoCollection)

