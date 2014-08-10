package com.tinkerpop.gremlin.scala

import java.lang.{ Long ⇒ JLong }
import java.util.{ List ⇒ JList, ArrayList ⇒ JArrayList, Map ⇒ JMap }
import scala.collection.JavaConversions._

import collection.mutable
import com.tinkerpop.gremlin.process._
import com.tinkerpop.gremlin.process.graph.step.filter._
import com.tinkerpop.gremlin.process.graph.step.map._
import com.tinkerpop.gremlin.process.graph.step.sideEffect
import com.tinkerpop.gremlin.process.graph.step.sideEffect._
import com.tinkerpop.gremlin.structure.Element
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import com.tinkerpop.gremlin.util.function.SConsumer
import shapeless._
import shapeless.ops.hlist._

class StandardTests extends TestBase {
  import Tests._

  describe("filter steps") {
    it("dedups") {
      val test = new ScalaDedupTest
      test.g_V_both_dedup_name
      test.g_V_both_dedupXlangX_name
      // test.g_V_both_name_orderXa_bX_dedup
    }

    it("filters") {
      val test = new ScalaFilterTest
      test.g_V_filterXfalseX
      test.g_V_filterXtrueX
      test.g_V_filterXlang_eq_javaX
      test.g_v1_out_filterXage_gt_30X
      test.g_V_filterXname_startsWith_m_OR_name_startsWith_pX
      test.g_E_filterXfalseX
      test.g_E_filterXtrueX
      test.g_v1_filterXage_gt_30X
    }

    it("excepts") {
      val test = new ScalaExceptTest
      test.g_v1_out_exceptXg_v2X
      test.g_v1_out_aggregateXxX_out_exceptXxX
      test.g_v1_outXcreatedX_inXcreatedX_exceptXg_v1X_valueXnameX
      test.g_V_exceptXg_VX
      test.g_V_exceptXX
      // test.g_v1_asXxX_bothEXcreatedX_exceptXeX_aggregateXeX_otherV_jumpXx_true_trueX_path
    }

    it("finds the simple path") {
      val test = new ScalaSimplePathTest
      test.g_v1_outXcreatedX_inXcreatedX_simplePath
      // test.g_V_asXxX_both_simplePath_jumpXx_loops_lt_3X_path
      // test.g_V_asXxX_both_simplePath_jumpXx_3X_path
    }

    it("finds the cyclic path") {
      val test = new ScalaCyclicPathTest
      test.g_v1_outXcreatedX_inXcreatedX_cyclicPath
      // test.g_v1_outXcreatedX_inXcreatedX_cyclicPath_path
    }

    it("filters with has") {
      val test = new ScalaHasTest
      test.g_V_hasXname_markoX
      test.g_V_hasXname_blahX
      test.g_V_hasXblahX
      test.g_v1_out_hasXid_2X
      test.g_V_hasXage_gt_30X
      test.g_E_hasXlabelXknowsX
      test.g_E_hasXlabelXknows_createdX
      test.g_e7_hasXlabelXknowsX
      test.g_v1_hasXage_gt_30X
      test.g_v1_hasXkeyX
      test.g_v1_hasXname_markoX
      test.get_g_V_hasXname_equalspredicate_markoX

    }

    it("filters with has not") {
      val test = new ScalaHasNotTest
      test.get_g_v1_hasNotXprop()
      test.get_g_V_hasNotXprop()
    }

    it("filters on interval") {
      val test = new ScalaIntervalTest
      test.g_v1_outE_intervalXweight_0_06X_inV
    }

    it("filters randomly based on probability") {
      val test = new ScalaRandomTest
      test.g_V_randomX1X
      test.g_V_randomX0X
    }

    it("filters on range") {
      val test = new ScalaRangeTest
      test.g_v1_out_rangeX0_1X
      test.g_V_outX1X_rangeX0_2X
      test.g_v1_outXknowsX_outEXcreatedX_rangeX0_0X_inV
      test.g_v1_outXknowsX_outXcreatedX_rangeX0_0X
      test.g_v1_outXcreatedX_inXcreatedX_rangeX1_2X
      test.g_v1_outXcreatedX_inEXcreatedX_rangeX1_2X_outV
    }

    it("retains certain objects") {
      val test = new ScalaRetainTest
      test.g_v1_out_retainXg_v2X
      test.g_v1_out_aggregateXxX_out_retainXxX
    }

    it("retains a given set of objects") {
      val g = TinkerFactory.createClassic
      val graph = GremlinScala(g)
      //val retainCollection = Seq(graph.v(v1Id).get, graph.v(v2Id).get)
      val retainCollection = Seq(g.v(v1Id), g.v(v2Id))
      graph.V.retainAll(retainCollection).toList.size shouldBe 2
    }
  }

  describe("map steps") {
    it("goes back to given step") {
      val test = new ScalaBackTest
      test.g_v1_asXhereX_out_backXhereX
      test.g_v4_out_asXhereX_hasXlang_javaX_backXhereX
      test.g_v4_out_asXhereX_hasXlang_javaX_backXhereX_valueXnameX
      test.g_v1_outEXknowsX_hasXweight_1X_asXhereX_inV_hasXname_joshX_backXhereX
    }

    ignore("jumps") {
      // val test = new ScalaJumpTest
      // test.g_v1_asXxX_out_jumpXx_loops_lt_2X_valueXnameX
    }

    it("maps") {
      val test = new ScalaMapTest
      test.g_v1_mapXnameX
      test.g_v1_outE_label_mapXlengthX
      test.g_v1_out_mapXnameX_mapXlengthX
      // test.g_V_asXaX_out_mapXa_nameX
    }

    it("orders") {
      val test = new ScalaOrderTest
      test.g_V_name_order
      test.g_V_name_orderXabX
      test.g_V_orderXa_nameXb_nameX_name
    }

    it("selects") {
      val test = new ScalaSelectTest
      test.g_v1_asXaX_outXknowsX_asXbX_select
      // test.g_v1_asXaX_outXknowsX_asXbX_selectXnameX
      test.g_v1_asXaX_outXknowsX_asXbX_selectXaX
      // test.g_v1_asXaX_outXknowsX_asXbX_selectXa_nameX
    }

    it("traverses") {
      val test = new ScalaTraversalTest
      test.g_V
      test.g_v1_out
      test.g_v2_in
      test.g_v4_both
      test.g_v1_outX1_knowsX_name
      test.g_V_bothX1_createdX_name
      test.g_E
      test.g_v1_outE
      test.g_v2_inE
      test.g_v4_bothE
      test.g_v4_bothEX1_createdX
      test.g_V_inEX2_knowsX_outV_name
      test.g_v1_outE_inV
      test.g_v2_inE_outV
      test.g_V_outE_hasXweight_1X_outV
      test.g_V_out_outE_inV_inE_inV_both_name
      test.g_v1_outEXknowsX_bothV_name
      test.g_v1_outE_otherV
      test.g_v4_bothE_outV
      test.g_v4_bothE_hasXweight_LT_1X_otherV
      test.g_v1_outXknowsX
      test.g_v1_outXknows_createdX
      test.g_v1_outEXknowsX_inV
      test.g_v1_outEXknows_createdX_inV
      test.g_V_out_out
      test.g_v1_out_out_out
      test.g_v1_out_propertyXnameX
      // test.g_v1_to_XOUT_knowsX
    }

    it("values") {
      val test = new ScalaValuesTest
      test.g_V_values
      test.g_V_valuesXname_ageX
      test.g_E_valuesXid_label_weightX
      test.g_v1_outXcreatedX_values
    }
  }

  describe("side effects") {
    it("aggregates") {
      val test = new ScalaAggregateTest
      test.g_V_valueXnameX_aggregate
      test.g_V_aggregateXnameX
      // test.g_V_out_aggregateXaX_path
    }

    it("counts") {
      val test = new ScalaCountTest
      test.g_V_count
      test.g_V_out_count
      test.g_V_both_both_count
      test.g_V_filterXfalseX_count
    }

    it("allows side effects") {
      val test = new ScalaSideEffectTest
      test.g_v1_sideEffectXstore_aX_valueXnameX
      test.g_v1_out_sideEffectXincr_cX_valueXnameX
      test.g_v1_out_sideEffectXX_valueXnameX
    }

    it("allows side effects with cap", org.scalatest.Tag("foo")) {
      val test = new ScalaSideEffectCapTest
      // test.g_V_hasXageX_groupCountXnameX_asXaX_out_capXaX
    }

    it("groupCounts") {
      val test = new ScalaGroupCountTest
      test.g_V_outXcreatedX_groupCountXnameX
      test.g_V_outXcreatedX_name_groupCount
      test.g_V_filterXfalseX_groupCount
      // test.g_V_asXxX_out_groupCountXnameX_asXaX_jumpXx_2X_capXaX
    }
  }

  val v1Id = 1: Integer
  val v2Id = 2: Integer
  val v3Id = 3: Integer
  val v4Id = 4: Integer
  val v5Id = 5: Integer
  val v6Id = 6: Integer
  val e7Id = 7: Integer
  val e8Id = 8: Integer
  val e9Id = 9: Integer
  val e10Id = 10: Integer
  val e11Id = 11: Integer
  val e12Id = 12: Integer
  val allNames = Set("lop", "vadas", "josh", "marko", "peter", "ripple")
  val allVertexIds = Set(v1Id, v2Id, v3Id, v4Id, v5Id, v6Id)
  val allEdgeIds = Set(e7Id, e8Id, e9Id, e10Id, e11Id, e12Id)
  def getId(v: Element) = v.id
}

object Tests {
  class ScalaDedupTest extends DedupTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_V_both_dedup_name = GremlinScala(g).V.both.dedup.value[String]("name")

    override def get_g_V_both_dedupXlangX_name =
      GremlinScala(g).V.both
        .dedup(_.property[String]("lang").orElse(null))
        .value[String]("name")

    override def get_g_V_both_name_orderXa_bX_dedup = ???
    // GremlinScala(g).V.both
    // return g.V().both().property("name").order((a, b) -> ((String) a.get().value()).compareTo((String) b.get().value())).dedup().value()
  }

  class ScalaFilterTest extends FilterTest with StandardTest {
    g = TinkerFactory.createClassic

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
    g = TinkerFactory.createClassic

    override def get_g_v1_out_exceptXg_v2X(v1Id: AnyRef, v2Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.except(g.v(v2Id))

    override def get_g_v1_out_aggregate_asXxX_out_exceptXxX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.aggregate.as("x").out.exceptVar("x")

    override def get_g_v1_outXcreatedX_inXcreatedX_exceptXg_v1X_valueXnameX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").in("created")
        .except(g.v(v1Id)).value[String]("name")

    override def get_g_V_exceptXg_VX =
      GremlinScala(g).V.except(GremlinScala(g).V.toList)

    override def get_g_V_exceptXX = GremlinScala(g).V.except(Nil)

    override def get_g_v1_asXxX_bothEXcreatedX_exceptXeX_aggregate_asXeX_otherV_jumpXx_true_trueX_path(v1Id: AnyRef) = ???
    // return g.v(v1Id).as("x").bothE("created").except("e").aggregate("e").otherV().jump("x", x -> true, x -> true).path()
  }

  class ScalaSimplePathTest extends SimplePathTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_v1_outXcreatedX_inXcreatedX_simplePath(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").in("created").simplePath

    override def get_g_V_asXxX_both_simplePath_jumpXx_loops_lt_3X_path = ???
    // return g.V().as("x").both().simplePath().jump("x", t -> t.getLoops() < 3).path()

    override def get_g_V_asXxX_both_simplePath_jumpXx_3X_path = ???
    // return g.V().as("x").both().simplePath().jump("x", 3).path()
  }

  class ScalaCyclicPathTest extends CyclicPathTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_v1_outXcreatedX_inXcreatedX_cyclicPath(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").in("created").cyclicPath

    override def get_g_v1_outXcreatedX_inXcreatedX_cyclicPath_path(v1Id: AnyRef) = ???
    // return g.v(v1Id).out("created").in("created").cyclicPath().path()
  }

  class ScalaHasTest extends HasTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_V_hasXname_markoX = GremlinScala(g).V.has("name", "marko")

    override def get_g_V_hasXname_blahX = GremlinScala(g).V.has("name", "blah")

    override def get_g_V_hasXblahX = GremlinScala(g).V.has("blah")

    override def get_g_v1_out_hasXid_2X(v1Id: AnyRef, v2Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out().has(Element.ID, v2Id)

    override def get_g_V_hasXage_gt_30X = GremlinScala(g).V.has("age", T.gt, 30)

    override def get_g_E_hasXlabelXknowsX = GremlinScala(g).E.has("label", "knows")

    override def get_g_E_hasXlabelXknows_createdX =
      GremlinScala(g).E.has("label", T.in, List("knows", "created"))

    override def get_g_e7_hasXlabelXknowsX(e7Id: AnyRef) = GremlinScala(g).e(e7Id).get.has("label", "knows")

    override def get_g_v1_hasXage_gt_30X(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.has("age", T.gt, 30)

    override def get_g_v1_hasXkeyX(v1Id: AnyRef, key: String) = GremlinScala(g).v(v1Id).get.has(key)

    override def get_g_v1_hasXname_markoX(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.has("name", "marko")

    override def get_g_V_hasXname_equalspredicate_markoX() = GremlinScala(g).V.has("name", "marko")

  }

  class ScalaHasNotTest extends HasNotTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_v1_hasNotXprop(v1Id: AnyRef, prop: String) = GremlinScala(g).v(v1Id).get.hasNot(prop)
    override def get_g_V_hasNotXprop(prop: String) = GremlinScala(g).V.hasNot(prop)
  }

  class ScalaIntervalTest extends IntervalTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_v1_outE_intervalXweight_0_06X_inV(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE.interval("weight", 0f, 0.6f).inV
  }

  class ScalaRandomTest extends RandomTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_V_randomX1X = GremlinScala(g).V.random(1.0d)
    override def get_g_V_randomX0X = GremlinScala(g).V.random(0.0d)
  }

  class ScalaRangeTest extends RangeTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_v1_out_rangeX0_1X(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.out.range(0, 1)

    override def get_g_V_outX1X_rangeX0_2X = GremlinScala(g).V.out(1).range(0, 2)

    override def get_g_v1_outXknowsX_outEXcreatedX_rangeX0_0X_inV(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("knows").outE("created").range(0, 0).inV

    override def get_g_v1_outXknowsX_outXcreatedX_rangeX0_0X(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("knows").out("created").range(0, 0)

    override def get_g_v1_outXcreatedX_inXcreatedX_rangeX1_2X(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").in("created").range(1, 2)

    override def get_g_v1_outXcreatedX_inEXcreatedX_rangeX1_2X_outV(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").inE("created").range(1, 2).outV
  }

  class ScalaRetainTest extends RetainTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_v1_out_retainXg_v2X(v1Id: AnyRef, v2Id: AnyRef) = {
      val v2 = g.v(v2Id)
      GremlinScala(g).v(v1Id).get.out.retainOne(v2)
    }

    override def get_g_v1_out_aggregateXxX_out_retainXxX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.aggregate.as("x").out.retain("x")
  }

  class ScalaBackTest extends BackTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_v1_asXhereX_out_backXhereX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.as("here").out.back[Vertex]("here")

    override def get_g_v4_out_asXhereX_hasXlang_javaX_backXhereX(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.out.as("here").has("lang", "java").back[Vertex]("here")

    override def get_g_v4_out_asXhereX_hasXlang_javaX_backXhereX_valueXnameX(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.out.as("here").has("lang", "java").back[Vertex]("here").value[String]("name")

    override def get_g_v1_outE_asXhereX_inV_hasXname_vadasX_backXhereX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE.as("here").inV.has("name", "vadas").back[Edge]("here")

    override def get_g_v1_outEXknowsX_hasXweight_1X_asXhereX_inV_hasXname_joshX_backXhereX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE("knows").has("weight", 1.0f).as("here").inV.has("name", "josh").back[Edge]("here")
  }

  // class ScalaJumpTest extends JumpTest with StandardTest {
  //   g = TinkerFactory.createClassic
  //
  //   override def get_g_v1_asXxX_out_jumpXx_loops_lt_2X_valueXnameX(v1Id: AnyRef) = ???
  //   //GremlinScala(g).v(v1Id).get.as("x").out.jump("x", h -> h.getLoops() < 2).value[String]("name")
  // }

  class ScalaMapTest extends MapTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_v1_mapXnameX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.map(_.value[String]("name"))

    override def get_g_v1_outE_label_mapXlengthX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE.label.map(_.length: Integer)

    override def get_g_v1_out_mapXnameX_mapXlengthX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.map(_.value[String]("name")).map(_.toString.length: Integer)

    override def get_g_V_asXaX_out_mapXa_nameX = ???
    //GremlinScala(g).V.as("a").out.map(v -> ((Vertex) v.getPath().get("a")).value("name")).trackPaths()
    //GremlinScala(g).V.as("a").out.map(_.getPath.get("a").value[String]("name")).trackPaths

  }

  class ScalaOrderTest extends OrderTest with StandardTest {
    g = TinkerFactory.createClassic
    override def get_g_V_name_order = GremlinScala(g).V.value[String]("name").order

    override def get_g_V_name_orderXabX = GremlinScala(g).V.value[String]("name").order {
      case (a, b) ⇒ a > b
    }

    override def get_g_V_orderXa_nameXb_nameX_name = GremlinScala(g).V.order {
      case (a, b) ⇒
        a.value[String]("name") < b.value[String]("name")
    }.value[String]("name")
  }

  class ScalaSelectTest extends SelectTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_v1_asXaX_outXknowsX_asXbX_select(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.as("a").out("knows").as("b").select()

    //not implementing for now - the same can be achieved by mapping the result later...
    override def get_g_v1_asXaX_outXknowsX_asXbX_selectXnameX(v1Id: AnyRef) = ???
    // GremlinScala(g).v(v1Id).get.as("a").out("knows").as("b").select { v: Vertex ⇒
    //   v.value[String]("name")
    // }

    override def get_g_v1_asXaX_outXknowsX_asXbX_selectXaX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.as("a").out("knows").as("b").select(Seq("a"))

    //not implementing for now - the same can be achieved by mapping the result later...
    override def get_g_v1_asXaX_outXknowsX_asXbX_selectXa_nameX(v1Id: AnyRef) = ???
    //GremlinScala(g).v(v1Id).get.as("a").out("knows").as("b").select(As.of("a"), v -> ((Vertex) v).value("name"))
  }

  class ScalaTraversalTest extends TraversalTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_V = GremlinScala(g).V
    override def get_g_v1_out(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.out
    override def get_g_v2_in(v2Id: AnyRef) = GremlinScala(g).v(v2Id).get.in
    override def get_g_v4_both(v4Id: AnyRef) = GremlinScala(g).v(v4Id).get.both

    override def get_g_v1_outX1_knowsX_name(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out(1, "knows").value[String]("name")

    override def get_g_V_bothX1_createdX_name =
      GremlinScala(g).V.both(1, "created").value[String]("name")

    override def get_g_E = GremlinScala(g).E
    override def get_g_v1_outE(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.outE
    override def get_g_v2_inE(v2Id: AnyRef) = GremlinScala(g).v(v2Id).get.inE
    override def get_g_v4_bothE(v4Id: AnyRef) = GremlinScala(g).v(v4Id).get.bothE
    override def get_g_v4_bothEX1_createdX(v4Id: AnyRef) = GremlinScala(g).v(v4Id).get.bothE(1, "created")
    override def get_g_V_inEX2_knowsX_outV_name = GremlinScala(g).V.inE(2, "knows").outV.value[String]("name")
    override def get_g_v1_outE_inV(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.outE.inV
    override def get_g_v2_inE_outV(v2Id: AnyRef) = GremlinScala(g).v(v2Id).get.inE.outV
    override def get_g_V_outE_hasXweight_1X_outV = GremlinScala(g).V.outE.has("weight", 1.0f).outV

    override def get_g_V_out_outE_inV_inE_inV_both_name =
      GremlinScala(g).V.out.outE.inV.inE.inV.both.value[String]("name")

    override def get_g_v1_outEXknowsX_bothV_name(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE("knows").bothV.value[String]("name")

    override def get_g_v1_outXknowsX(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.out("knows")
    override def get_g_v1_outXknows_createdX(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.out("knows", "created")
    override def get_g_v1_outEXknowsX_inV(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.outE("knows").inV
    override def get_g_v1_outEXknows_createdX_inV(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.outE("knows", "created").inV
    override def get_g_v1_outE_otherV(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.outE.otherV
    override def get_g_v4_bothE_otherV(v4Id: AnyRef) = GremlinScala(g).v(v4Id).get.bothE.otherV
    override def get_g_v4_bothE_hasXweight_lt_1X_otherV(v4Id: AnyRef) = GremlinScala(g).v(v4Id).get.bothE.has("weight", T.lt, 1f).otherV
    override def get_g_V_out_out = GremlinScala(g).V.out.out
    override def get_g_v1_out_out_out(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.out.out.out
    override def get_g_v1_out_valueXnameX(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.out.value[String]("name")
    override def get_g_v1_to_XOUT_knowsX(v1Id: AnyRef) = ??? //GremlinScala(g).v(v1Id).get.to(Direction.OUT, "knows")
  }

  class ScalaValuesTest extends ValuesTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_V_values = GremlinScala(g).V.values()

    override def get_g_V_valuesXname_ageX = GremlinScala(g).V.values("name", "age")

    override def get_g_E_valuesXid_label_weightX =
      GremlinScala(g).E.values("id", "label", "weight")

    override def get_g_v1_outXcreatedX_values(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").values()
  }

  class ScalaAggregateTest extends AggregateTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_V_valueXnameX_aggregate =
      GremlinScala(g).V.value[String]("name").aggregate
        .traversal.asInstanceOf[Traversal[Vertex, JList[String]]]

    override def get_g_V_aggregateXnameX =
      GremlinScala(g).V.aggregate { v: Vertex ⇒ v.value[String]("name") }
        .traversal.asInstanceOf[Traversal[Vertex, JList[String]]]

    override def get_g_V_out_aggregate_asXaX_path = ???
    // return g.V().out().aggregate("a").path()
  }

  class ScalaCountTest extends CountTest with StandardTest {
    g = TinkerFactory.createClassic
    override def get_g_V_count = GremlinScala(g).V.count
    override def get_g_V_out_count = GremlinScala(g).V.out.count
    override def get_g_V_both_both_count = GremlinScala(g).V.both.both.count
    override def get_g_V_filterXfalseX_count = GremlinScala(g).V.filter { _ ⇒ false }.count
  }

  class ScalaSideEffectTest extends sideEffect.SideEffectTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_v1_sideEffectXstore_aX_valueXnameX(v1Id: AnyRef) = {
      val a = new JArrayList[Vertex] //test is expecting a java arraylist..
      GremlinScala(g).v(v1Id).get.`with`(("a", a)).sideEffect { traverser ⇒
        a.add(traverser.get)
      }.value[String]("name")
    }

    override def get_g_v1_out_sideEffectXincr_cX_valueXnameX(v1Id: AnyRef) = {
      val c = new JArrayList[Integer] //test is expecting a java arraylist..
      c.add(0)
      GremlinScala(g).v(v1Id).get.`with`(("c", c)).out.sideEffect { traverser ⇒
        val tmp = c.get(0)
        c.clear()
        c.add(tmp + 1)
      }.value[String]("name")
    }

    override def get_g_v1_out_sideEffectXX_valueXnameX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.sideEffect { traverser: Traverser[Vertex] ⇒
        println("side effect")
      }.value[String]("name")
  }

  class ScalaSideEffectCapTest extends SideEffectCapTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_V_hasXageX_groupCountXnameX_asXaX_out_capXaX = ???
      // GremlinScala(g).V.has("age").groupCount { v: Vertex ⇒
      //   v.value[Int]("age")
      // }.out.cap("a")
      //inconsistent types for groupCount step? is it a side effect step or not?
  }

  class ScalaGroupCountTest extends GroupCountTest with StandardTest {
    g = TinkerFactory.createClassic

    override def get_g_V_outXcreatedX_groupCountXnameX =
      GremlinScala(g).V.out("created").groupCount[String](_.value[String]("name"))
        .traversal.asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

    override def get_g_V_outXcreatedX_name_groupCount =
      GremlinScala(g).V.out("created").value[String]("name").groupCount()

    override def get_g_V_outXcreatedX_name_groupCount_asXaX =
      GremlinScala(g).V.out("created").value[String]("name").groupCount().as("a")

    override def get_g_V_filterXfalseX_groupCount =
      GremlinScala(g).V.filter(_ ⇒ false).groupCount

    override def get_g_V_asXxX_out_groupCountXnameX_asXaX_jumpXx_loops_lt_2X_capXaX = ???
    //GremlinScala(g).V.as("x").out
    // .groupCount(v -> v.value("name")).as("a")
    // .jump("x", h -> h.getLoops < 2).cap("a")

    override def get_g_V_asXxX_out_groupCountXnameX_asXaX_jumpXx_2X_capXaX = ???
    //GremlinScala(g).V.as("x").out
    // .groupCount(v -> v.value("name")).as("a")
    // .jump("x", 2).cap("a")

  }
}

trait StandardTest {
  implicit def toTraversal[S, E](gs: GremlinScala[_, E]): Traversal[S, E] =
    gs.traversal.asInstanceOf[Traversal[S, E]]
}

/* running the tests with the standard TP3 testsuite broke in 5469da9 for some weired reason..
 * bisecting it down showed that just adding a comment in IoTest.java breaks it...
 * falling back to manually calling them in scalatest - that's more flexible anyway
 * downside: cannot reuse what the guys built in tp3 for running tests in multiple dbs
 */
//import Tests._
//import com.tinkerpop.gremlin._
//import org.junit.runners.model.RunnerBuilder
//import org.junit.runner.RunWith
//import java.util.{ Map ⇒ JMap }
//import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
//import org.apache.commons.configuration.Configuration
//import java.io.File
//class ScalaProcessStandardSuite(clazz: Class[_], builder: RunnerBuilder)
//extends AbstractGremlinSuite(clazz, builder, Array( //classOf[ScalaDedupTest],
//classOf[ScalaFilterTest],
//classOf[ScalaExceptTest],
//classOf[ScalaSimplePathTest],
//classOf[ScalaCyclicPathTest],
//classOf[ScalaHasTest]
//))

//@RunWith(classOf[ScalaProcessStandardSuite])
//@AbstractGremlinSuite.GraphProviderClass(classOf[ScalaTinkerGraphProcessStandardTest])
//class ScalaTinkerGraphProcessStandardTest extends AbstractGraphProvider {
//override def getBaseConfiguration(graphName: String): JMap[String, AnyRef] =
//Map("gremlin.graph" -> classOf[TinkerGraph].getName)

//override def clear(graph: Graph, configuration: Configuration): Unit =
//Option(graph) map { graph ⇒
//graph.close()
//if (configuration.containsKey("gremlin.tg.directory"))
//new File(configuration.getString("gremlin.tg.directory")).delete()
//}
//}
