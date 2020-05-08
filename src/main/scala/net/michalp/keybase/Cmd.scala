package net.michalp.keybase

import scala.util.Try
import zio.ZLayer
import zio.ZIO
import zio.Has
import zio.stream.ZStream
import zio.Managed
import zio.URIO
import os.SubProcess


object cmd {
  case class RuntimeException(msg: String) extends Exception(msg)

  object CmdRuntime {

    trait Service {
      def execute(command: Seq[String]): zio.Task[String]
      def execute(command: String): zio.Task[String]
      def listen(command: Seq[String]): ZIO[Any, Throwable, zio.stream.Stream[Throwable, String]]
      def spawn(command: Seq[String], input: String): zio.Task[String] 
      def spawn(command: String, input: String): zio.Task[String] 
    }

    def instance = ZLayer.succeed {
      new Service {

        def spawn(command: String, input: String): zio.Task[String] =
          spawn(splitCommand(command), input)

        def spawn(command: Seq[String], input: String): zio.Task[String] = ZIO.effect {
          val spawned = os.proc(command).spawn()
          spawned.stdin.writeLine(input)
          spawned.stdin.close()
          val resp = spawned.stdout.lines.distinct.mkString("\n")
          spawned.waitFor(5 * 1000)
          spawned.close()
          resp
        }
        
        def listen(command: Seq[String]): zio.Task[zio.stream.Stream[Throwable, String]] = 
          ZIO.bracket{
            ZIO.effect {
              val spawned = os.proc(command).spawn()
              (spawned, ZStream.unfold(spawned.stdout){ stdout =>
                Try{stdout.readLine()}.toOption.map((_, stdout))
              })
            }
          }{
            p => 
              // FIXME: This release happens too soon to kill the process
              URIO.unit
          }{
            p => ZIO.apply(p._2)
          }

        def execute(command: Seq[String]) = ZIO.effect{
          val status = os.proc(command).call()
          status.out.lines.mkString("\n")
        }

        def execute(command: String) = execute(splitCommand(command))
      }
    }

    def execute(c: Seq[String]): ZIO[Has[CmdRuntime.Service], Throwable, String] = 
      ZIO.accessM(_.get.execute(c))

    def execute(c: String): ZIO[Has[CmdRuntime.Service], Throwable, String] = 
      ZIO.accessM(_.get.execute(c))

    def listen(c: Seq[String]): ZIO[Has[CmdRuntime.Service], Throwable, zio.stream.Stream[Throwable, String]] = 
      ZIO.accessM(_.get.listen(c))

    def spawn(c: Seq[String], i: String): ZIO[Has[CmdRuntime.Service], Throwable, String] = 
      ZIO.accessM(_.get.spawn(c, i))

    def spawn(c: String, i: String): ZIO[Has[CmdRuntime.Service], Throwable, String] = 
      ZIO.accessM(_.get.spawn(c, i))

    private def splitCommand(c: String) = c.split(" ").toSeq
  }

}
