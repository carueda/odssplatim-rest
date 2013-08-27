package org.mbari.odss.services.timeline

import com.typesafe.scalalogging.slf4j.Logging
import org.scalatra.{ScalatraServlet, CorsSupport}


/**
 * crude workaround for https://github.com/scalatra/scalatra/issues/290
 */
class ApiDocsFix extends ScalatraServlet with CorsSupport with Logging {

  options("/*"){
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"))
  }


  get("/:filename") {
    logger.info("GET /  params=" + params)

    val res = params.getOrElse("filename", "") match {
      case "resources.json" => resources
      case "tokens.json"    => tokens
      case "platforms.json" => platforms
      case "periods.json"   => periods
      case _ => halt(404)
    }

    response.setHeader("Access-Control-Allow-Origin", "*")
    res
  }
  
  private val restBasePath = "http://odss-test.shore.mbari.org:8080/odssplatim-rest"

  private val resources =
    """
       {"basePath": """" + restBasePath + """",
       "swaggerVersion": "1.0", "apiVersion": "1", "apis": [
          {
              "path": "/api-docs-fix/periods.{format}",
              "description": "Periods REST API"
          },
          {
              "path": "/api-docs-fix/platforms.{format}",
              "description": "Platforms REST API"
          },
          {
              "path": "/api-docs-fix/tokens.{format}",
              "description": "Tokens REST API"
          }
      ]}
    """

  private val tokens =
    """
      {"resourcePath": "/tokens", "listingPath": "/api-docs-fix/tokens", "description": "Tokens REST API", "apis": [
          {
              "path": "/tokens/",
              "description": "",
              "secured": false,
              "operations": [
                  {
                      "httpMethod": "GET",
                      "responseClass": "List[Token]",
                      "summary": "Gets tokens",
                      "notes": "Gets the tokens intersecting a time period as indicated by optional parameters.",
                      "deprecated": false,
                      "nickname": "getTokens",
                      "parameters": [
                          {
                              "name": "from",
                              "description": "Lower date-time limit",
                              "required": false,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "to",
                              "description": "Upper date-time limit",
                              "required": false,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          }
                      ],
                      "errorResponses": []
                  },
                  {
                      "httpMethod": "POST",
                      "responseClass": "Token",
                      "summary": "Add a new token",
                      "notes": "Dates must be strings parsable according to Joda's\n <a href=\"http://joda-time.sourceforge.net/apidocs/org/joda/time/format/ISODateTimeFormat.html#dateOptionalTimeParser()\">\ndateOptionalTimeParser</a>.",
                      "deprecated": false,
                      "nickname": "addToken",
                      "parameters": [
                          {
                              "name": "platform_id",
                              "description": "ID of associated platform",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "start",
                              "description": "Start date for the token",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "end",
                              "description": "End date for the token",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "state",
                              "description": "State or activity",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          }
                      ],
                      "errorResponses": []
                  }
              ]
          },
          {
              "path": "/tokens/{id}",
              "description": "",
              "secured": false,
              "operations": [
                  {
                      "httpMethod": "GET",
                      "responseClass": "Token",
                      "summary": "Finds a token by its id",
                      "deprecated": false,
                      "nickname": "findById",
                      "parameters": [
                          {
                              "name": "id",
                              "description": "ID of desired token",
                              "required": true,
                              "paramType": "path",
                              "allowMultiple": false,
                              "dataType": "string"
                          }
                      ],
                      "errorResponses": []
                  },
                  {
                      "httpMethod": "PUT",
                      "responseClass": "Token",
                      "summary": "Updates a token",
                      "notes": "Dates must be strings parsable according to Joda's\n <a href=\"http://joda-time.sourceforge.net/apidocs/org/joda/time/format/ISODateTimeFormat.html#dateOptionalTimeParser()\">\ndateOptionalTimeParser</a>.<br /> TODO: all parameters are required at the moment but they should be optional (except for the id).",
                      "deprecated": false,
                      "nickname": "updateToken",
                      "parameters": [
                          {
                              "name": "id",
                              "description": "ID of the token to be updated",
                              "required": true,
                              "paramType": "path",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "platform_id",
                              "description": "ID of associated platform",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "start",
                              "description": "Start date for the token",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "end",
                              "description": "End date for the token",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "state",
                              "description": "State or activity",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          }
                      ],
                      "errorResponses": []
                  },
                  {
                      "httpMethod": "DELETE",
                      "responseClass": "Token",
                      "summary": "Deletes a token of given id",
                      "deprecated": false,
                      "nickname": "deleteToken",
                      "parameters": [
                          {
                              "name": "id",
                              "description": "ID of the token to be removed",
                              "required": true,
                              "paramType": "path",
                              "allowMultiple": false,
                              "dataType": "string"
                          }
                      ],
                      "errorResponses": []
                  }
              ]
          }
      ], "models": {
          "Token": {
              "id": "Token",
              "description": "Token",
              "properties": {
                  "state": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "id": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "end": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "start": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "platform_id": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  }
              }
          }
      }, "basePath": """" + restBasePath + """",
      "swaggerVersion": "1.0", "apiVersion": "1"}
      """

  private val platforms =
    """
      {"resourcePath": "/platforms", "listingPath": "/api-docs-fix/platforms", "description": "Platforms REST API", "apis": [
          {
              "path": "/platforms/",
              "description": "",
              "secured": false,
              "operations": [
                  {
                      "httpMethod": "GET",
                      "responseClass": "List[Platform]",
                      "summary": "Gets platforms",
                      "deprecated": false,
                      "nickname": "getPlatforms",
                      "parameters": [],
                      "errorResponses": []
                  },
                  {
                      "httpMethod": "POST",
                      "responseClass": "Platform",
                      "summary": "Add a new platform",
                      "deprecated": false,
                      "nickname": "addPlatform",
                      "parameters": [
                          {
                              "name": "id",
                              "description": "ID for the platform",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "name",
                              "description": "Name of the platform",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          }
                      ],
                      "errorResponses": []
                  }
              ]
          },
          {
              "path": "/platforms/{id}",
              "description": "",
              "secured": false,
              "operations": [
                  {
                      "httpMethod": "GET",
                      "responseClass": "Platform",
                      "summary": "Finds a platform by its id",
                      "deprecated": false,
                      "nickname": "findById",
                      "parameters": [
                          {
                              "name": "id",
                              "description": "ID of desired platform",
                              "required": true,
                              "paramType": "path",
                              "allowMultiple": false,
                              "dataType": "string"
                          }
                      ],
                      "errorResponses": []
                  },
                  {
                      "httpMethod": "DELETE",
                      "responseClass": "Token",
                      "summary": "Deletes a platform of given id",
                      "deprecated": false,
                      "nickname": "deletePlatform",
                      "parameters": [
                          {
                              "name": "id",
                              "description": "ID of the platform to be removed",
                              "required": true,
                              "paramType": "path",
                              "allowMultiple": false,
                              "dataType": "string"
                          }
                      ],
                      "errorResponses": []
                  }
              ]
          }
      ], "models": {
          "Platform": {
              "id": "Platform",
              "description": "Platform",
              "properties": {
                  "name": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "id": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  }
              }
          },
          "Token": {
              "id": "Token",
              "description": "Token",
              "properties": {
                  "state": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "id": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "end": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "start": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "platform_id": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  }
              }
          }
      }, "basePath": """" + restBasePath + """",
      "swaggerVersion": "1.0", "apiVersion": "1"}
      """

  private val periods =
    """
      {"resourcePath": "/periods", "listingPath": "/api-docs-fix/periods", "description": "Periods REST API", "apis": [
          {
              "path": "/periods/",
              "description": "",
              "secured": false,
              "operations": [
                  {
                      "httpMethod": "GET",
                      "responseClass": "List[Period]",
                      "summary": "Gets all periods",
                      "notes": "Gets all periods",
                      "deprecated": false,
                      "nickname": "getPeriods",
                      "parameters": [],
                      "errorResponses": []
                  },
                  {
                      "httpMethod": "POST",
                      "responseClass": "Period",
                      "summary": "Adds a new period",
                      "notes": "Dates must be strings parsable according to Joda's\n <a href=\"http://joda-time.sourceforge.net/apidocs/org/joda/time/format/ISODateTimeFormat.html#dateOptionalTimeParser()\">\ndateOptionalTimeParser</a>.",
                      "deprecated": false,
                      "nickname": "addPeriod",
                      "parameters": [
                          {
                              "name": "name",
                              "description": "Name of the period",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "start",
                              "description": "Start date for the period",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "end",
                              "description": "End date for the period",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          }
                      ],
                      "errorResponses": []
                  }
              ]
          },
          {
              "path": "/periods/{id}",
              "description": "",
              "secured": false,
              "operations": [
                  {
                      "httpMethod": "PUT",
                      "responseClass": "Token",
                      "summary": "Updates a period",
                      "notes": "Dates must be strings parsable according to Joda's\n <a href=\"http://joda-time.sourceforge.net/apidocs/org/joda/time/format/ISODateTimeFormat.html#dateOptionalTimeParser()\">\ndateOptionalTimeParser</a>.<br /> TODO: all parameters are required at the moment but they should be optional (except for the id).",
                      "deprecated": false,
                      "nickname": "updatePeriod",
                      "parameters": [
                          {
                              "name": "id",
                              "description": "ID of the period to be updated",
                              "required": true,
                              "paramType": "path",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "name",
                              "description": "Name of the period",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "start",
                              "description": "Start date for the period",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          },
                          {
                              "name": "end",
                              "description": "End date for the period",
                              "required": true,
                              "paramType": "query",
                              "allowMultiple": false,
                              "dataType": "string"
                          }
                      ],
                      "errorResponses": []
                  },
                  {
                      "httpMethod": "DELETE",
                      "responseClass": "Token",
                      "summary": "Deletes a period of given id",
                      "deprecated": false,
                      "nickname": "deletePeriod",
                      "parameters": [
                          {
                              "name": "id",
                              "description": "ID of the period to be removed",
                              "required": true,
                              "paramType": "path",
                              "allowMultiple": false,
                              "dataType": "string"
                          }
                      ],
                      "errorResponses": []
                  }
              ]
          }
      ], "models": {
          "Token": {
              "id": "Token",
              "description": "Token",
              "properties": {
                  "state": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "id": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "end": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "start": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "platform_id": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  }
              }
          },
          "Period": {
              "id": "Period",
              "description": "Period",
              "properties": {
                  "name": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "start": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "id": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  },
                  "end": {
                      "description": null,
                      "required": true,
                      "type": "string"
                  }
              }
          }
      }, "basePath": """" + restBasePath + """",
      "swaggerVersion": "1.0", "apiVersion": "1"}
      """

}
