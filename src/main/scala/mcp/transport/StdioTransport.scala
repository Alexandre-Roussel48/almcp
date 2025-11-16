package mcp.transport

import zio._
import zio.stream._
import mcp.util.DebugLogging

object StdioTransport:
  val layer: ZLayer[Any, Nothing, Transport] = ZLayer.succeed(new Transport:
    val read: ZStream[Any, TransportError, String] =
      ZStream
        .repeatZIO(
          Console.readLine.mapError {
            case eof: java.io.EOFException =>
              TransportError.EndOfStream
            case other =>
              TransportError.Read(other)
          }
        )
        .catchSome { case TransportError.EndOfStream => ZStream.empty }
        .tap(line => DebugLogging.log(s"[MCP RX] $line"))
    
    def write(line: String): UIO[Unit] =
      Console.printLine(line).orDie *> DebugLogging.log(s"[MCP TX] $line")
  )
