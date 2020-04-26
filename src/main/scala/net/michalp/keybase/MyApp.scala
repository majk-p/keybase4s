package net.michalp.keybase
import zio.App
import zio.console._
import scala.util.Try
import java.io.InputStreamReader
import java.io.BufferedReader
import zio.ZIO
import zio.IO
import zio.Task
import zio.ZLayer
import zio.Layer
import zio.Has
import zio.ZEnv



object MyApp extends App {

  def run(args: List[String]) = {
    val env = cmd.CmdRuntime.instance ++ (cmd.CmdRuntime.instance >>> KeybaseClient.instance)
    myAppLogic.provideSomeLayer[ZEnv](env).fold(_ => 1, _ => 0)
  }

  val myAppLogic =
    for {
      // _    <- putStr("$ ")
      // exec <- getStrLn
      // _    <- putStrLn(s"Executing command: `${exec}`")
      // r1   <- cmd.CmdRuntime.execute(exec)
      // _    <- putStrLn(s"Resp: $r1")
      r2   <- KeybaseClient.get("test-channel")
      _    <- putStrLn(s"Resp: ${r2.toList}")
      // _    <- getStrLn
      // _    <- putStrLn(r.map(_.body).mkString("\n"))
    } yield ()
}