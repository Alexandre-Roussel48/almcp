package mcp.tools.common

import io.circe.{Json, JsonObject}

object NumericPairSchema:
  val definition: JsonObject = JsonObject(
    "type" -> Json.fromString("object"),
    "properties" -> Json.obj(
      "a" -> Json.obj("type" -> Json.fromString("number")),
      "b" -> Json.obj("type" -> Json.fromString("number"))
    ),
    "required" -> Json.arr(Json.fromString("a"), Json.fromString("b"))
  )
