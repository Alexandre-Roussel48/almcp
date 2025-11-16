package mcp.tools

import io.circe.{Json, JsonObject}
import mcp.model.mcp.ToolDefinition
import zio._

sealed trait ToolError

object ToolError:
  final case class InvalidArguments(msg: String) extends ToolError
  final case class ExecutionFailed(msg: String) extends ToolError
  final case class ToolNotFound(name: String) extends ToolError

trait Tool:
  def definition: ToolDefinition
  def execute(args: JsonObject): ZIO[Any, ToolError, Json]
