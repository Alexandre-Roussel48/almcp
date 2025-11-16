package mcp.config

import mcp.model.mcp.{ServerCapabilities, ToolsServerCapabilities}
import zio._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import mcp.model.mcp.given

final case class ServerConfig(
    protocolVersion: String,
    serverInfo: ServerInfo,
    capabilities: ServerCapabilities
)

final case class ServerInfo(
    name: String,
    title: String,
    version: String
)

object ServerConfig:
    val layer: ZLayer[Any, Nothing, ServerConfig] = ZLayer.succeed(
        ServerConfig(
            protocolVersion = "2025-06-18",
            serverInfo = ServerInfo(
                name    = "ALMCP",
                title   = "Arithmetic & Logic Model for Code Processing",
                version = "1.0.0"
            ),
            capabilities = ServerCapabilities(
                tools   = Some(ToolsServerCapabilities(false)),
                logging = None,
                resources = None,
                prompts = None
            )
        )
    )

given Encoder[ServerInfo] = deriveEncoder
given Decoder[ServerInfo] = deriveDecoder

given Encoder[ServerConfig] = deriveEncoder
given Decoder[ServerConfig] = deriveDecoder
