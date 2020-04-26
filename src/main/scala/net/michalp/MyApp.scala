import zio.App
import zio.console._
import scala.util.Try
import java.io.InputStreamReader
import java.io.BufferedReader
import zio.ZIO
import zio.IO

object CmdRuntime {
  case class RuntimeException(msg: String) extends Exception(msg)

  def execute(command: String) = ZIO.effect{
    val status = os.proc(command.split(" ").toSeq).call()
    status.out.lines.mkString("\n")
  }
}

object MyApp extends App {

  def run(args: List[String]) =
    myAppLogic.fold(_ => 1, _ => 0)

  val myAppLogic =
    for {
      _    <- putStr("$")
      cmd  <- getStrLn
      _    <- putStrLn(s"Executing command: `${cmd}`")
      r    <- CmdRuntime.execute(cmd)
      _    <- putStrLn(r)
    } yield ()
}