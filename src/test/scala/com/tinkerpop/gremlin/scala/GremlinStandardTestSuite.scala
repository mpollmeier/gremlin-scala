package com.tinkerpop.gremlin.scala

import java.lang.{ Long ⇒ JLong }
import java.util.{ List ⇒ JList, ArrayList ⇒ JArrayList, Map ⇒ JMap, Collection ⇒ JCollection }
import scala.collection.JavaConversions._

import collection.mutable
import com.tinkerpop.gremlin.process._
import com.tinkerpop.gremlin.process.graph.step.branch._
import com.tinkerpop.gremlin.process.graph.step.filter._
import com.tinkerpop.gremlin.process.graph.step.map._
import com.tinkerpop.gremlin.process.graph.step.sideEffect
import com.tinkerpop.gremlin.process.graph.step.sideEffect._
import com.tinkerpop.gremlin.structure
import com.tinkerpop.gremlin.structure.{ Compare, Contains, Direction }
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import shapeless._
import shapeless.ops.hlist._

object Tests {
  class ScalaDedupTest extends DedupTest with StandardTest {

    override def get_g_V_both_dedup_name = GremlinScala(g).V.both.dedup.values[String]("name")

    override def get_g_V_both_dedupXlangX_name =
      GremlinScala(g).V.both
        .dedup(_.property[String]("lang").orElse(null))
        .values[String]("name")

    override def get_g_V_both_propertiesXnameX_orderXa_bX_dedup_value =
      GremlinScala(g).V.both
        .values[String]("name")
        .order { (a, b) ⇒ a < b }
        .dedup
  }

  class ScalaFilterTest extends FilterTest with StandardTest {

    override def get_g_V_filterXfalseX = GremlinScala(g).V.filter(_ ⇒ false)

    override def get_g_V_filterXtrueX = GremlinScala(g).V.filter(_ ⇒ true)

    override def get_g_V_filterXlang_eq_javaX =
      GremlinScala(g).V.filter(_.property("lang").orElse("none") == "java")

    override def get_g_v1_out_filterXage_gt_30X(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.filter(_.property("age").orElse(0) > 30)

    override def get_g_V_filterXname_startsWith_m_OR_name_startsWith_pX = GremlinScala(g).V.filter { v ⇒
      val name = v.value[String]("name")
      name.startsWith("m") || name.startsWith("p")
    }

    override def get_g_E_filterXfalseX = GremlinScala(g).E.filter(_ ⇒ false)

    override def get_g_E_filterXtrueX = GremlinScala(g).E.filter(_ ⇒ true)

    override def get_g_v1_filterXage_gt_30X(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.filter(_.property("age").orElse(0) > 30)
  }

  class ScalaExceptTest extends ExceptTest with StandardTest {

    override def get_g_v1_out_exceptXg_v2X(v1Id: AnyRef, v2Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.except(g.v(v2Id))

    override def get_g_v1_out_aggregateXxX_out_exceptXxX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.aggregate("x").out.exceptVar("x")

    override def get_g_v1_outXcreatedX_inXcreatedX_exceptXg_v1X_name(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").in("created")
        .except(g.v(v1Id)).values[String]("name")

    override def get_g_V_exceptXg_V_toListX =
      GremlinScala(g).V.except(GremlinScala(g).V.toList)

    override def get_g_V_exceptXX = GremlinScala(g).V.except(Nil)

    override def get_g_v1_asXxX_bothEXcreatedX_exceptXeX_aggregateXeX_otherV_jumpXx_true_trueX_path(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.as("x")
        .bothE("created").exceptVar("e").aggregate("e")
        .otherV
        .jump("x", _ ⇒ true, _ ⇒ true)
        .path
  }

  class ScalaSimplePathTest extends SimplePathTest with StandardTest {

    override def get_g_v1_outXcreatedX_inXcreatedX_simplePath(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").in("created").simplePath

    override def get_g_V_asXxX_both_simplePath_jumpXx_loops_lt_3X_path =
      GremlinScala(g).V.as("x").both
        .simplePath
        .jumpWithTraverser("x", _.loops < 3)
        .path

    override def get_g_V_asXxX_both_simplePath_jumpXx_3X_path =
      GremlinScala(g).V.as("x").both.simplePath.jump("x", 3).path
  }

  class ScalaCyclicPathTest extends CyclicPathTest with StandardTest {

    override def get_g_v1_outXcreatedX_inXcreatedX_cyclicPath(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").in("created").cyclicPath

    override def get_g_v1_outXcreatedX_inXcreatedX_cyclicPath_path(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").in("created").cyclicPath.path
  }

  class ScalaHasTest extends HasTest with StandardTest {

    override def get_g_v1_hasXkeyX(v1Id: AnyRef, key: String) = GremlinScala(g).v(v1Id).get.has(key)

    override def get_g_v1_hasXname_markoX(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.has("name", "marko")

    override def get_g_V_hasXname_markoX = GremlinScala(g).V.has("name", "marko")

    override def get_g_V_hasXname_blahX = GremlinScala(g).V.has("name", "blah")

    override def get_g_V_hasXblahX = GremlinScala(g).V.has("blah")

    override def get_g_v1_hasXage_gt_30X(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.has("age", Compare.gt, 30)

    override def get_g_v1_out_hasXid_2X(v1Id: AnyRef, v2Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.has(T.id, v2Id)

    override def get_g_V_hasXage_gt_30X = GremlinScala(g).V.has("age", Compare.gt, 30)

    override def get_g_E_hasXlabelXknowsX = GremlinScala(g).E.has(T.label, "knows")

    override def get_g_e7_hasXlabelXknowsX(e7Id: AnyRef) = GremlinScala(g).e(e7Id).get.has(T.label, "knows")

    override def get_g_V_hasXlabelXperson_software_blahX =
      GremlinScala(g).V.has(T.label, Contains.within, Seq("person", "software"))

    override def get_g_E_hasXlabelXuses_traversesX =
      GremlinScala(g).E.has(T.label, Contains.within, List("uses", "traverses"))

    override def get_g_V_hasXname_equalspredicate_markoX = GremlinScala(g).V.has("name", "marko")

    override def get_g_V_hasXperson_name_markoX_age =
      GremlinScala(g).V.has("person", "name", "marko").values[Integer]("age")

  }

  class ScalaHasNotTest extends HasNotTest with StandardTest {

    override def get_g_v1_hasNotXprop(v1Id: AnyRef, prop: String) =
      GremlinScala(g).v(v1Id).get.hasNot(prop)

    override def get_g_V_hasNotXprop(prop: String) = GremlinScala(g).V.hasNot(prop)
  }

  class ScalaIntervalTest extends IntervalTest with StandardTest {

    override def get_g_v1_outE_intervalXweight_0_06X_inV(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE.interval("weight", 0d, 0.6d).inV
  }

  class ScalaRandomTest extends RandomTest with StandardTest {

    override def get_g_V_randomX1X = GremlinScala(g).V.random(1.0d)
    override def get_g_V_randomX0X = GremlinScala(g).V.random(0.0d)
  }

  class ScalaRangeTest extends RangeTest with StandardTest {

    override def get_g_v1_out_limitX2X(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.limit(2)

    override def get_g_V_outE_localLimitX1X_inV_limitX3X =
      GremlinScala(g).V.outE.localLimit(1).inV.limit(3)

    override def get_g_v1_outXknowsX_outEXcreatedX_rangeX0_1X_inV(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("knows").outE("created").range(0, 1).inV

    override def get_g_v1_outXknowsX_outXcreatedX_rangeX0_1X(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("knows").out("created").range(0, 1)

    override def get_g_v1_outXcreatedX_inXcreatedX_rangeX1_3X(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").in("created").range(1, 3)

    override def get_g_v1_outXcreatedX_inEXcreatedX_rangeX1_3X_outV(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").inE("created").range(1, 3).outV

    override def get_g_V_asXaX_both_jumpXa_3X_rangeX5_11X =
      GremlinScala(g).V.as("a").both.jump("a", 3).range(5, 11)
  }

  class ScalaRetainTest extends RetainTest with StandardTest {

    override def get_g_v1_out_retainXg_v2X(v1Id: AnyRef, v2Id: AnyRef) = {
      val v2 = g.v(v2Id)
      GremlinScala(g).v(v1Id).get.out.retainOne(v2)
    }

    override def get_g_v1_out_aggregateXxX_out_retainXxX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.aggregate("x").out.retain("x")
  }

  class ScalaBackTest extends BackTest with StandardTest {

    override def get_g_v1_asXhereX_out_backXhereX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.as("here").out.back[Vertex]("here")

    override def get_g_v4_out_asXhereX_hasXlang_javaX_backXhereX(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.out.as("here").has("lang", "java").back[Vertex]("here")

    override def get_g_v4_out_asXhereX_hasXlang_javaX_backXhereX_name(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.out.as("here").has("lang", "java")
        .back[Vertex]("here").values[String]("name")

    override def get_g_v1_outE_asXhereX_inV_hasXname_vadasX_backXhereX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE.as("here").inV.has("name", "vadas").back[Edge]("here")

    override def get_g_v1_outEXknowsX_hasXweight_1X_asXhereX_inV_hasXname_joshX_backXhereX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE("knows").has("weight", 1.0d).as("here").inV.has("name", "josh").back[Edge]("here")

    override def get_g_v1_outEXknowsX_asXhereX_hasXweight_1X_inV_hasXname_joshX_backXhereX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE("knows").as("here").has("weight", 1.0d).inV.has("name", "josh").back[Edge]("here")

    override def get_g_v1_outEXknowsX_asXhereX_hasXweight_1X_asXfakeX_inV_hasXname_joshX_backXhereX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE("knows").has("weight", 1.0d).as("here").inV.has("name", "josh").back[Edge]("here")

    override def get_g_V_asXhereXout_name_backXhereX =
      GremlinScala(g).V.as("here").out.values[String]("name").back[Vertex]("here")
  }

  class ScalaJumpTest extends JumpTest with StandardTest {

    override def get_g_v1_asXxX_out_jumpXx_loops_lt_2X_name(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.as("x").out
        .jumpWithTraverser("x", _.loops < 2)
        .values[String]("name")

    override def get_g_V_asXxX_out_jumpXx_loops_lt_2X =
      GremlinScala(g).V.as("x").out.jumpWithTraverser("x", _.loops < 2)

    override def get_g_V_asXxX_out_jumpXx_loops_lt_2_trueX =
      GremlinScala(g).V.as("x").out
        .jumpWithTraverser("x", _.loops < 2, _ ⇒ true)

    override def get_g_V_asXxX_out_jumpXx_loops_lt_2_trueX_path =
      GremlinScala(g).V.as("x").out
        .jumpWithTraverser("x", _.loops < 2, _ ⇒ true).path

    override def get_g_V_asXxX_out_jumpXx_2_trueX_path =
      GremlinScala(g).V.as("x").out.jump("x", 2, { _: Vertex ⇒ true }).path
    //TODO why does it need the type for _ ?

    override def get_g_V_asXxX_out_jumpXx_loops_lt_2X_asXyX_in_jumpXy_loops_lt_2X_name =
      GremlinScala(g).V.as("x").out
        .jumpWithTraverser("x", _.loops < 2).as("y").in
        .jumpWithTraverser("y", _.loops < 2).values[String]("name")

    override def get_g_V_asXxX_out_jumpXx_2X_asXyX_in_jumpXy_2X_name =
      GremlinScala(g).V.as("x").out
        .jump("x", 2).as("y").in
        .jump("y", 2).values[String]("name")

    override def get_g_V_asXxX_out_jumpXx_2X =
      GremlinScala(g).V.as("x").out.jump("x", 2)

    override def get_g_V_asXxX_out_jumpXx_2_trueX =
      GremlinScala(g).V.as("x").out
        .jumpWithTraverser("x", 2, { _: Traverser[_] ⇒ true })

    override def get_g_v1_out_jumpXx_t_out_hasNextX_in_jumpXyX_asXxX_out_asXyX_path(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out
        .jump("x", _.out().hasNext) //TODO why do I need to specify parenthesis here?
        .in.jump("y").as("x")
        .out.as("y").path

    override def get_g_V_jumpXxX_out_out_asXxX =
      GremlinScala(g).V.jump("x").out.out.as("x")

    override def get_g_v1_asXaX_jumpXb_loops_gt_1X_out_jumpXaX_asXbX_name(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.as("a")
        .jumpWithTraverser("b", _.loops > 1)
        .out.jump("a").as("b").values[String]("name")

  }

  class ScalaMapTest extends MapTest with StandardTest {

    override def get_g_v1_mapXnameX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.map(_.value[String]("name"))

    override def get_g_v1_outE_label_mapXlengthX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE.label.map(_.length: Integer)

    override def get_g_v1_out_mapXnameX_mapXlengthX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.map(_.value[String]("name")).map(_.toString.length: Integer)

    override def get_g_V_asXaX_out_mapXa_nameX =
      GremlinScala(g).V.as("a").out
        .mapWithTraverser { _.get[Vertex]("a").value[String]("name") }

    override def get_g_V_asXaX_out_out_mapXa_name_it_nameX =
      GremlinScala(g).V.as("a").out.out.mapWithTraverser { t: Traverser[Vertex] ⇒
        val a = t.get[Vertex]("a")
        val aName = a.value[String]("name")
        val vName = t.get.value[String]("name")
        s"$aName$vName"
      }
  }

  class ScalaOrderTest extends OrderTest with StandardTest {
    override def get_g_V_name_order = GremlinScala(g).V.values[String]("name").order

    override def get_g_V_name_orderXabX = GremlinScala(g).V.values[String]("name").order {
      case (a, b) ⇒ a > b
    }

    override def get_g_V_name_orderXa1_b1__b2_a2X =
      GremlinScala(g).V.values[String]("name")
        .order { case (a, b) ⇒ b.substring(2, 3) < a.substring(2, 3) }
        .order { case (a, b) ⇒ a.substring(1, 2) < b.substring(1, 2) }

  }

  class ScalaSelectTest extends SelectTest with StandardTest {

    override def get_g_v1_asXaX_outXknowsX_asXbX_select(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.as("a").out("knows").as("b").select()

    override def get_g_v1_asXaX_outXknowsX_asXbX_selectXnameX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.as("a").out("knows").as("b").select { v: Vertex ⇒
        v.value[String]("name")
      }

    override def get_g_v1_asXaX_outXknowsX_asXbX_selectXaX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.as("a").out("knows").as("b").select(Seq("a"))

    override def get_g_v1_asXaX_outXknowsX_asXbX_selectXa_nameX(v1Id: AnyRef) =
      ???
    // GremlinScala(g).v(v1Id).get.as("a").out("knows").as("b")
    //   .select(Seq("a"), {v: Vertex ⇒ v.value[String]("name")})

    override def get_g_V_asXaX_out_asXbX_selectXnameX =
      GremlinScala(g).V.as("a").out.as("b").select { v: Vertex ⇒
        v.value[String]("name")
      }

    override def get_g_V_asXaX_out_aggregate_asXbX_selectXnameX =
      GremlinScala(g).V.as("a").out.aggregate.as("b").select { v: Vertex ⇒
        v.value[String]("name")
      }

    override def get_g_V_asXaX_name_order_asXbX_selectXname_itX = ???
    // return g.V().as("a").values("name").order().as("b").select(v -> ((Vertex) v).value("name"), Function.identity())
    // GremlinScala(g).V.as("a").values[String]("name").order.as("b").select { name: String ⇒
    //   v.value[String]("name")
    // }
  }

  class ScalaVertexTest extends VertexTest with StandardTest {
    override def get_g_V = GremlinScala(g).V
    override def get_g_v1_out(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.out
    override def get_g_v2_in(v2Id: AnyRef) = GremlinScala(g).v(v2Id).get.in
    override def get_g_v4_both(v4Id: AnyRef) = GremlinScala(g).v(v4Id).get.both

    override def get_g_v1_outEXknowsX_localLimitX1X_inV_name(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE("knows").localLimit(1).inV.values[String]("name")

    override def get_g_V_bothEXcreatedX_localLimitX1X_otherV_name =
      GremlinScala(g).V.bothE("created").localLimit(1).inV.values[String]("name")

    override def get_g_E = GremlinScala(g).E
    override def get_g_v1_outE(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.outE
    override def get_g_v2_inE(v2Id: AnyRef) = GremlinScala(g).v(v2Id).get.inE
    override def get_g_v4_bothE(v4Id: AnyRef) = GremlinScala(g).v(v4Id).get.bothE

    override def get_g_v4_bothEX1_createdX_localLimitX1X(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.bothE("created").localLimit(1)

    override def get_g_v1_outE_inV(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.outE.inV
    override def get_g_v2_inE_outV(v2Id: AnyRef) = GremlinScala(g).v(v2Id).get.inE.outV
    override def get_g_V_outE_hasXweight_1X_outV = GremlinScala(g).V.outE.has("weight", 1.0d).outV

    override def get_g_V_out_outE_inV_inE_inV_both_name =
      GremlinScala(g).V.out.outE.inV.inE.inV.both.values[String]("name")

    override def get_g_v1_outEXknowsX_bothV_name(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE("knows").bothV.values[String]("name")

    override def get_g_v1_outXknowsX(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.out("knows")
    override def get_g_v1_outXknows_createdX(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.out("knows", "created")
    override def get_g_v1_outEXknowsX_inV(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.outE("knows").inV
    override def get_g_v1_outEXknows_createdX_inV(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.outE("knows", "created").inV
    override def get_g_v1_outE_otherV(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.outE.otherV
    override def get_g_v4_bothE_otherV(v4Id: AnyRef) = GremlinScala(g).v(v4Id).get.bothE.otherV

    override def get_g_v4_bothE_hasXweight_lt_1X_otherV(v4Id: AnyRef) = GremlinScala(g).v(v4Id).get.bothE.has("weight", Compare.lt, 1d).otherV

    override def get_g_V_out_out = GremlinScala(g).V.out.out
    override def get_g_v1_out_out_out(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.out.out.out
    override def get_g_v1_to_XOUT_knowsX(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.to(Direction.OUT, "knows")

    override def get_g_v4_bothEXknows_createdX_localLimitX1X(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.bothE("knows", "created").localLimit(1)

    override def get_g_v4_bothEXcreatedX(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.bothE("created")

    override def get_g_V_inEXknowsX_localLimitX2X_outV_name =
      GremlinScala(g).V.inE("knows").localLimit(2).outV.values[String]("name")

    override def get_g_v1_out_name(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.values[String]("name")

    override def get_g_v4_bothE_localLimitX1X_otherV_name(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.bothE.localLimit(1).otherV.values[String]("name")

    override def get_g_v4_bothE_localLimitX2X_otherV_name(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.bothE.localLimit(2).inV.values[String]("name")
  }

  class ScalaAggregateTest extends AggregateTest with StandardTest {

    override def get_g_V_name_aggregate =
      GremlinScala(g).V.values[String]("name").aggregate
        .traversal.asInstanceOf[Traversal[Vertex, JList[String]]]

    override def get_g_V_aggregateXnameX =
      GremlinScala(g).V.aggregate { v: Vertex ⇒ v.value[String]("name") }
        .traversal.asInstanceOf[Traversal[Vertex, JList[String]]]

    override def get_g_V_out_aggregateXaX_path =
      GremlinScala(g).V.out.aggregate("a").path
  }

  class ScalaCountTest extends CountTest with StandardTest {
    override def get_g_V_count = GremlinScala(g).V.count
    override def get_g_V_out_count = GremlinScala(g).V.out.count
    override def get_g_V_both_both_count = GremlinScala(g).V.both.both.count
    override def get_g_V_filterXfalseX_count = GremlinScala(g).V.filter { _ ⇒ false }.count
    override def get_g_V_asXaX_out_jumpXa_loops_lt_3X_count =
      GremlinScala(g).V.as("a").out.jumpWithTraverser("a", _.loops < 3).count
  }

  class ScalaSideEffectTest extends sideEffect.SideEffectTest with StandardTest {

    override def get_g_v1_sideEffectXstore_aX_name(v1Id: AnyRef) = {
      val a = new JArrayList[Vertex] //test is expecting a java arraylist..
      GremlinScala(g).v(v1Id).get.`with`("a", a).sideEffect { traverser ⇒
        a.add(traverser.get)
      }.values[String]("name")
    }

    override def get_g_v1_out_sideEffectXincr_cX_name(v1Id: AnyRef) = {
      val c = new JArrayList[Integer] //test is expecting a java arraylist..
      c.add(0)
      GremlinScala(g).v(v1Id).get.`with`("c", c).out.sideEffect { traverser ⇒
        val tmp = c.get(0)
        c.clear()
        c.add(tmp + 1)
      }.values[String]("name")
    }

    override def get_g_v1_out_sideEffectXX_name(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.sideEffect { traverser: Traverser[Vertex] ⇒
        // println("side effect")
      }.values[String]("name")
  }

  class ScalaSideEffectCapTest extends SideEffectCapTest with StandardTest {

    override def get_g_V_hasXageX_groupCountXa_nameX_out_capXaX =
      GremlinScala(g).V.has("age")
        .groupCount("a", _.value[String]("name"))
        .out.cap("a").traversal
        .asInstanceOf[Traversal[Vertex, JMap[String, JLong]]] //only for Scala 2.10...
  }

  class ScalaGroupCountTest extends GroupCountTest with StandardTest {

    override def get_g_V_outXcreatedX_groupCountXnameX =
      GremlinScala(g).V.out("created").groupCount[String] { v: Vertex ⇒
        v.value[String]("name")
      }.traversal.asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

    override def get_g_V_outXcreatedX_name_groupCount =
      GremlinScala(g).V.out("created").values[String]("name").groupCount
        .traversal.asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

    override def get_g_V_outXcreatedX_name_groupCountXaX =
      GremlinScala(g).V.out("created").values[String]("name").groupCount("a")
        .traversal.asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

    override def get_g_V_filterXfalseX_groupCount =
      GremlinScala(g).V.filter(_ ⇒ false).groupCount
        .traversal.asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

    override def get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_loops_lt_2X_capXaX =
      GremlinScala(g).V.as("x").out
        .groupCount("a", _.value[String]("name"))
        .jumpWithTraverser("x", _.loops < 2)
        .cap("a")
        .traversal
        .asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

    override def get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_2X_capXaX =
      GremlinScala(g).V.as("x").out
        .groupCount("a", _.value[String]("name"))
        .jump("x", 2)
        .cap("a")
        .traversal
        .asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]
  }

  class ScalaGroupByTest extends GroupByTest with StandardTest {

    override def get_g_V_groupByXnameX =
      GremlinScala(g).V.groupBy(_.value[String]("name"))
        .traversal.asInstanceOf[Traversal[Vertex, JMap[String, JCollection[Vertex]]]]

    override def get_g_V_hasXlangX_groupByXa_lang_nameX_out_capXaX =
      GremlinScala(g).V.has("lang").groupBy(
        sideEffectKey = "a",
        keyFunction = _.value[String]("lang"),
        valueFunction = _.value[String]("name")
      ).out.cap("a")
        .traversal
        .asInstanceOf[Traversal[Vertex, JMap[String, JCollection[String]]]] //only for Scala 2.10...

    override def get_g_V_hasXlangX_groupByXlang_1_sizeX =
      GremlinScala(g).V.has("lang").groupBy(
        keyFunction = _.value[String]("lang"),
        valueFunction = _ ⇒ 1,
        reduceFunction = { c: JCollection[_] ⇒ c.size }
      ).traversal.asInstanceOf[Traversal[Vertex, JMap[String, Integer]]]

    override def get_g_V_asXxX_out_groupByXa_name_sizeX_jumpXx_2X_capXaX =
      GremlinScala(g).V.as("x").out
        .groupBy(
          sideEffectKey = "a",
          keyFunction = _.value[String]("name"),
          valueFunction = v ⇒ v,
          reduceFunction = { c: JCollection[_] ⇒ c.size }
        ).jump("x", 2).cap("a")
        .traversal
        .asInstanceOf[Traversal[Vertex, JMap[String, Integer]]] //only for Scala 2.10...

    override def get_g_V_asXxX_out_groupByXa_name_sizeX_jumpXx_loops_lt_2X_capXaX =
      GremlinScala(g).V.as("x").out
        .groupBy(
          sideEffectKey = "a",
          keyFunction = _.value[String]("name"),
          valueFunction = v ⇒ v,
          reduceFunction = { c: JCollection[_] ⇒ c.size }
        ).jumpWithTraverser("x", _.loops < 2).cap("a")
        .traversal
        .asInstanceOf[Traversal[Vertex, JMap[String, Integer]]] //only for Scala 2.10...
  }

  class ScalaUntilTest extends UntilTest with StandardTest {
    override def get_g_v1_untilXa_loops_gt_1X_out_asXaX_name(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.untilWithTraverser("a", _.loops > 1).out.as("a").values[String]("name")

    override def get_g_v1_untilXa_1X_out_asXaX_name(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.until("a", 1).out.as("a").values[String]("name")

    override def get_g_V_untilXa_loops_gt_1X_out_asXaX_count =
      GremlinScala(g).V.untilWithTraverser("a", _.loops > 1).out.as("a").count

    override def get_g_V_untilXa_1X_out_asXaX_count =
      GremlinScala(g).V.until("a", 1).out.as("a").count

    override def get_g_V_untilXa_1_trueX_out_asXaX_name =
      GremlinScala(g).V.until("a", 1, { _: Any ⇒ true }).out.as("a").values[String]("name")
  }

}

trait StandardTest {
  implicit def toTraversal[S, E](gs: GremlinScala[E, _]): Traversal[S, E] =
    gs.traversal.asInstanceOf[Traversal[S, E]]
}

import Tests._
import com.tinkerpop.gremlin._
import org.junit.runners.model.RunnerBuilder
import org.junit.runner.RunWith
import java.util.{ Map ⇒ JMap }
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
// import com.tinkerpop.gremlin.tinkergraph.TinkerGraphGraphProvider //send a pr?
import com.tinkerpop.gremlin.structure.StructureStandardSuite
import org.apache.commons.configuration.Configuration
import java.io.File
class GremlinScalaStandardSuite(clazz: Class[_], builder: RunnerBuilder)
  extends AbstractGremlinSuite(clazz, builder,
    Array( //testsToExecute - all are in ProcessStandardSuite
      classOf[ScalaDedupTest],
      classOf[ScalaFilterTest],
      classOf[ScalaExceptTest],
      classOf[ScalaSimplePathTest],
      classOf[ScalaCyclicPathTest],
      classOf[ScalaHasTest],
      classOf[ScalaHasNotTest],
      classOf[ScalaIntervalTest],
      classOf[ScalaRandomTest],
      classOf[ScalaRangeTest],
      classOf[ScalaRetainTest],
      classOf[ScalaBackTest],
      classOf[ScalaJumpTest],
      classOf[ScalaMapTest],
      classOf[ScalaOrderTest],
      classOf[ScalaVertexTest],
      classOf[ScalaAggregateTest],
      classOf[ScalaCountTest],
      classOf[ScalaSideEffectTest],
      classOf[ScalaSideEffectCapTest],
      classOf[ScalaGroupCountTest],
      classOf[ScalaGroupByTest],
      classOf[ScalaUntilTest]
    // classOf[ScalaSelectTest] //doesnt fully work yet.. we need a typesafe alternative
    ),
    Array.empty, //testsToEnforce
    true //gremlinFlavourSuite - don't enforce opt-ins for graph implementations
  )

@RunWith(classOf[GremlinScalaStandardSuite])
@AbstractGremlinSuite.GraphProviderClass(
  provider = classOf[TinkerGraphGraphProvider],
  graph = classOf[TinkerGraph])
class ScalaTinkerGraphStandardTest {}
//TODO configure sbt to run with junit so it properly prints the test count - does that work in gremlin-java?

class TinkerGraphGraphProvider extends AbstractGraphProvider {
  override def getBaseConfiguration(graphName: String, test: Class[_], testMethodName: String): JMap[String, AnyRef] =
    Map("gremlin.graph" -> classOf[TinkerGraph].getName)

  override def clear(graph: Graph, configuration: Configuration): Unit =
    Option(graph) map { graph ⇒
      graph.close()
      if (configuration.containsKey("gremlin.tg.directory"))
        new File(configuration.getString("gremlin.tg.directory")).delete()
    }
}
