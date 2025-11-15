package mcp.test

import mcp.model.mcp.ClientInfo
import mcp.session.{SessionError, SessionService, SessionState}
import zio._

object TestSessionLayer:

  def make(initial: SessionState = SessionState.initial): ULayer[SessionService] =
    ZLayer.fromZIO(
      Ref.make(initial).map { ref =>
        new SessionService {
          def get: UIO[SessionState] = ref.get

          def markInitialized(version: String, clientInfo: ClientInfo): UIO[Unit] =
            ref
              .update(
                _.copy(
                  initializeCompleted = true,
                  protocolVersion = Some(version),
                  clientInfo = Some(clientInfo)
                )
              )
              .unit

          def markClientAcknowledged(): UIO[Unit] =
            ref.update(_.copy(clientAcknowledged = true)).unit

          def ensureInitialized(): IO[SessionError, Unit] =
            ref.get.flatMap { state =>
              if state.initializeCompleted && state.clientAcknowledged then ZIO.unit
              else ZIO.fail(SessionError.NotInitialized)
            }
        }
      }
    )
