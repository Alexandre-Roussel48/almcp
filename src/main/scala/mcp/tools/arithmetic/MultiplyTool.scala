package mcp.tools.arithmetic

import io.circe.syntax._
import io.circe.{Json, JsonObject}
import zio._

import mcp.tools.common.{BinaryArgs, NumericPairSchema}
import mcp.tools.common.BinaryArgs.given
import mcp.model.mcp.ToolDefinition
import mcp.tools.{Tool, ToolError}

object MultiplyTool extends Tool:
  val definition: ToolDefinition = ToolDefinition(
    name = "multiply",
    description = Some("Multiply two numbers."),
    inputSchema = NumericPairSchema.definition
  )

  def execute(args: JsonObject): ZIO[Any, ToolError, Json] =
    ZIO
      .fromEither(args.asJson.as[BinaryArgs])
      .mapError(err => ToolError.InvalidArguments(s"Invalid arguments for multiply: ${err.getMessage}"))
      .map(parsed => Json.fromInt(parsed.a * parsed.b))
