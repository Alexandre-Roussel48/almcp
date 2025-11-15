package mcp.transport

import zio._
import zio.stream._

sealed trait TransportError extends Throwable

object TransportError:
  final case class Read(cause: Throwable) extends TransportError
  final case class Write(cause: Throwable) extends TransportError
  case object EndOfStream extends TransportError

trait Transport:
  def read: ZStream[Any, TransportError, String]
  def write(line: String): UIO[Unit]
