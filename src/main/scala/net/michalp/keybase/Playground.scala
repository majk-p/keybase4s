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
import zio.system
import net.michalp.keybase.transport.response.MessageWrapper
import net.michalp.keybase.transport.syntax._

import cats.implicits._

object Playground extends App {

  def run(args: List[String]) = {
    val env = cmd.CmdRuntime.instance >>> KeybaseClient.instance
    myAppLogic.provideSomeLayer[ZEnv](env).fold({err => 
      err.printStackTrace()
      1
    }, _ => 0)
  }

  def debugInfo(login: String): String = 
    s"Hello, this is $login. The bot is now initialized."

  def handleMessage(login: String)(msg: MessageWrapper): zio.ZIO[Has[KeybaseClient.Service] with zio.console.Console, Throwable, Unit] = 
    putStrLn(s"New message from ${msg.sender}: ${msg.content}") *> (
    if(msg.sender != login)
        KeybaseClient
          .send(
            msg.channel,
            s"Hello ${msg.sender}"
          )
      else
        ZIO.unit
    )

  val ownerVariable = "KEYBASE_BOT_OWNER"
  val loginVariable = "KEYBASE_LOGIN"
  val keyVariable = "KEYBASE_PAPER_KEY"
 
  val credentials: ZIO[zio.system.System,Exception,(String, String, String)] = for {
    owner  <- system.env(ownerVariable)
    login  <- system.env(loginVariable)
    key    <- system.env(keyVariable)
    result <- ZIO.fromEither(
      Either.fromOption(
        (owner, login, key).tupled.map(t => (t._1.trim, t._2.trim, t._3.trim)),
        new Exception("Environment setup error")
      )
    )
  } yield result

  val myAppLogic = for {
    _  <- putStrLn("Keybase bot playground starting")
    _  <- putStrLn(s"Reading configuration from env variables: $ownerVariable, $loginVariable, $keyVariable")
    (owner, login, key) <- credentials
    _  <- putStrLn(s"Login: $login, Owner: $owner")
    _  <- KeybaseClient.init(login, key)
    _  <- putStrLn(s"Greeting owner")
    r1 <- KeybaseClient.send(owner, debugInfo(login))
    _  <- putStrLn(s"Owner greeting sent")
    r2 <- KeybaseClient.get(owner)
    lm = r2.messages.head
    _  <- putStrLn(s"Last message in direct owner chat was from ${lm.sender}: ${lm.content}")
    _  <- putStrLn("Starting message listener loop")
    s  <- KeybaseClient.listen
    _  <- putStrLn("Message listener loop started")
    _  <- s.foreach(handleMessage(login)).forever
  } yield ()
}