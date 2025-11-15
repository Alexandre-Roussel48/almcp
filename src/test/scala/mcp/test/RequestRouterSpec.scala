package mcp.test

import io.circe.{Decoder, Encoder, Json, JsonObject}
import io.circe.syntax._
import mcp.config.ServerConfig
import mcp.model.jsonrpc.{JsonRpcError, JsonRpcId, JsonRpcMessage}
import mcp.model.mcp._
import mcp.model.mcp.given
import mcp.router.{InitializeHandler, RequestRouter, ToolsHandler}
import mcp.session.SessionService
import mcp.tools.ToolRegistry
import zio._
import zio.test.Assertion._
import zio.test._

object RequestRouterSpec extends ZIOSpecDefault:

  private def routerLayer =
    ZLayer.make[RequestRouter & SessionService & ServerConfig & ToolRegistry](
      ServerConfig.layer,
      ToolRegistry.layer,
      TestSessionLayer.make(),
      InitializeHandler.layer,
      ToolsHandler.layer,
      RequestRouter.layer
    )

  private def withRouterEnv[A](
      effect: ZIO[RequestRouter & SessionService & ServerConfig & ToolRegistry, Any, A]
  ): ZIO[Any, Any, A] =
    effect.provideLayer(routerLayer)

  private def paramsObject[A: Encoder](value: A): JsonObject =
    value.asJson.asObject.getOrElse(JsonObject.empty)

  private def request(id: Int, method: String, params: Option[JsonObject]): JsonRpcMessage.Request =
    JsonRpcMessage.Request(
      method = method,
      params = params,
      id = JsonRpcId.NumberId(id.toLong)
    )

  private def decodeResult[A: Decoder](response: JsonRpcMessage.Response): UIO[A] =
    ZIO
      .fromOption(response.result)
      .orElseFail(new RuntimeException("expected result"))
      .flatMap(json => ZIO.fromEither(json.as[A]))
      .orDie

  private def expectResponse(opt: Option[JsonRpcMessage.Response]): UIO[JsonRpcMessage.Response] =
    ZIO
      .fromOption(opt)
      .orElseFail(new RuntimeException("expected response"))
      .orDie

  def spec: Spec[TestEnvironment, Any] =
    suite("RequestRouterSpec")(
      test("initialize with supported protocol returns success and marks session initialized") {
        withRouterEnv {
          for
            config  <- ZIO.service[ServerConfig]
            router  <- ZIO.service[RequestRouter]
            session <- ZIO.service[SessionService]
            params: InitializeParams =
              InitializeParams(
                protocolVersion = config.protocolVersion,
                capabilities = ClientCapabilities(None, None, None),
                clientInfo = ClientInfo("client", Some("Test Client"), "0.0.1")
              )
            req       = request(1, "initialize", Some(paramsObject[InitializeParams](params)))
            response <- router.handle(req).flatMap(expectResponse)
            result   <- decodeResult[InitializeResult](response)
            state    <- session.get
          yield assertTrue(
            response.id == req.id,
            result.protocolVersion == config.protocolVersion,
            state.initializeCompleted,
            state.protocolVersion.contains(config.protocolVersion),
            state.clientInfo.exists(_.name == "client")
          )
        }
      },
      test("initialize with unsupported protocol returns error") {
        withRouterEnv {
          for
            router <- ZIO.service[RequestRouter]
            params: InitializeParams =
              InitializeParams(
                protocolVersion = "1.0.0",
                capabilities = ClientCapabilities(None, None, None),
                clientInfo = ClientInfo("client", Some("Test Client"), "0.0.1")
              )
            req       = request(2, "initialize", Some(paramsObject[InitializeParams](params)))
            response <- router.handle(req).flatMap(expectResponse)
            error     = response.error
          yield assertTrue(error.isDefined) &&
            assertTrue(error.exists(_.code == JsonRpcError.InvalidParams.code)) &&
            assertTrue(error.exists(_.message.contains("Unsupported protocol version")))
        }
      },
      test("tools/list before initialize returns InvalidRequest error") {
        withRouterEnv {
          for
            router   <- ZIO.service[RequestRouter]
            listReq   = request(3, "tools/list", None)
            response <- router.handle(listReq).flatMap(expectResponse)
          yield assertTrue(response.error.exists(_.code == JsonRpcError.InvalidRequest.code))
        }
      },
      test("full flow: initialize -> acknowledge -> tools/list -> tools/call/add") {
        withRouterEnv {
          for
            config  <- ZIO.service[ServerConfig]
            router  <- ZIO.service[RequestRouter]
            params: InitializeParams =
              InitializeParams(
                protocolVersion = config.protocolVersion,
                capabilities = ClientCapabilities(None, None, None),
                clientInfo = ClientInfo("client", Some("Test Client"), "0.0.1")
              )
            initReq   = request(4, "initialize", Some(paramsObject[InitializeParams](params)))
            _        <- router.handle(initReq).flatMap(expectResponse)
            _        <- router.handle(JsonRpcMessage.Notification("notifications/initialized", None))
            listReq   = request(5, "tools/list", None)
            listResp <- router.handle(listReq).flatMap(expectResponse)
            listRes  <- decodeResult[ToolsListResult](listResp)
            tools      = listRes.tools.map(_.name).toSet
            callParams = ToolCallParams(
                           name = "add",
                           arguments = JsonObject("a" -> Json.fromInt(2), "b" -> Json.fromInt(3))
                         )
            callReq    = request(6, "tools/call", Some(paramsObject[ToolCallParams](callParams)))
            callResp  <- router.handle(callReq).flatMap(expectResponse)
            callRes   <- decodeResult[ToolCallResult](callResp)
          yield assertTrue(
            tools.contains("add"),
            callRes.content == List(ToolResultContent.text("5"))
          )
        }
      },
      test("unknown method returns MethodNotFound error") {
        withRouterEnv {
          for
            router   <- ZIO.service[RequestRouter]
            req       = request(7, "bogus/method", None)
            response <- router.handle(req).flatMap(expectResponse)
            error     = response.error
          yield assertTrue(error.exists(_.code == JsonRpcError.MethodNotFound.code))
        }
      },
      test("notifications/initialized does not produce a response") {
        withRouterEnv {
          for
            router <- ZIO.service[RequestRouter]
            notif   = JsonRpcMessage.Notification("notifications/initialized", None)
            result <- router.handle(notif)
          yield assertTrue(result.isEmpty)
        }
      }
    )
