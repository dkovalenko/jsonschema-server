package com.dkovalenko.config

import zio.ZLayer

final case class RedisConfig(host: String, port: Int)

object RedisConfig {

  val localhost: ZLayer[Any, Nothing, RedisConfig] =
    ZLayer.succeed(RedisConfig("localhost", 6379)) //In real world this should come from the config/ENV

  val test: ZLayer[Any, Nothing, RedisConfig] =
    ZLayer.succeed(RedisConfig("localhost", 6379))

}
