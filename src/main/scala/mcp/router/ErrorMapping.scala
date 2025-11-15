package mcp.router

import io.circe.Json
import mcp.model.jsonrpc.JsonRpcError
import mcp.session.SessionError
import mcp.tools.ToolError

object ErrorMapping:
  def fromSessionError(e: SessionError): JsonRpcError = e match
    case SessionError.NotInitialized =>
      JsonRpcError.InvalidRequest.copy(message = "Session not initialized")
    case SessionError.InvalidVersion(requested, supported) =>
      JsonRpcError.InvalidRequest.copy(
        message = "Unsupported protocol version",
        data = Some(
          Json.obj(
            "requested" -> Json.fromString(requested),
            "supported" -> Json.fromString(supported)
          )
        )
      )

  def fromToolError(e: ToolError): JsonRpcError = e match
    case ToolError.ToolNotFound(name) =>
      JsonRpcError.MethodNotFound.copy(
        data = Some(Json.obj("tool" -> Json.fromString(name)))
      )
    case ToolError.InvalidArguments(msg) =>
      JsonRpcError.InvalidParams.copy(
        data = Some(Json.obj("reason" -> Json.fromString(msg)))
      )
    case ToolError.ExecutionFailed(msg) =>
      JsonRpcError.InternalError.copy(
        data = Some(Json.obj("reason" -> Json.fromString(msg)))
      )

  def unknownMethod(method: String): JsonRpcError =
    JsonRpcError.MethodNotFound.copy(
      data = Some(Json.obj("method" -> Json.fromString(method)))
    )
