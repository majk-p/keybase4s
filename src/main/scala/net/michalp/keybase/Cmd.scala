package net.michalp.keybase

import zio.ZLayer
import zio.ZIO
import zio.Has


object cmd {
  case class RuntimeException(msg: String) extends Exception(msg)

  object CmdRuntime {

    trait Service {
      def execute(command: String): zio.Task[String]
      def spawn(command: String, input: String): zio.Task[String] 
    }

    def instance = ZLayer.succeed {
      new Service {

        def spawn(command: String, input: String): zio.Task[String] = ZIO.effect {
          val spawned = os.proc(splitCommand(command)).spawn()
          spawned.stdin.writeLine(input)
          spawned.stdin.close()
          println("Input received:")
          println(input)
          val resp = spawned.stdout.lines.distinct.mkString("\n")
          spawned.waitFor(5 * 1000)
          spawned.close()
          resp
        }

        def execute(command: String) = ZIO.effect{
          val status = os.proc(splitCommand(command)).call()
          status.out.lines.mkString("\n")
        }
      }
    }

    def execute(c: String): ZIO[Has[CmdRuntime.Service], Throwable, String] = 
      ZIO.accessM(_.get.execute(c))

    def spawn(c: String, i: String): ZIO[Has[CmdRuntime.Service], Throwable, String] = 
      ZIO.accessM(_.get.spawn(c, i))

    private def splitCommand(c: String) = c.split(" ").toSeq
  }

}
