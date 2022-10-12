package com.dkovalenko.config

import zio.ZLayer

final case class RedisConfig(host: String, port: Int)

object RedisConfig {
  val localhost: ZLayer[Any, Nothing, RedisConfig] =
    ZLayer.succeed(RedisConfig("localhost", 6379))
  val test: ZLayer[Any, Nothing, RedisConfig] =
    ZLayer.succeed(RedisConfig("localhost", 6379))
}

