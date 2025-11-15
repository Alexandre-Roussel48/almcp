package mcp.router

import io.circe.Json
import io.circe.syntax._
import mcp.config.ServerConfig
import mcp.model.jsonrpc.{JsonRpcError, JsonRpcMessage}
import mcp.model.mcp.{InitializeParams, InitializeResult}
import mcp.model.mcp.given
import mcp.session.{SessionError, SessionService}
import zio._

trait InitializeHandler:
  def handle(req: JsonRpcMessage.Request): ZIO[Any, JsonRpcError, JsonRpcMessage.Response]

object InitializeHandler:
  val layer: ZLayer[ServerConfig & SessionService, Nothing, InitializeHandler] =
    ZLayer.fromFunction { (config: ServerConfig, sessionService: SessionService) =>
      new InitializeHandler {
        def handle(req: JsonRpcMessage.Request): ZIO[Any, JsonRpcError, JsonRpcMessage.Response] = {
          for {
            paramsObj <- ZIO.fromOption(req.params).orElseFail(JsonRpcError.InvalidParams)
            params    <- ZIO
                           .fromEither(Json.fromJsonObject(paramsObj).as[InitializeParams])
                           .mapError(_ => JsonRpcError.InvalidParams)
            _         <- ensureNotAlreadyInitialized(sessionService)
            response  <- buildResponse(req, params, config, sessionService)
          } yield response
        }
      }
    }

  private def buildResponse(
      req: JsonRpcMessage.Request,
      params: InitializeParams,
      config: ServerConfig,
      sessionService: SessionService
  ): ZIO[Any, JsonRpcError, JsonRpcMessage.Response] =
    val clientVersion = params.protocolVersion
    val serverVersion = config.protocolVersion

    if clientVersion != serverVersion then
      ZIO.fail(
        JsonRpcError.InvalidParams.copy(
          message = "Unsupported protocol version",
          data = Some(
            Json.obj(
              "supported" -> Json.fromString(serverVersion),
              "requested" -> Json.fromString(clientVersion)
            )
          )
        )
      )
    else
      val result = InitializeResult(
        protocolVersion = serverVersion,
        capabilities = config.capabilities,
        serverInfo = config.serverInfo,
        instructions = None
      )
      
      for
        _ <- sessionService.markInitialized(serverVersion, params.clientInfo)
      yield JsonRpcMessage.Response(
        id = req.id,
        result = Some(result.asJson),
        error = None
      )

  private def ensureNotAlreadyInitialized(sessionService: SessionService): ZIO[Any, JsonRpcError, Unit] =
    sessionService.ensureInitialized().either.flatMap {
      case Right(_) =>
        ZIO.fail(JsonRpcError.InvalidRequest.copy(message = "Already initialized"))
      case Left(SessionError.NotInitialized) =>
        ZIO.unit
      case Left(err) =>
        ZIO.fail(ErrorMapping.fromSessionError(err))
    }
