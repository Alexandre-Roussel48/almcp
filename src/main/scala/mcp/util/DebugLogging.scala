package mcp.util

import zio._

object DebugLogging:
  private val enabled: Boolean =
    sys.env
      .get("MCP_DEBUG")
      .map(_.trim.toLowerCase)
      .exists(value => value == "1" || value == "true" || value == "yes")

  def log(line: => String): UIO[Unit] =
    if enabled then Console.printLineError(line).orDie
    else ZIO.unit
