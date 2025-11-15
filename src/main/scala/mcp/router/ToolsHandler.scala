package mcp.router

import io.circe.syntax._
import io.circe.{JsonObject, Json}
import mcp.model.jsonrpc.{JsonRpcError, JsonRpcMessage}
import mcp.model.mcp.{
  ToolCallParams,
  ToolCallResult,
  ToolResultContent,
  ToolsListResult,
  given
}
import mcp.session.SessionService
import mcp.tools.ToolRegistry
import zio._

trait ToolsHandler:
  def handleList(req: JsonRpcMessage.Request): ZIO[Any, JsonRpcError, JsonRpcMessage.Response]
  def handleCall(req: JsonRpcMessage.Request): ZIO[Any, JsonRpcError, JsonRpcMessage.Response]

object ToolsHandler:
  val layer: ZLayer[ToolRegistry & SessionService, Nothing, ToolsHandler] =
    ZLayer.fromFunction { (registry: ToolRegistry, sessionService: SessionService) =>
      new ToolsHandler {
        def handleList(req: JsonRpcMessage.Request): ZIO[Any, JsonRpcError, JsonRpcMessage.Response] =
          for
            _          <- ensureSession(sessionService)
            tools      <- registry.listTools()
            resultObj    = ToolsListResult(tools)
            jsonResult   = resultObj.asJson
          yield JsonRpcMessage.Response(
            id = req.id,
            result = Some(jsonResult),
            error = None
          )

        def handleCall(req: JsonRpcMessage.Request): ZIO[Any, JsonRpcError, JsonRpcMessage.Response] =
          for
            _           <- ensureSession(sessionService)
            paramsObj   <- ZIO.fromOption(req.params).orElseFail(JsonRpcError.InvalidParams)
            callParams  <- decodeCallParams(paramsObj)
            tool        <- registry.getTool(callParams.name).mapError(ErrorMapping.fromToolError)
            toolResult  <- tool.execute(callParams.arguments).mapError(ErrorMapping.fromToolError)
            resultObj    = ToolCallResult(List(toTextContent(toolResult)))
            jsonResult   = resultObj.asJson
          yield JsonRpcMessage.Response(
            id = req.id,
            result = Some(jsonResult),
            error = None
          )
      }
    }

  private def ensureSession(sessionService: SessionService): IO[JsonRpcError, Unit] =
    sessionService.ensureInitialized().mapError(_ => JsonRpcError.InvalidRequest)

  private def toTextContent(json: Json): ToolResultContent =
    ToolResultContent.text(json.asString.getOrElse(json.spaces2))

  private def decodeCallParams(params: JsonObject): IO[JsonRpcError, ToolCallParams] =
    ZIO
      .fromEither(Json.fromJsonObject(params).as[ToolCallParams])
      .mapError(_ => JsonRpcError.InvalidParams)
