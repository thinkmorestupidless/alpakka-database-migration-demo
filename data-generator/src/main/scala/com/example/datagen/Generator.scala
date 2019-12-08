package com.example.datagen

import java.nio.file.Paths
import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.CsvFormatting
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.github.javafaker.Faker
import org.slf4j.LoggerFactory

class Generator {
  val log = LoggerFactory.getLogger(classOf[Generator])

  log.info("starting up")

  implicit val system = ActorSystem("data-generator")
  implicit val materializer = ActorMaterializer()

  val faker = new Faker()

  val recordCount = 1000000
  val outputPath = Paths.get("./docker/generator/docker-entrypoint-initdb.d/generated-records.csv")

  Source.repeat(NotUsed)
    .take(recordCount)
    .map(_ => createRecord())
    .via(CsvFormatting.format())
    .runWith(FileIO.toPath(outputPath))

  def createRecord(): List[String] = {
    val customerId = UUID.randomUUID().toString
    val firstName = faker.name().firstName()
    val lastName = faker.name().lastName()
    val email = faker.internet().emailAddress()
    val amount = faker.number().randomDouble(2, 0, 1000).toString

    List(customerId, firstName, lastName, email, amount)
  }
}

object Generator extends App {

  new Generator()
}
