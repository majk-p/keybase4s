package net.michalp.keybase

import zio.Task
import zio.ZLayer
import zio.ZIO
import zio.Has
import net.michalp.keybase.transport.methods.Envelope

import io.circe.generic.auto._, io.circe.syntax._
import io.circe._
import io.circe.parser._
import net.michalp.keybase.transport.methods.chat.Read
import net.michalp.keybase.transport.methods.chat.Send
import net.michalp.keybase.transport.methods.chat.Channel
import net.michalp.keybase.transport.methods.chat.Pagination
import net.michalp.keybase.transport.methods.chat.{Message => TransportMessage}
import net.michalp.keybase.transport.methods.Params
import net.michalp.keybase.transport.response

object KeybaseClient {
  
  trait Service {
    def get(channel: String): Task[response.Read]
    def send(channel: String, message: String): Task[Unit]
    def init(user: String, paperKey: String): Task[Unit]
    def listen: zio.stream.Stream[Throwable, response.MessageWrapper]
  }

  private val baseCommand = "keybase chat api"
  
  case class Message(body: String)
  def instance =  
    ZLayer.fromService { runtime: cmd.CmdRuntime.Service =>  
      new Service {

        def listen: zio.stream.Stream[Throwable,response.MessageWrapper] =
          runtime.listen(Seq("keybase", "chat", "api-listen")).mapM{s =>
            ZIO.fromEither(
              decode[response.MessageWrapper](s)
            )
          }

        def send(channel: String, message: String): zio.Task[Unit] = 
          runtime.spawn(
            baseCommand,
            Envelope.make(Send(Channel(channel),TransportMessage(message))).asJson.noSpaces
          ).unit

        def get(channel: String): zio.Task[response.Read] = for {
          result <- runtime
            .spawn(
              baseCommand,
              Envelope.make(
                Read(Channel(channel), Some(Pagination(1)))
              ).asJson.noSpaces
            )
          parsed <- ZIO.fromEither(
            decode[response.Envelope[response.Read]](result)
          )
        } yield parsed.result

        def init(user: String, paperKey: String): zio.Task[Unit] = 
          runtime.spawn(Seq("keybase", "oneshot", "-u", user), paperKey).unit
      }
    }
  
    def send(channel: String, msg: String): ZIO[Has[KeybaseClient.Service], Throwable, Unit] = 
      ZIO.accessM(_.get.send(channel, msg))
  
    def get(channel: String): ZIO[Has[KeybaseClient.Service], Throwable, response.Read] = 
      ZIO.accessM(_.get.get(channel))
  
    def listen: ZIO[Has[KeybaseClient.Service], Throwable, zio.stream.Stream[Throwable,response.MessageWrapper]] = 
      ZIO.access(_.get.listen)
  
    def init(user: String, paperKey: String): ZIO[Has[KeybaseClient.Service], Throwable, Unit] = 
      ZIO.accessM(_.get.init(user, paperKey))     
  
}