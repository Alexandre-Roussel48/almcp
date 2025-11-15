package mcp.tools

import mcp.model.mcp.ToolDefinition
import zio._

trait ToolRegistry:
  def listTools(): UIO[List[ToolDefinition]]
  def getTool(name: String): IO[ToolError, Tool]

object ToolRegistry:
  private val bundledTools: List[Tool] = ArithmeticTools.all ++ LogicTools.all

  val layer: ZLayer[Any, Nothing, ToolRegistry] =
    ZLayer.succeed(make())

  def make(tools: List[Tool] = bundledTools): ToolRegistry = new ToolRegistry:
    def listTools(): UIO[List[ToolDefinition]] =
      ZIO.succeed(tools.map(_.definition))

    def getTool(name: String): IO[ToolError, Tool] =
      ZIO.fromEither(
        tools.find(_.definition.name == name).toRight(ToolError.ToolNotFound(name))
      )
