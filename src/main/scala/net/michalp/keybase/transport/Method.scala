package net.michalp.keybase.transport

import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import io.circe.Codec
import io.circe.Encoder
import io.circe.Decoder
object methods {
    implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

    final case class Params[T: Encoder](options: T)
    final case class Envelope[T: Encoder](method: String, params: Params[T])
    object Envelope {
        def make[T <: Options : Encoder](op: T): Envelope[T] = 
            Envelope(op.getClass.getSimpleName.toLowerCase, Params(op))
    }

    sealed trait Options extends Product with Serializable
    object chat {
        final case class Channel(name: String)
        final case class Message(body: String)
        final case class Send(channel: Channel, message: Message) extends Options
        object Send {
            implicit val encoder: Encoder[Send] = deriveConfiguredEncoder
        }
        final case class Pagination(num: Long)
        final case class Read(channel: Channel, pagination: Option[Pagination]=None) extends Options
        object Read {
            implicit val encoder: Encoder[Read] = deriveConfiguredEncoder
        }
        implicit val encodeOptions: Encoder[Options] = Encoder.instance {
            case x: Send => x.asJson
            case x: Read => x.asJson
        }

        // implicit val decodeOptions: Decoder[Options] =
        //     List[Decoder[Event]](
        //         Decoder[Send].widen,
        //         Decoder[Read].widen
        //     ).reduceLeft(_ or _)
    }
    implicit val encodeEnvelope: Encoder[Envelope[Options]] = deriveConfiguredEncoder
}