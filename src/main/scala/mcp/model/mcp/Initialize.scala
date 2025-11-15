package mcp.model.mcp

import io.circe.{Decoder, Encoder, JsonObject}
import mcp.config.ServerInfo
import io.circe.generic.semiauto._
import mcp.config.given

final case class InitializeParams(
    protocolVersion: String,
    capabilities: ClientCapabilities,
    clientInfo: ClientInfo
)

final case class InitializeResult(
    protocolVersion: String,
    capabilities: ServerCapabilities,
    serverInfo: ServerInfo,
    instructions: Option[String]
)

final case class ClientInfo(
    name: String,
    title: Option[String],
    version: String
)

given Encoder[InitializeParams] = deriveEncoder

given Decoder[InitializeParams] = deriveDecoder

private def dropInitializeNulls(obj: JsonObject): JsonObject =
  JsonObject.fromIterable(obj.toIterable.filterNot(_._2.isNull))

given Encoder.AsObject[InitializeResult] = deriveEncoder[InitializeResult].mapJsonObject(dropInitializeNulls)

given Decoder[InitializeResult] = deriveDecoder

given Encoder[ClientInfo] = deriveEncoder

given Decoder[ClientInfo] = deriveDecoder
