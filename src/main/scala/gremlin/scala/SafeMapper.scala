package gremlin.scala

import shapeless._, labelled.{ FieldType, field },record._

trait FromMap[L <: HList] {
  def apply(m: Map[String, Any]): Option[L]
}

trait ToMapRec[L <: HList] { def apply(l: L): Map[String, Any] }

trait LowPriorityToMapRec {
  implicit def hconsToMapRec1[K <: Symbol, V, T <: HList](implicit
                                                          wit: Witness.Aux[K],
                                                          tmrT: ToMapRec[T]
                                                           ): ToMapRec[FieldType[K, V] :: T] = new ToMapRec[FieldType[K, V] :: T] {
    def apply(l: FieldType[K, V] :: T): Map[String, Any] =
      tmrT(l.tail) + (wit.value.name -> l.head)
  }
}

trait LowPriorityFromMap {
  implicit def hconsFromMap1[K <: Symbol, V, T <: HList](implicit
                                                         witness: Witness.Aux[K],
                                                         typeable: Typeable[V],
                                                         fromMapT: FromMap[T]
                                                          ): FromMap[FieldType[K, V] :: T] = new FromMap[FieldType[K, V] :: T] {
    def apply(m: Map[String, Any]): Option[FieldType[K, V] :: T] = for {
      v <- m.get(witness.value.name)
      h <- typeable.cast(v)
      t <- fromMapT(m)
    } yield field[K](h) :: t
  }
}

object ToMapRec extends LowPriorityToMapRec {
  implicit val hnilToMapRec: ToMapRec[HNil] = new ToMapRec[HNil] {
    def apply(l: HNil): Map[String, Any] = Map.empty
  }

  implicit def hconsToMapRec0[K <: Symbol, V, R <: HList, T <: HList](implicit
                                                                      wit: Witness.Aux[K],
                                                                      gen: LabelledGeneric.Aux[V, R],
                                                                      tmrH: ToMapRec[R],
                                                                      tmrT: ToMapRec[T]
                                                                       ): ToMapRec[FieldType[K, V] :: T] = new ToMapRec[FieldType[K, V] :: T] {
    def apply(l: FieldType[K, V] :: T): Map[String, Any] =
      tmrT(l.tail) + (wit.value.name -> tmrH(gen.to(l.head)))
  }
}

object FromMap extends LowPriorityFromMap {
  implicit val hnilFromMap: FromMap[HNil] = new FromMap[HNil] {
    def apply(m: Map[String, Any]): Option[HNil] = Some(HNil)
  }

  implicit def hconsFromMap0[K <: Symbol, V, R <: HList, T <: HList](implicit
                                                                     witness: Witness.Aux[K],
                                                                     gen: LabelledGeneric.Aux[V, R],
                                                                     fromMapH: FromMap[R],
                                                                     fromMapT: FromMap[T]
                                                                      ): FromMap[FieldType[K, V] :: T] = new FromMap[FieldType[K, V] :: T] {
    def apply(m: Map[String, Any]): Option[FieldType[K, V] :: T] = for {
      v <- m.get(witness.value.name)
      r <- Typeable[Map[String, Any]].cast(v)
      h <- fromMapH(r)
      t <- fromMapT(m)
    } yield field[K](gen.from(h)) :: t
  }
}

trait CcFromMapRec[A] { def apply(a: Map[String, Any]): Option[A] }

object CcFromMapRec {
  implicit def ccFromMapRec[A, R <: HList](implicit
                                         gen: LabelledGeneric.Aux[A, R],
                                           fromMap: FromMap[R]
                                          ): CcFromMapRec[A] = new CcFromMapRec[A] {
    def apply(a: Map[String, Any]): Option[A] = fromMap(a).map(gen.from(_))
  }
}

trait CcToMapRec[A] { def apply(a: A): Map[String, Any] }

object CcToMapRec {
  implicit def ccToMapRec[A, R <: HList](implicit
                                         gen: LabelledGeneric.Aux[A, R],
                                         tmr: ToMapRec[R]
                                          ): CcToMapRec[A] = new CcToMapRec[A] {
    def apply(a: A): Map[String, Any] = tmr(gen.to(a))
  }
}
