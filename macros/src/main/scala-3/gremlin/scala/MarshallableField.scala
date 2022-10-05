package gremlin.scala


import scala.annotation.{implicitNotFound, targetName}
import scala.deriving.Mirror
import MacroUtils.*
import scala.jdk.CollectionConverters.*
import java.{util => ju}

@implicitNotFound(
  "Could not find an implicit MarshallableField[${T}]\n" +
  "You may be missing an `MarshallingFeatureSet` instance\n" +
  "Try one of the following:\n" +
  " - import gremlin.scala.MarshallingFeatureSet.PrimitivesOnly.given\n" +
  " - import gremlin.scala.MarshallingFeatureSet.Neptune.given\n" +
  " - import gremlin.scala.MarshallingFeatureSet.FullMonty.given"
)
trait MarshallableField[T]:
  type Repr = T
  type Encoded
  def encode(value: T): Encoded
  def decode(value: Encoded): T

object MarshallableField:

  import scala.quoted.*
  import scala.compiletime.erasedValue

  def identityInstance[T]: MarshallableField[T] = new MarshallableField[T]:
    type Encoded = T
    def encode(value: T): Encoded = value
    def decode(value: Encoded): T = value

  inline given MarshallableField[Int] = identityInstance[Int]
  inline given MarshallableField[Long] = identityInstance[Long]
  inline given MarshallableField[Float] = identityInstance[Float]
  inline given MarshallableField[Double] = identityInstance[Double]
  inline given MarshallableField[String] = identityInstance[String]

  inline given [T, Coll[_] <: Iterable[_], FeatureSet <: MarshallingFeatureSet]
    (using featureSet: FeatureSet)
    (using nested: MarshallableField[T])
  : MarshallableField[Coll[T]] =
    inline erasedValue[Coll[T]] match
      case _: Seq[T] =>
        inline erasedValue[featureSet.SeqFlag] match
          case _: FeatureEnabled =>
            new MarshallableField[Coll[T]] :
              type Encoded = ju.List[T]
              def encode(value: Seq[T]): Encoded = value.asJava
              def decode(value: Encoded): Seq[T] = value.asScala.to(Seq)
          case _ => compiletime.error("Seq inspection not enabled")
      case _: Set[T] =>
        inline erasedValue[featureSet.SetFlag] match
          case _: FeatureEnabled =>
            new MarshallableField[Coll[T]] :
              type Encoded = ju.Set[T]
              def encode(value: Set[T]): Encoded = value.asJava
              def decode(value: Encoded): Set[T] = value.asScala.to(Set)
          case _ => compiletime.error("Set inspection not enabled")
      case _ => compiletime.error("Unknown collection type")

  inline given [K, V, FeatureSet <: MarshallingFeatureSet]
    (using featureSet: FeatureSet)
    (using keyInstance: MarshallableField[K], valInstance: MarshallableField[V])
  : MarshallableField[Map[K,V]] =
    inline erasedValue[featureSet.MapFlag] match
      case _: FeatureEnabled =>
        new MarshallableField[Map[K,V]] :
          type Encoded = ju.Map[keyInstance.Encoded, valInstance.Encoded]
          def encode(value: Map[K,V]): Encoded =
            value.map((k,v) => keyInstance.encode(k) -> valInstance.encode(v)).asJava
          def decode(value: Encoded): Map[K,V] =
            value.asScala.map((k,v) => keyInstance.decode(k) -> valInstance.decode(v)).to(Map)
      case _ => compiletime.error("Seq inspection not enabled")

  inline given [P <: Product, FeatureSet <: MarshallingFeatureSet]
    (using featureSet: FeatureSet)
  : MarshallableField[P] =
    ${productMacro[P, FeatureSet]}

  private def productMacro[
    P: Type,
    FeatureSet <: MarshallingFeatureSet : Type,
  ](using q: Quotes): Expr[MarshallableField[P]] =
    new MarshallableFieldMacros[q.type, P, FeatureSet].handleProduct

