package mcp.model.mcp

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, Json, JsonObject}

final case class JsonSchema(
    `type`: String = "object",
    properties: Option[JsonObject] = None,
    required: Option[List[String]] = None
)

final case class ToolDefinition(
    name: String,
    inputSchema: JsonSchema,
    description: Option[String] = None,
    outputSchema: Option[JsonSchema] = None,
    title: Option[String] = None
)

final case class ToolsListResult(
    tools: List[ToolDefinition]
)

final case class ToolCallParams(
    name: String,
    arguments: JsonObject
)

final case class ToolResultContent(
    `type`: String,
    text: String
)

object ToolResultContent:
  def text(value: String): ToolResultContent =
    ToolResultContent(`type` = "text", text = value)

final case class ToolCallResult(
    content: List[ToolResultContent]
)

given Encoder.AsObject[JsonSchema] = deriveEncoder[JsonSchema].mapJsonObject(dropNullFields)

given Decoder[JsonSchema] = deriveDecoder

given Encoder.AsObject[ToolDefinition] = deriveEncoder[ToolDefinition].mapJsonObject(dropNullFields)

given Decoder[ToolDefinition] = deriveDecoder

given Encoder[ToolsListResult] = deriveEncoder

given Decoder[ToolsListResult] = deriveDecoder

given Encoder[ToolCallParams] = deriveEncoder

given Decoder[ToolCallParams] = deriveDecoder

given Encoder[ToolResultContent] = deriveEncoder

given Decoder[ToolResultContent] = deriveDecoder

given Encoder[ToolCallResult] = deriveEncoder

given Decoder[ToolCallResult] = deriveDecoder

private def dropNullFields(obj: JsonObject): JsonObject =
  JsonObject.fromIterable(obj.toIterable.filterNot(_._2.isNull))
