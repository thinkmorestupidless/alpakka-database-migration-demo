//#full-example
package com.example

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.couchbase.scaladsl.CouchbaseFlow
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.stream.alpakka.couchbase.{CouchbaseSessionSettings, CouchbaseWriteSettings}
import akka.stream.scaladsl.Sink
import com.couchbase.client.java.{PersistTo, ReplicateTo}

import scala.concurrent.duration._
import akka.stream.alpakka.slick.scaladsl._
import akka.stream.scaladsl._
import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import slick.jdbc.GetResult

import com.lightbend.cinnamon.akka.stream.CinnamonAttributes.SourceWithInstrumented

import scala.concurrent.Future


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

  val sessionSettings = CouchbaseSessionSettings(system)

  // Domain
  case class SourceData(id: UUID, firstName: String, lastName: String, email: String, amount: BigDecimal)
  case class DestinationData(id: UUID, firstName: String, lastName: String, email: String, amount: BigDecimal, phone: String, country: String)

  // Slick query result mapping
  implicit val getUserResult = GetResult(r => SourceData(UUID.fromString(r.nextString()), r.nextString, r.nextString(), r.nextString(), r.nextBigDecimal()))

  import postgresSession.profile.api._

  // Converts the destination data into a JsonDocument that can be upserted into Couchbase
  def toJsonDocument(data: DestinationData): JsonDocument = {
    val obj = JsonObject.create()
    obj.put("firstName", data.firstName)
    obj.put("lastName", data.lastName)
    obj.put("email", data.email)
    obj.put("amount", data.amount)

    JsonDocument.create(data.id.toString, obj)
  }

  val done: Future[Done] =
    Slick
      .source(sql"SELECT customer_id, first_name, last_name, email, amount FROM unbilled_data".as[SourceData])
      .map { src =>
        DestinationData(src.id, src.firstName, src.lastName, src.email, src.amount, "", "")
      }
      .map(toJsonDocument)
//      .via(
//        CouchbaseFlow.upsert(
//          sessionSettings,
//          writeSettings,
//          "destination_data"
//        )
//      )
      .instrumentedRunWith(Sink.foreach(println))(name = "migration-stream")
}

//#main-class
object AlpakkaDemo extends App {

  new AlpakkaDemo
}
//#main-class
//#full-example
