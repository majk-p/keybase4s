package net.michalp.keybase.transport

import io.circe.generic.extras.auto._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.Codec
import io.circe.Encoder
object methods {
    implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

    final case class Params[T: Encoder](options: T)
    final case class Envelope[T: Encoder](method: String, params: Params[T])
    object Envelope {
        def make[T: Encoder](op: T): Envelope[T] = 
            Envelope(op.getClass.getSimpleName.toLowerCase, Params(op))
    }

    sealed trait Options extends Product with Serializable
    object chat {
        final case class Channel(name: String)
        final case class Message(body: String)
        final case class Send(channel: Channel, message: Message) extends Options
        final case class Pagination(num: Long)
        final case class Read(channel: Channel, pagination: Option[Pagination]=None) extends Options
    }
}

object results {
    final case class Envelope[T: Decoder](result: T)

}