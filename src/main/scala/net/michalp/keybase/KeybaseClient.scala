package net.michalp.keybase

import zio.Task
import zio.ZLayer
import zio.ZIO
import zio.Has
import net.michalp.keybase.transport.methods.Envelope

import io.circe.generic.auto._, io.circe.syntax._
import net.michalp.keybase.transport.methods.chat.Read
import net.michalp.keybase.transport.methods.chat.Send
import net.michalp.keybase.transport.methods.chat.Channel
import net.michalp.keybase.transport.methods.chat.{Message => TransportMessage}
import net.michalp.keybase.transport.methods.Params

object KeybaseClient {
  trait Service {
    def get(channel: String): Task[Seq[KeybaseClient.Message]]
    def send(channel: String, message: String): Task[Unit]
  }

  private val baseCommand = "keybase chat api"
  
  case class Message(body: String)
  def instance =  
    ZLayer.fromService { runtime: cmd.CmdRuntime.Service =>  
      new Service {
        def send(channel: String, message: String): zio.Task[Unit] = 
          runtime.spawn(
            baseCommand,
            Envelope.make(Send(Channel(channel),TransportMessage(message))).asJson.noSpaces
          ).map( r => println(r))
        
          // runtime.spawn(baseCommand, sendMsg(channel, message)).map( r => println(r))

        def get(channel: String): zio.Task[Seq[Message]] = 
          runtime
            .spawn(
              baseCommand,
              Envelope.make(Read(Channel(channel))).asJson.noSpaces
            )
            .map(_.split("\n").toSeq.map(Message.apply))

      }
    }
  
    def send(channel: String, msg: String): ZIO[Has[KeybaseClient.Service], Throwable, Unit] = 
      ZIO.accessM(_.get.send(channel, msg))
  
    def get(channel: String): ZIO[Has[KeybaseClient.Service], Throwable, Seq[Message]] = 
      ZIO.accessM(_.get.get(channel))
}