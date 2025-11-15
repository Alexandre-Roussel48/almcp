package mcp.test

import mcp.model.mcp.ClientInfo
import mcp.session.{SessionError, SessionService}
import zio._
import zio.test.Assertion._
import zio.test._

object SessionServiceSpec extends ZIOSpecDefault:

  private val testClient = ClientInfo(name = "cli", title = Some("CLI Tester"), version = "0.0.1")

  private def withSession[A](effect: ZIO[SessionService, Any, A]): ZIO[Any, Any, A] =
    effect.provideLayer(TestSessionLayer.make())

  def spec: Spec[TestEnvironment, Any] =
    suite("SessionServiceSpec")(
      test("initial state is not initialized") {
        withSession {
          for
            service <- ZIO.service[SessionService]
            state   <- service.get
          yield assertTrue(
            state.initializeCompleted == false,
            state.clientAcknowledged == false,
            state.protocolVersion.isEmpty,
            state.clientInfo.isEmpty
          )
        }
      },
      test("markInitialized updates the state") {
        withSession {
          for
            service <- ZIO.service[SessionService]
            _       <- service.markInitialized("2025-06-18", testClient)
            state   <- service.get
          yield assertTrue(
            state.initializeCompleted,
            !state.clientAcknowledged,
            state.protocolVersion.contains("2025-06-18"),
            state.clientInfo.contains(testClient)
          )
        }
      },
      test("ensureInitialized fails when not initialized") {
        withSession {
          for
            service <- ZIO.service[SessionService]
            result  <- service.ensureInitialized().either
          yield assert(result)(isLeft(equalTo(SessionError.NotInitialized)))
        }
      },
      test("ensureInitialized succeeds only after client acknowledgement") {
        withSession {
          for
            service <- ZIO.service[SessionService]
            _       <- service.markInitialized("2025-06-18", testClient)
            interim <- service.ensureInitialized().either
            _       <- service.markClientAcknowledged()
            finalR  <- service.ensureInitialized().either
          yield assert(interim)(isLeft(equalTo(SessionError.NotInitialized))) &&
            assert(finalR)(isRight(isUnit))
        }
      }
    )
