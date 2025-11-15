package mcp.test

import io.circe._
import io.circe.syntax._
import mcp.config.ServerInfo
import mcp.model.jsonrpc.{JsonRpcError, JsonRpcId, JsonRpcMessage}
import mcp.model.mcp._
import mcp.model.mcp.given
import zio.test.Assertion._
import zio.test._

object JsonRpcCodecSpec extends ZIOSpecDefault:

  def spec: Spec[TestEnvironment, Any] =
    suite("JsonRpcCodecSpec")(
      test("encode and decode Request with StringId round-trips") {
        val params = JsonObject("x" -> Json.fromInt(1))
        val request = JsonRpcMessage.Request(
          method = "test/method",
          params = Some(params),
          id = JsonRpcId.StringId("abc")
        )

        val decoded = request.asJson.as[JsonRpcMessage.Request]
        assert(decoded)(isRight(equalTo(request)))
      },
      test("encode and decode Response with result round-trips") {
        val response = JsonRpcMessage.Response(
          id = JsonRpcId.NumberId(1),
          result = Some(Json.obj("value" -> Json.fromBoolean(true))),
          error = None
        )

        val decoded = response.asJson.as[JsonRpcMessage.Response]
        assert(decoded)(isRight(equalTo(response)))
      },
      test("encode and decode Response with error round-trips") {
        val error = JsonRpcError(code = -32001, message = "Custom error", data = Some(Json.obj("a" -> Json.fromInt(1))))
        val response = JsonRpcMessage.Response(
          id = JsonRpcId.StringId("err"),
          result = None,
          error = Some(error)
        )

        val decoded = response.asJson.as[JsonRpcMessage.Response]
        assert(decoded)(isRight(equalTo(response)))
      },
      test("decode distinguishes Request vs Notification based on id") {
        val params = JsonObject("flag" -> Json.fromBoolean(true))
        val requestJson = Json.obj(
          "jsonrpc" -> Json.fromString("2.0"),
          "method" -> Json.fromString("ping"),
          "params" -> Json.fromJsonObject(params),
          "id" -> Json.fromInt(42)
        )
        val notificationJson = Json.obj(
          "jsonrpc" -> Json.fromString("2.0"),
          "method" -> Json.fromString("ping"),
          "params" -> Json.fromJsonObject(params)
        )

        val decodedRequest = requestJson.as[JsonRpcMessage]
        val decodedNotification = notificationJson.as[JsonRpcMessage]

        assert(decodedRequest)(isRight(isSubtype[JsonRpcMessage.Request](anything))) &&
        assert(decodedNotification)(isRight(isSubtype[JsonRpcMessage.Notification](anything)))
      },
      test("InitializeParams and InitializeResult encode/decode round-trip") {
        val clientCaps = ClientCapabilities(
          roots = Some(RootCapabilities(listChanged = true)),
          sampling = Some(SamplingCapabilities()),
          elicitation = Some(ElicitationCapabilities())
        )
        val params = InitializeParams(
          protocolVersion = "2025-06-18",
          capabilities = clientCaps,
          clientInfo = ClientInfo(name = "test", title = Some("Test Client"), version = "0.1.0")
        )

        val serverCaps = ServerCapabilities(
          tools = Some(ToolsServerCapabilities(listChanged = false)),
          logging = Some(LoggingCapabilities()),
          resources = Some(ResourcesCapabilities(subscribe = true, listChanged = true)),
          prompts = Some(PromptsCapabilities(listChanged = false))
        )
        val result = InitializeResult(
          protocolVersion = "2025-06-18",
          capabilities = serverCaps,
          serverInfo = ServerInfo("almcp", "Arithmetic & Logic", "1.0.0"),
          instructions = Some("Ready")
        )

        val decodedParams = params.asJson.as[InitializeParams]
        val decodedResult = result.asJson.as[InitializeResult]

        assert(decodedParams)(isRight(equalTo(params))) &&
        assert(decodedResult)(isRight(equalTo(result)))
      },
      test("ToolCallParams and ToolCallResult encode/decode round-trip") {
        val params = ToolCallParams(
          name = "add",
          arguments = JsonObject("a" -> Json.fromInt(1), "b" -> Json.fromInt(2))
        )
        val result = ToolCallResult(content = List(ToolResultContent.text("3")))

        val decodedParams = params.asJson.as[ToolCallParams]
        val decodedResult = result.asJson.as[ToolCallResult]

        assert(decodedParams)(isRight(equalTo(params))) &&
        assert(decodedResult)(isRight(equalTo(result)))
      }
    )
