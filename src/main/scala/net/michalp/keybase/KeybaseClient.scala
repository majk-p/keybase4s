package net.michalp.keybase

import zio.Task
import zio.ZLayer
import zio.ZIO
import zio.Has

object KeybaseClient {
  trait Service {
    def get(channel: String): Task[Seq[KeybaseClient.Message]]
  }

  case class Message(body: String)
  def instance =  
    ZLayer.fromService { runtime: cmd.CmdRuntime.Service =>  
      new Service {
        def get(channel: String): zio.Task[Seq[Message]] = 
          runtime
            .execute(s"echo 'keybase chat on channel $channel'")
            .map(_.split("\n").toSeq.map(Message.apply))
      }
    }
  
    def get(channel: String): ZIO[Has[KeybaseClient.Service], Throwable, Seq[Message]] = 
      ZIO.accessM(_.get.get(channel))
}