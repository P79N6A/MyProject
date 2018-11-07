package com.sankuai.octo.msgp.model

object CellarPath extends Enumeration {
  type Cellar = Value

  val cellar_providers = Value("providers/cellar")
  val cellar_routes = Value("routes/cellar")
}