package mcp.model.mcp

import io.circe.{Decoder, Encoder, Json, JsonObject}
import io.circe.generic.semiauto._

final case class ToolDefinition(
    name: String,
    description: String,
    parameters: JsonObject
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

given Encoder[ToolDefinition] = deriveEncoder

given Decoder[ToolDefinition] = deriveDecoder

given Encoder[ToolsListResult] = deriveEncoder

given Decoder[ToolsListResult] = deriveDecoder

given Encoder[ToolCallParams] = deriveEncoder

given Decoder[ToolCallParams] = deriveDecoder

given Encoder[ToolResultContent] = deriveEncoder

given Decoder[ToolResultContent] = deriveDecoder

given Encoder[ToolCallResult] = deriveEncoder

given Decoder[ToolCallResult] = deriveDecoder
