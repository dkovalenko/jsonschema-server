package com.dkovalenko.schema

import redis.RedisClient
import zio._

trait SchemaRepository {
  def getByIdAsString(key: String): Task[Option[String]]

  def setById(key: String, value: String): Task[Boolean]
}

class SchemaRepositoryRedis(host: String, port: Int) extends SchemaRepository {
  
  implicit val akkaSystem = akka.actor.ActorSystem()
  val redis = RedisClient(host, port)

  def getByIdAsString(key: String): Task[Option[String]] = {
    ZIO.fromFuture(implicit ec => redis.get(key).map(_.map(_.utf8String)))
  }

  def setById(key: String, value: String): Task[Boolean] = {
    ZIO.fromFuture(ec => redis.set(key, value))
  }
}
