package org.mbari.odss.services.timeline

import org.scalatra.test.specs2._

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class TokensControllerSpec extends MutableScalatraSpec {

  val setup = new Setup("/etc/odssplatim.conf")
  import setup._
  addServlet(new TokensController, "/tokens/*")

  "GET / on TokensController" should {
    "return status 200" in {
      get("/") { status must_== 200 }
    }
  }
}
