package mcp

import zio._
import mcp.transport.{Transport, StdioTransport}
import mcp.session.SessionService
import mcp.config.ServerConfig
import mcp.tools.ToolRegistry
import mcp.router.{InitializeHandler, RequestRouter, ToolsHandler}
import mcp.model.jsonrpc.JsonRpcMessage
import io.circe.syntax.*
import io.circe.parser.decode

object Main extends ZIOAppDefault:
  val appLayer: ZLayer[Any, Throwable, Transport & RequestRouter] =
    ZLayer.make[Transport & RequestRouter](
    ServerConfig.layer,
    SessionService.layer,
    ToolRegistry.layer,
    InitializeHandler.layer,
    ToolsHandler.layer,
    RequestRouter.layer,
    StdioTransport.layer
  )

  override def run: ZIO[Any, Throwable, Unit] =
    val program =
      for
        transport <- ZIO.service[Transport]
        router    <- ZIO.service[RequestRouter]
        _ <- loop(transport, router)
      yield ()

    program.provide(appLayer)

  private def loop(
      transport: Transport,
      router: RequestRouter
  ): ZIO[Any, Throwable, Unit] =
    transport.read.runForeach { line =>
      decode[JsonRpcMessage](line) match
        case Left(_) =>
          ZIO.unit
        case Right(msg) =>
          router.handle(msg).flatMap {
            case Some(response) =>
              val json = response.asJson.noSpaces
              transport.write(json)
            case None =>
              ZIO.unit
          }
    }
