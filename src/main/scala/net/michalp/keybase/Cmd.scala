package net.michalp.keybase

import zio.ZLayer
import zio.ZIO
import zio.Has


object cmd {
  case class RuntimeException(msg: String) extends Exception(msg)

  object CmdRuntime {

    trait Service {
      def execute(command: String): zio.Task[String]
    }

    def instance = ZLayer.succeed {
      new Service {
        def execute(command: String) = ZIO.effect{
          val status = os.proc(command.split(" ").toSeq).call()
          status.out.lines.mkString("\n")
        }
      }
    }

    def execute(c: String): ZIO[Has[CmdRuntime.Service], Throwable, String] = 
      ZIO.accessM(_.get.execute(c))

  }

}
