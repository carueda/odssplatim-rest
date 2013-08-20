package org.mbari.odss.services.timeline

import org.scalatra.test.specs2._

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class TimelineServletSpec extends ScalatraSpec { def is =
  "GET / on TimelineServlet"                     ^
    "should return status 302"                  ! root302^
                                                end

  addServlet(classOf[TokensController], "/*")

  def root302 = get("/") {
    status must_== 302
  }
}
