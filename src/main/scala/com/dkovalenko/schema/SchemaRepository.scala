package com.dkovalenko.schema

import redis.RedisClient
import com.dkovalenko.config.RedisConfig
import zio._

trait SchemaRepository {
  def getByIdAsString(key: String): Task[Option[String]]

  def setById(key: String, value: String): Task[Boolean]
}

object SchemaRepository {
  def getByIdAsString(key: String): ZIO[SchemaRepository, Nothing, Task[Option[String]]] = 
    ZIO.serviceWith[SchemaRepository](_.getByIdAsString(key))

  def setById(key: String, value: String): ZIO[SchemaRepository, Nothing, Task[Boolean]] = 
    ZIO.serviceWith[SchemaRepository](_.setById(key, value))
}

case class SchemaRepositoryRedis(config: RedisConfig) extends SchemaRepository {
  
  implicit val akkaSystem = akka.actor.ActorSystem()
  val redis = RedisClient(config.host, config.port) //Better way 

  def getByIdAsString(key: String): Task[Option[String]] = {
    ZIO.fromFuture(implicit ec => redis.get(key).map(_.map(_.utf8String)))
  }

  def setById(key: String, value: String): Task[Boolean] = {
    ZIO.fromFuture(ec => redis.set(key, value))
  }
}

object SchemaRepositoryRedis {
  val layer: ZLayer[RedisConfig, Nothing, SchemaRepository] =
    ZLayer.fromFunction(SchemaRepositoryRedis(_))
}
  