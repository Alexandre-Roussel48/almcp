package mcp.model.mcp

import io.circe.{Decoder, Encoder, JsonObject}
import io.circe.generic.semiauto._

final case class ClientCapabilities(
    roots: Option[RootCapabilities],
    sampling: Option[SamplingCapabilities],
    elicitation: Option[ElicitationCapabilities]
)

final case class ServerCapabilities(
    tools: Option[ToolsServerCapabilities],
    logging: Option[LoggingCapabilities],
    resources: Option[ResourcesCapabilities],
    prompts: Option[PromptsCapabilities]
)

final case class ToolsServerCapabilities(listChanged: Boolean)

final case class LoggingCapabilities()

final case class ResourcesCapabilities(subscribe: Boolean, listChanged: Boolean)

final case class PromptsCapabilities(listChanged: Boolean)

final case class RootCapabilities(listChanged: Boolean)

final case class SamplingCapabilities()

final case class ElicitationCapabilities()

private def dropCapabilityNulls(obj: JsonObject): JsonObject =
  JsonObject.fromIterable(obj.toIterable.filterNot(_._2.isNull))

given Encoder.AsObject[ClientCapabilities] = deriveEncoder[ClientCapabilities].mapJsonObject(dropCapabilityNulls)

given Decoder[ClientCapabilities] = deriveDecoder

given Encoder.AsObject[ServerCapabilities] = deriveEncoder[ServerCapabilities].mapJsonObject(dropCapabilityNulls)

given Decoder[ServerCapabilities] = deriveDecoder

given Encoder[ToolsServerCapabilities] = deriveEncoder

given Decoder[ToolsServerCapabilities] = deriveDecoder

given Encoder[LoggingCapabilities] = deriveEncoder

given Decoder[LoggingCapabilities] = deriveDecoder

given Encoder[ResourcesCapabilities] = deriveEncoder

given Decoder[ResourcesCapabilities] = deriveDecoder

given Encoder[PromptsCapabilities] = deriveEncoder

given Decoder[PromptsCapabilities] = deriveDecoder

given Encoder[RootCapabilities] = deriveEncoder

given Decoder[RootCapabilities] = deriveDecoder

given Encoder[SamplingCapabilities] = deriveEncoder

given Decoder[SamplingCapabilities] = deriveDecoder

given Encoder[ElicitationCapabilities] = deriveEncoder

given Decoder[ElicitationCapabilities] = deriveDecoder
