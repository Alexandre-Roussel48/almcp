package mcp.util

import zio._

object DebugLogging:
  private val enabled: Boolean =
    sys.env.get("MCP_DEBUG").exists(_.nonEmpty)

  def log(line: => String): UIO[Unit] =
    if enabled then Console.printLineError(line).orDie
    else ZIO.unit
