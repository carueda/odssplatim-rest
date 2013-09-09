package org.mbari.odss.services.timeline

import org.json4s._
import org.json4s.native.JsonMethods._

import org.scalatra.test.specs2._


// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class TokensControllerSpec extends MutableScalatraSpec {

  implicit val formats = org.json4s.DefaultFormats

  val setup = new Setup("/etc/odssplatim.conf")
  import setup._
  addServlet(new TokensController, "/*")

  val platform_id = "test_plat_id"

  "GET /" should {
    "return status 200" in {
      get("/") {
        status must_== 200
      }
    }
  }

  "GET /badid" should {
    "return status 404 because of invalid id" in {
      get("/badid") { status must_== 404 }
    }
  }

  "POST/PUT/DELETE" should {
    "work" in {

      // add a new token:
      val postParams = Map("platform_id" -> platform_id,
        "start" -> "test_start", "end" -> "test_end", "state" -> "test_state")

      var token_id: Option[String] = None

      post("/", params=postParams) {
        status must_== 200

        val json = parse(body)
        val map = json.extract[Map[String, String]]
        map.get("platform_id") must_== Some(platform_id)
        map must haveKey("id")

        token_id = map.get("id")
      }

      // update token:
      val map = Map("platform_id" -> platform_id,
        "start" -> "test_start2", "end" -> "test_end2", "state" -> "test_state2")

      import org.json4s.JsonDSL._
      val putBody = pretty(render(map))
      put(s"/${token_id.get}", putBody, headers=Map("content-type" -> "application/json")) {
        status must_== 200
      }

      // delete token:
      submit("DELETE", s"/${token_id.get}") {
        status must_== 200
      }

    }
  }

}
