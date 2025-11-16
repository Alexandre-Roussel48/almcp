package mcp.tools.common

import io.circe.{Json, JsonObject}
import mcp.model.mcp.JsonSchema

object NumericPairSchema:
  val definition: JsonSchema = JsonSchema(
    properties = Some(
      JsonObject(
        "a" -> Json.obj("type" -> Json.fromString("number")),
        "b" -> Json.obj("type" -> Json.fromString("number"))
      )
    ),
    required = Some(List("a", "b"))
  )
