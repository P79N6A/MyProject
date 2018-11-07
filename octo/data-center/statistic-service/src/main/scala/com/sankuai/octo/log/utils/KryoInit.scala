package com.sankuai.octo.log.utils

import com.esotericsoftware.kryo.Kryo
import com.romix.scala.serialization.kryo._
import org.objenesis.strategy.StdInstantiatorStrategy

class KryoInit {

  def customize(kryo: Kryo): Unit = {

    kryo.setInstantiatorStrategy(new StdInstantiatorStrategy())

    // Serialization of Scala enumerations
    kryo.addDefaultSerializer(classOf[scala.Enumeration#Value], classOf[EnumerationSerializer])

    // Serialization of Scala maps like Trees, etc
    kryo.addDefaultSerializer(classOf[scala.collection.immutable.Map[_, _]], classOf[ScalaImmutableMapSerializer])
    kryo.addDefaultSerializer(classOf[scala.collection.generic.MapFactory[scala.collection.immutable.Map]], classOf[ScalaImmutableMapSerializer])

    kryo.addDefaultSerializer(classOf[scala.collection.mutable.Map[_, _]], classOf[ScalaMutableMapSerializer])
    kryo.addDefaultSerializer(classOf[scala.collection.generic.MapFactory[scala.collection.mutable.Map]], classOf[ScalaMutableMapSerializer])

    // Serialization of Scala sets
    kryo.addDefaultSerializer(classOf[scala.collection.immutable.Set[_]], classOf[ScalaImmutableSetSerializer])
    kryo.addDefaultSerializer(classOf[scala.collection.generic.SetFactory[scala.collection.immutable.Set]], classOf[ScalaImmutableSetSerializer])

    kryo.addDefaultSerializer(classOf[scala.collection.mutable.Set[_]], classOf[ScalaMutableSetSerializer])
    kryo.addDefaultSerializer(classOf[scala.collection.generic.SetFactory[scala.collection.mutable.Set]], classOf[ScalaMutableSetSerializer])

    // Serialization of all Traversable Scala collections like Lists, Vectors, etc
    kryo.addDefaultSerializer(classOf[scala.collection.Traversable[_]], classOf[ScalaCollectionSerializer])
  }
}