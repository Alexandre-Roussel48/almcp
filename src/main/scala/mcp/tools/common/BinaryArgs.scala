package mcp.tools.common

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class BinaryArgs(a: Int, b: Int)

object BinaryArgs:
  given Decoder[BinaryArgs] = deriveDecoder
