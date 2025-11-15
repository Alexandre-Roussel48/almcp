package mcp.model.jsonrpc

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto._

final case class JsonRpcError(
    code: Int,
    message: String,
    data: Option[Json]
)

given Encoder[JsonRpcError] = deriveEncoder

given Decoder[JsonRpcError] = deriveDecoder

object JsonRpcError:
  val ParseError: JsonRpcError = JsonRpcError(-32700, "Parse Error", None)
  val InvalidRequest: JsonRpcError = JsonRpcError(-32600, "Invalid Request", None)
  val MethodNotFound: JsonRpcError = JsonRpcError(-32601, "Method Not Found", None)
  val InvalidParams: JsonRpcError = JsonRpcError(-32602, "Invalid Params", None)
  val InternalError: JsonRpcError = JsonRpcError(-32603, "Internal Error", None)
  val ServerError: JsonRpcError = JsonRpcError(-32000, "Server Error", None)
