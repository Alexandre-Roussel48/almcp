package mcp.tools.arithmetic

import io.circe.syntax._
import io.circe.{Json, JsonObject}
import zio._

import mcp.tools.common.{BinaryArgs, NumericPairSchema}
import mcp.tools.common.BinaryArgs.given
import mcp.model.mcp.ToolDefinition
import mcp.tools.{Tool, ToolError}

object DivideTool extends Tool:
  val definition: ToolDefinition = ToolDefinition(
    name = "divide",
    description = "Divide two numbers.",
    parameters = NumericPairSchema.definition
  )

  def execute(args: JsonObject): ZIO[Any, ToolError, Json] =
    ZIO
      .fromEither(args.asJson.as[BinaryArgs])
      .mapError(err => ToolError.InvalidArguments(s"Invalid arguments for divide: ${err.getMessage}"))
      .map(parsed => Json.fromInt(parsed.a / parsed.b))
