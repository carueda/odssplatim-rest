package org.mbari.odss.services.timeline

import com.typesafe.config.Config
import com.mongodb.casbah.Imports._
import java.io.File


case class Platform(id:        Option[String],
                    name:       String)

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

