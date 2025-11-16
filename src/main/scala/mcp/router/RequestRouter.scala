package mcp.router

import mcp.model.jsonrpc.{JsonRpcError, JsonRpcId, JsonRpcMessage}
import mcp.router.ErrorMapping
import mcp.session.SessionService
import zio._

trait RequestRouter:
  def handle(msg: JsonRpcMessage): ZIO[Any, Nothing, Option[JsonRpcMessage.Response]]

object RequestRouter:
  val layer: ZLayer[InitializeHandler & ToolsHandler & SessionService, Nothing, RequestRouter] =
    ZLayer.fromFunction {
      (
          initHandler: InitializeHandler,
          toolsHandler: ToolsHandler,
          sessionService: SessionService
      ) =>
      new RequestRouter {
        def handle(msg: JsonRpcMessage): ZIO[Any, Nothing, Option[JsonRpcMessage.Response]] =
          msg match
            case req: JsonRpcMessage.Request =>
              req.method match
                case "initialize" =>
                  respond(initHandler.handle(req), req.id)

                case "tools/list" =>
                  respond(toolsHandler.handleList(req), req.id)

                case "tools/call" =>
                  respond(toolsHandler.handleCall(req), req.id)

                case other =>
                  ZIO.succeed(Some(errorResponse(req.id, ErrorMapping.unknownMethod(other))))

            case notif: JsonRpcMessage.Notification =>
              notif.method match
                case "notifications/initialized" =>
                  sessionService.markClientAcknowledged().as(None)
                case _ =>
                  ZIO.none

            case _: JsonRpcMessage.Response =>
              ZIO.none

        private def respond(
            effect: ZIO[Any, JsonRpcError, JsonRpcMessage.Response],
            id: JsonRpcId
        ): ZIO[Any, Nothing, Option[JsonRpcMessage.Response]] =
          effect
            .map(response => Some(response))
            .catchAll(err => ZIO.succeed(Some(errorResponse(id, err))))

        private def errorResponse(id: JsonRpcId, err: JsonRpcError): JsonRpcMessage.Response =
          JsonRpcMessage.Response(
            id = id,
            result = None,
            error = Some(err)
          )
      }
    }
