package mcp.model.jsonrpc

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto._

enum JsonRpcId:
  case StringId(value: String)
  case NumberId(value: Long)

given Encoder[JsonRpcId] = Encoder.instance {
  case JsonRpcId.StringId(v) => Json.fromString(v)
  case JsonRpcId.NumberId(v) => Json.fromLong(v)
}

given Decoder[JsonRpcId] = Decoder.instance { c =>
  c.as[String]
    .map(JsonRpcId.StringId.apply)
    .orElse(
      c.as[Long].map(JsonRpcId.NumberId.apply)
    )
}