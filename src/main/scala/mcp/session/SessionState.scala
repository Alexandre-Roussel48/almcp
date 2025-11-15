package mcp.session

import mcp.model.mcp.ClientInfo

final case class SessionState(
    initializeCompleted: Boolean,
    clientAcknowledged: Boolean,
    protocolVersion: Option[String],
    clientInfo: Option[ClientInfo]
)

object SessionState:
  val initial: SessionState = SessionState(
    initializeCompleted = false,
    clientAcknowledged = false,
    protocolVersion = None,
    clientInfo = None
  )
