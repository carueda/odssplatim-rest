package org.mbari.odss.services.timeline

import com.typesafe.config.Config
import com.mongodb.casbah.Imports._


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
  val config:       Config,
  val platformColl: MongoCollection,
  val tokenColl:    MongoCollection,
  val periodColl:   MongoCollection)

