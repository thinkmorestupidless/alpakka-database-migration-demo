//#full-example
package com.example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.stream.alpakka.couchbase.CouchbaseWriteSettings
import com.couchbase.client.java.{PersistTo, ReplicateTo}

import scala.concurrent.duration._

class AlpakkaDemo {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  // Configure Postgres Session
  implicit val postgresSession = SlickSession.forConfig("slick-postgres")
  system.registerOnTermination(() => postgresSession.close())

  // Configure Couchbase Session
  val writeSettings = CouchbaseWriteSettings()
    .withParallelism(3)
    .withPersistTo(PersistTo.FOUR)
    .withReplicateTo(ReplicateTo.THREE)
    .withTimeout(5.seconds)

}

//#main-class
object AlpakkaDemo extends App {

  new AlpakkaDemo
}
//#main-class
//#full-example
