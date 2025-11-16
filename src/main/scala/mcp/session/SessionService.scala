package mcp.session

import mcp.model.mcp.ClientInfo
import zio._

sealed trait SessionError

object SessionError:
  case object NotInitialized extends SessionError
  final case class InvalidVersion(requested: String, supported: String) extends SessionError

trait SessionService:
  def get: UIO[SessionState]
  def markInitialized(version: String, clientInfo: ClientInfo): UIO[Unit]
  def markClientAcknowledged(): UIO[Unit]
  def ensureInitialized(): IO[SessionError, Unit]

object SessionService:
  private var sessionState: SessionState = SessionState.initial

  val layer: ZLayer[Any, Nothing, SessionService] = ZLayer.succeed(new SessionService {
    def get: UIO[SessionState] = ZIO.succeed(sessionState)
    def markInitialized(version: String, clientInfo: ClientInfo): UIO[Unit] =
      ZIO.succeed {
        sessionState = sessionState.copy(
          initializeCompleted = true,
          protocolVersion = Some(version),
          clientInfo = Some(clientInfo)
        )
      }

    def markClientAcknowledged(): UIO[Unit] = ZIO.succeed {
      sessionState = sessionState.copy(clientAcknowledged = true)
    }

    def ensureInitialized(): IO[SessionError, Unit] =
      if sessionState.initializeCompleted && sessionState.clientAcknowledged then ZIO.unit
      else ZIO.fail(SessionError.NotInitialized)
  })
