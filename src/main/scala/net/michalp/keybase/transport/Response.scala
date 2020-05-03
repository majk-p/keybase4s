package net.michalp.keybase.transport

import io.circe.Decoder
import cats.syntax.functor._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

object response {
    implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
    sealed trait Response extends Product with Serializable
    final case class Envelope[T <:Response : Decoder](result: T)
    final case class Read(messages: List[MessageWrapper]) extends Response
    object Read {
        implicit val decoder: Decoder[Read] = deriveConfiguredDecoder
    }

    final case class ChannelInfo(name: String, membersType: String, topicType: String)
    final case class SenderInfo(uid: String, username: String, deviceId: String, deviceName: String)
    final case class MessageContentText(body: String)
    final case class MessageContent(`type`: String, text: MessageContentText)
    final case class MessageWrapper(msg: Message)
    object MessageWrapper {
        implicit val decoder: Decoder[MessageWrapper] = deriveConfiguredDecoder
    }
    final case class Message(id: Long, conversationId: String, channel: ChannelInfo, content: MessageContent, unread: Boolean)
    object Message {
        implicit val decoder: Decoder[Message] = deriveConfiguredDecoder
    }
    
    implicit val decodeResponse: Decoder[Response] =
        List[Decoder[Response]](
            Decoder[Read].widen
            // Decoder[Message].widen
        ).reduceLeft(_ or _)
    implicit val decodeEnvelope: Decoder[Envelope[Response]] = deriveConfiguredDecoder
}