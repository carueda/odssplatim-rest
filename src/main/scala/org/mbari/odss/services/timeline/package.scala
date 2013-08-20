package org.mbari.odss.services

import org.joda.time.format.{ISODateTimeFormat, DateTimeFormatter}
import com.github.nscala_time.time.Imports._
import scala.language.implicitConversions

/**
 * Some common utilities and basic definitions.
 */
package object timeline {

  /**
   * @see http://joda-time.sourceforge.net/apidocs/org/joda/time/format/ISODateTimeFormat.html#dateOptionalTimeParser()
   */
  val dateTimeParser: DateTimeFormatter = ISODateTimeFormat.dateOptionalTimeParser()

  implicit def s2d(str: String): DateTime = dateTimeParser.parseDateTime(str)

  val minDate: DateTime = "0000-01-01"
  val maxDate: DateTime = "9999-12-31T23:59"

}
