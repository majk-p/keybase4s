package net.michalp.keybase

import zio.Task
import zio.ZLayer
import zio.ZIO
import zio.Has

object KeybaseClient {
  trait Service {
    def get(channel: String): Task[Seq[KeybaseClient.Message]]
    def send(channel: String, message: String): Task[Unit]
  }

  private val baseCommand = "keybase chat api"

  private def sendMsg(channel: String, body: String) = 
    s"""{"method": "send","params": {"options": {"channel": {"name": "${channel}"},"message": {"body": "${body}"}}}}"""

  private def readConversation(channel: String) = 
    s"""{"method": "read", "params": {"options": {"channel": {"name": "${channel}"}}}}"""

  case class Message(body: String)
  def instance =  
    ZLayer.fromService { runtime: cmd.CmdRuntime.Service =>  
      new Service {

        def send(channel: String, message: String): zio.Task[Unit] = 
          runtime.spawn(baseCommand, sendMsg(channel, message)).map( r => println(r))

        def get(channel: String): zio.Task[Seq[Message]] = 
          runtime
            .spawn(baseCommand, readConversation(channel))
            .map(_.split("\n").toSeq.map(Message.apply))

      }
    }
  
    def send(channel: String, msg: String): ZIO[Has[KeybaseClient.Service], Throwable, Unit] = 
      ZIO.accessM(_.get.send(channel, msg))
  
    def get(channel: String): ZIO[Has[KeybaseClient.Service], Throwable, Seq[Message]] = 
      ZIO.accessM(_.get.get(channel))
}