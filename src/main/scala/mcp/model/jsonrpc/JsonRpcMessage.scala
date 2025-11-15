package mcp.model.jsonrpc

import io.circe.{Decoder, Encoder, Json, JsonObject}
import io.circe.generic.semiauto._
import io.circe.syntax._

sealed trait JsonRpcMessage:
  def jsonrpc: String

object JsonRpcMessage:
  final case class Request(
      method: String,
      params: Option[JsonObject],
      id: JsonRpcId,
      jsonrpc: String = "2.0"
  ) extends JsonRpcMessage

  final case class Notification(
      method: String,
      params: Option[JsonObject],
      jsonrpc: String = "2.0"
  ) extends JsonRpcMessage

  final case class Response(
      id: JsonRpcId,
      result: Option[Json],
      error: Option[JsonRpcError],
      jsonrpc: String = "2.0"
  ) extends JsonRpcMessage

  given Encoder[Request] = deriveEncoder

  given Decoder[Request] = deriveDecoder

  given Encoder[Notification] = deriveEncoder

  given Decoder[Notification] = deriveDecoder

  given Encoder.AsObject[Response] = Encoder.AsObject.instance { resp =>
    val baseFields = List(
      "jsonrpc" -> Json.fromString(resp.jsonrpc),
      "id" -> resp.id.asJson
    )
    val withResult = resp.result match
      case Some(value) => ("result", value) :: baseFields
      case None        => baseFields
    val withError = resp.error match
      case Some(err) => ("error", err.asJson) :: withResult
      case None      => withResult

    JsonObject.fromIterable(withError.reverse)
  }

  given Decoder[Response] = deriveDecoder

  given Decoder[JsonRpcMessage] = Decoder.instance { c =>
    c.downField("id").as[JsonRpcId].map(Some(_)).orElse(Right(None)).flatMap {
      case Some(id) =>
        // Response or Request
        val hasResult = c.downField("result").success.isDefined
        val hasError  = c.downField("error").success.isDefined

        if hasResult || hasError then
          c.as[JsonRpcMessage.Response]
        else
          c.as[JsonRpcMessage.Request]

      case None =>
        c.as[JsonRpcMessage.Notification]
    }
  }
