package com.tinkerpop.gremlin.scala

import java.lang.{ Long ⇒ JLong }
import java.util.{ List ⇒ JList, ArrayList ⇒ JArrayList, Map ⇒ JMap, Collection ⇒ JCollection }
import scala.collection.JavaConversions._

import collection.mutable
import com.tinkerpop.gremlin.process._
import com.tinkerpop.gremlin.process.graph.step.filter._
import com.tinkerpop.gremlin.process.graph.step.map._
import com.tinkerpop.gremlin.process.graph.step.sideEffect
import com.tinkerpop.gremlin.process.graph.step.sideEffect._
import com.tinkerpop.gremlin.structure
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
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
      test.g_V_hasXlabelXperson_animalX
      test.get_g_V_hasXname_equalspredicate_markoX
      test.g_V_hasXperson_name_markoX_age
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

    it("retains certain objects", org.scalatest.Tag("foo")) {
      val test = new ScalaRetainTest
      test.g_v1_out_retainXg_v2X
      test.g_v1_out_aggregateXxX_out_retainXxX
    }

    it("retains a given set of objects") {
      // val g = new StandardTest{}. newTestGraphClassicDouble
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
      test.g_v1_outE_asXhereX_inV_hasXname_vadasX_backXhereX
      test.g_v4_out_asXhereX_hasXlang_javaX_backXhereX_valueXnameX
      test.g_v1_outEXknowsX_hasXweight_1X_asXhereX_inV_hasXname_joshX_backXhereX
      test.g_V_asXhereXout_valueXnameX_backXhereX
    }

    it("jumps") {
      val test = new ScalaJumpTest
      test.g_v1_asXxX_out_jumpXx_loops_lt_2X_valueXnameX
      test.g_V_asXxX_out_jumpXx_2X_asXyX_in_jumpXy_2X_name
      test.g_V_asXxX_out_jumpXx_2
      test.g_V_asXxX_out_jumpXx_2_trueX
      test.g_V_jumpXxX_out_out_asXxX
      // test.g_v1_out_jumpXx_t_out_hasNextX_in_jumpXyX_asXxX_out_asXyX_path
      // test.g_V_asXxX_out_jumpXx_loops_lt_2_trueX_path
    }

    it("maps") {
      val test = new ScalaMapTest
      test.g_v1_mapXnameX
      test.g_v1_outE_label_mapXlengthX
      test.g_v1_out_mapXnameX_mapXlengthX
      test.g_V_asXaX_out_out_mapXa_name_it_nameX
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

    it("traverses vertices") {
      val test = new ScalaVertexTest
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
      test.g_v4_bothEX1_knows_createdX
      test.g_v4_bothEXcreateX
      test.g_v4_bothX1X_name
      test.g_v4_bothX2X_name
    }

    // TODO: implement ValueMapTest
    // it("values") {
    //   val test = new ScalaValuesTest
    //   test.g_V_values
    //   test.g_V_valuesXname_ageX
    //   test.g_E_valuesXid_label_weightX
    //   test.g_v1_outXcreatedX_values
    // }
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

    it("allows side effects with cap") {
      val test = new ScalaSideEffectCapTest
      test.g_V_hasXageX_groupCountXnameX_asXaX_out_capXaX
    }

    it("groupCounts", org.scalatest.Tag("foo")) {
      val test = new ScalaGroupCountTest
      test.g_V_outXcreatedX_groupCountXnameX
      test.g_V_outXcreatedX_name_groupCount
      test.g_V_filterXfalseX_groupCount
      test.g_V_asXxX_out_groupCountXnameX_asXaX_jumpXx_2X_capXaX
    }

    it("groupsBy", org.scalatest.Tag("foo")) {
      val test = new ScalaGroupByTest
      test.g_V_groupByXnameX
      test.g_V_hasXlangX_groupByXa_lang_nameX_out_capXaX
      test.g_V_hasXlangX_groupByXlang_1_sizeX
      test.g_V_asXxX_out_groupByXa_name_sizeX_jumpXx_2X_capXaX
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
    g = newTestGraphClassicDouble

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
    g = newTestGraphClassicDouble

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
    g = newTestGraphClassicDouble

    override def get_g_v1_out_exceptXg_v2X(v1Id: AnyRef, v2Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.except(g.v(v2Id))

    override def get_g_v1_out_aggregateXxX_out_exceptXxX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.aggregate("x").out.exceptVar("x")

    override def get_g_v1_outXcreatedX_inXcreatedX_exceptXg_v1X_valueXnameX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").in("created")
        .except(g.v(v1Id)).value[String]("name")

    override def get_g_V_exceptXg_VX =
      GremlinScala(g).V.except(GremlinScala(g).V.toList)

    override def get_g_V_exceptXX = GremlinScala(g).V.except(Nil)

    override def get_g_v1_asXxX_bothEXcreatedX_exceptXeX_aggregateXeX_otherV_jumpXx_true_trueX_path(v1Id: AnyRef) = ???
    // return g.v(v1Id).as("x").bothE("created").except("e").aggregate("e").otherV().jump("x", x -> true, x -> true).path()
  }

  class ScalaSimplePathTest extends SimplePathTest with StandardTest {
    g = newTestGraphClassicDouble

    override def get_g_v1_outXcreatedX_inXcreatedX_simplePath(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").in("created").simplePath

    override def get_g_V_asXxX_both_simplePath_jumpXx_loops_lt_3X_path = ???
    // return g.V().as("x").both().simplePath().jump("x", t -> t.getLoops() < 3).path()

    override def get_g_V_asXxX_both_simplePath_jumpXx_3X_path = ???
    // return g.V().as("x").both().simplePath().jump("x", 3).path()
  }

  class ScalaCyclicPathTest extends CyclicPathTest with StandardTest {
    g = newTestGraphClassicDouble

    override def get_g_v1_outXcreatedX_inXcreatedX_cyclicPath(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out("created").in("created").cyclicPath

    override def get_g_v1_outXcreatedX_inXcreatedX_cyclicPath_path(v1Id: AnyRef) = ???
    // return g.v(v1Id).out("created").in("created").cyclicPath().path()
  }

  class ScalaHasTest extends HasTest with StandardTest {
    g = newTestGraphClassicDouble

    override def get_g_V_hasXname_markoX = GremlinScala(g).V.has("name", "marko")

    override def get_g_V_hasXname_blahX = GremlinScala(g).V.has("name", "blah")

    override def get_g_V_hasXblahX = GremlinScala(g).V.has("blah")

    override def get_g_v1_out_hasXid_2X(v1Id: AnyRef, v2Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out().has(structure.Element.ID, v2Id)

    override def get_g_V_hasXage_gt_30X = GremlinScala(g).V.has("age", T.gt, 30)

    override def get_g_E_hasXlabelXknowsX = GremlinScala(g).E.has("label", "knows")

    override def get_g_E_hasXlabelXknows_createdX =
      GremlinScala(g).E.has("label", T.in, List("knows", "created"))

    override def get_g_e7_hasXlabelXknowsX(e7Id: AnyRef) = GremlinScala(g).e(e7Id).get.has("label", "knows")

    override def get_g_v1_hasXage_gt_30X(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.has("age", T.gt, 30)

    override def get_g_v1_hasXkeyX(v1Id: AnyRef, key: String) = GremlinScala(g).v(v1Id).get.has(key)

    override def get_g_v1_hasXname_markoX(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.has("name", "marko")

    override def get_g_V_hasXlabelXperson_animalX =
      GremlinScala(g).V.has(structure.Element.LABEL, T.in, Seq("person", "animal"))

    override def get_g_V_hasXname_equalspredicate_markoX = GremlinScala(g).V.has("name", "marko")

    override def get_g_V_hasXperson_name_markoX_age = ???
      // GremlinScala(g).V.has("person", "name", "marko").value[Int]("age")
 

  }

  class ScalaHasNotTest extends HasNotTest with StandardTest {
    g = newTestGraphClassicDouble

    override def get_g_v1_hasNotXprop(v1Id: AnyRef, prop: String) = GremlinScala(g).v(v1Id).get.hasNot(prop)
    override def get_g_V_hasNotXprop(prop: String) = GremlinScala(g).V.hasNot(prop)
  }

  class ScalaIntervalTest extends IntervalTest with StandardTest {
    g = newTestGraphClassicDouble

    override def get_g_v1_outE_intervalXweight_0_06X_inV(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE.interval("weight", 0d, 0.6d).inV
  }

  class ScalaRandomTest extends RandomTest with StandardTest {
    g = newTestGraphClassicDouble

    override def get_g_V_randomX1X = GremlinScala(g).V.random(1.0d)
    override def get_g_V_randomX0X = GremlinScala(g).V.random(0.0d)
  }

  class ScalaRangeTest extends RangeTest with StandardTest {
    g = newTestGraphClassicDouble

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
    g = newTestGraphClassicDouble

    override def get_g_v1_out_retainXg_v2X(v1Id: AnyRef, v2Id: AnyRef) = {
      val v2 = g.v(v2Id)
      GremlinScala(g).v(v1Id).get.out.retainOne(v2)
    }

    override def get_g_v1_out_aggregateXxX_out_retainXxX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.aggregate("x").out.retain("x")
  }

  class ScalaBackTest extends BackTest with StandardTest {
    g = newTestGraphClassicDouble

    override def get_g_v1_asXhereX_out_backXhereX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.as("here").out.back[Vertex]("here")

    override def get_g_v4_out_asXhereX_hasXlang_javaX_backXhereX(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.out.as("here").has("lang", "java").back[Vertex]("here")

    override def get_g_v4_out_asXhereX_hasXlang_javaX_backXhereX_valueXnameX(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.out.as("here").has("lang", "java").back[Vertex]("here").value[String]("name")

    override def get_g_v1_outE_asXhereX_inV_hasXname_vadasX_backXhereX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE.as("here").inV.has("name", "vadas").back[Edge]("here")

    override def get_g_v1_outEXknowsX_hasXweight_1X_asXhereX_inV_hasXname_joshX_backXhereX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE("knows").has("weight", 1.0d).as("here").inV.has("name", "josh").back[Edge]("here")

    override def get_g_v1_outEXknowsX_asXhereX_hasXweight_1X_inV_hasXname_joshX_backXhereX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE("knows").as("here").has("weight", 1.0d).inV.has("name", "josh").back[Edge]("here")

    override def get_g_v1_outEXknowsX_asXhereX_hasXweight_1X_asXfakeX_inV_hasXname_joshX_backXhereX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE("knows").has("weight", 1.0d).as("here").inV.has("name", "josh").back[Edge]("here")

    override def get_g_V_asXhereXout_valueXnameX_backXhereX =
      GremlinScala(g).V.as("here").out.value[String]("name").back[Vertex]("here")
  }

  class ScalaJumpTest extends JumpTest with StandardTest {
    g = newTestGraphClassicDouble

    override def get_g_v1_asXxX_out_jumpXx_loops_lt_2X_valueXnameX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.as("x").out
        .jumpWithTraverser("x", _.getLoops < 2)
        .value[String]("name")

    override def get_g_V_asXxX_out_jumpXx_loops_lt_2X =
      GremlinScala(g).V.as("x").out.jumpWithTraverser("x", _.getLoops < 2)

    override def get_g_V_asXxX_out_jumpXx_loops_lt_2_trueX =
      GremlinScala(g).V.as("x").out
        .jumpWithTraverser("x", _.getLoops < 2, _ ⇒ true)

    override def get_g_V_asXxX_out_jumpXx_loops_lt_2_trueX_path = ???
    // GremlinScala(g).V.as("x").out.jump("x", t -> t.getLoops < 2, t -> true).path

    override def get_g_V_asXxX_out_jumpXx_2_trueX_path = ???
    //GremlinScala(g).V.as("x").out.jump("x", 2, t -> true).path

    override def get_g_V_asXxX_out_jumpXx_loops_lt_2X_asXyX_in_jumpXy_loops_lt_2X_name =
      GremlinScala(g).V.as("x").out
        .jumpWithTraverser("x", _.getLoops < 2).as("y").in
        .jumpWithTraverser("y", _.getLoops < 2).value[String]("name")

    override def get_g_V_asXxX_out_jumpXx_2X_asXyX_in_jumpXy_2X_name =
      GremlinScala(g).V.as("x").out
        .jump("x", 2).as("y").in
        .jump("y", 2).value[String]("name")

    override def get_g_V_asXxX_out_jumpXx_2X =
      GremlinScala(g).V.as("x").out.jump("x", 2)

    override def get_g_V_asXxX_out_jumpXx_2_trueX =
      GremlinScala(g).V.as("x").out
        .jumpWithTraverser("x", 2, { _: Traverser[_] ⇒ true })

    override def get_g_v1_out_jumpXx_t_out_hasNextX_in_jumpXyX_asXxX_out_asXyX_path(v1Id: AnyRef) = ???
    //GremlinScala(g).v(v1Id).out.jump("x", t -> t.get.out.hasNext).in.jump("y").as("x").out.as("y").path

    override def get_g_V_jumpXxX_out_out_asXxX =
      GremlinScala(g).V.jump("x").out.out.as("x")

    override def get_g_v1_asXaX_jumpXb_loops_gt_1X_out_jumpXaX_asXbX_name(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.as("a")
        .jumpWithTraverser("b", _.getLoops > 1)
        .out.jump("a").as("b").value[String]("name")

  }

  class ScalaMapTest extends MapTest with StandardTest {
    g = newTestGraphClassicDouble

    override def get_g_v1_mapXnameX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.map(_.value[String]("name"))

    override def get_g_v1_outE_label_mapXlengthX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.outE.label.map(_.length: Integer)

    override def get_g_v1_out_mapXnameX_mapXlengthX(v1Id: AnyRef) =
      GremlinScala(g).v(v1Id).get.out.map(_.value[String]("name")).map(_.toString.length: Integer)

    override def get_g_V_asXaX_out_mapXa_nameX = ???
    //GremlinScala(g).V.as("a").out.map(v -> ((Vertex) v.getPath().get("a")).value("name")).trackPaths()
    //GremlinScala(g).V.as("a").out.map(_.getPath.get("a").value[String]("name")).trackPaths

    override def get_g_V_asXaX_out_out_mapXa_name_it_nameX =
      GremlinScala(g).V.as("a").out.out.mapWithTraverser{ t: Traverser[Vertex] ⇒ 
        val a = t.get[Vertex]("a")
        val aName = a.value[String]("name")
        val vName = t.get.value[String]("name")
        s"$aName$vName"
      }
  }

  class ScalaOrderTest extends OrderTest with StandardTest {
    g = newTestGraphClassicDouble
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
    g = newTestGraphClassicDouble

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

  class ScalaVertexTest extends VertexTest with StandardTest {
    g = newTestGraphClassicDouble

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
    override def get_g_V_outE_hasXweight_1X_outV = GremlinScala(g).V.outE.has("weight", 1.0d).outV

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
    override def get_g_v4_bothE_hasXweight_lt_1X_otherV(v4Id: AnyRef) = GremlinScala(g).v(v4Id).get.bothE.has("weight", T.lt, 1d).otherV
    override def get_g_V_out_out = GremlinScala(g).V.out.out
    override def get_g_v1_out_out_out(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.out.out.out
    override def get_g_v1_out_valueXnameX(v1Id: AnyRef) = GremlinScala(g).v(v1Id).get.out.value[String]("name")
    override def get_g_v1_to_XOUT_knowsX(v1Id: AnyRef) = ??? //GremlinScala(g).v(v1Id).get.to(Direction.OUT, "knows")

    override def get_g_v4_bothEX1_knows_createdX(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.bothE(1, "knows", "created")

    override def get_g_v4_bothEXcreatedX(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.bothE("created")

    override def get_g_v4_bothX1X_name(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.both(1).value[String]("name")

    override def get_g_v4_bothX2X_name(v4Id: AnyRef) =
      GremlinScala(g).v(v4Id).get.both(2).value[String]("name")
  }

  // TODO: implement ValueMapTest
  // class ScalaValuesTest extends ValuesTest with StandardTest {
  //   g = newTestGraphClassicDouble
  //
  //   override def get_g_V_values = GremlinScala(g).V.values()
  //
  //   override def get_g_V_valuesXname_ageX = GremlinScala(g).V.values("name", "age")
  //
  //   override def get_g_E_valuesXid_label_weightX =
  //     GremlinScala(g).E.values("id", "label", "weight")
  //
  //   override def get_g_v1_outXcreatedX_values(v1Id: AnyRef) =
  //     GremlinScala(g).v(v1Id).get.out("created").values()
  // }

  class ScalaAggregateTest extends AggregateTest with StandardTest {
    g = newTestGraphClassicDouble

    override def get_g_V_valueXnameX_aggregate =
      GremlinScala(g).V.value[String]("name").aggregate
        .traversal.asInstanceOf[Traversal[Vertex, JList[String]]]

    override def get_g_V_aggregateXnameX =
      GremlinScala(g).V.aggregate { v: Vertex ⇒ v.value[String]("name") }
        .traversal.asInstanceOf[Traversal[Vertex, JList[String]]]

    override def get_g_V_out_aggregateXaX_path = ???
    // return g.V().out().aggregate("a").path()
  }

  class ScalaCountTest extends CountTest with StandardTest {
    g = newTestGraphClassicDouble
    override def get_g_V_count = GremlinScala(g).V.count
    override def get_g_V_out_count = GremlinScala(g).V.out.count
    override def get_g_V_both_both_count = GremlinScala(g).V.both.both.count
    override def get_g_V_filterXfalseX_count = GremlinScala(g).V.filter { _ ⇒ false }.count
  }

  class ScalaSideEffectTest extends sideEffect.SideEffectTest with StandardTest {
    g = newTestGraphClassicDouble

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
    g = newTestGraphClassicDouble

    override def get_g_V_hasXageX_groupCountXa_nameX_out_capXaX =
      GremlinScala(g).V.has("age")
        .groupCount("a", _.value[String]("name"))
        .out.cap("a")
        .asInstanceOf[Traversal[Vertex, JMap[String, JLong]]] //only for Scala 2.10...
  }

  class ScalaGroupCountTest extends GroupCountTest with StandardTest {
    g = newTestGraphClassicDouble

    override def get_g_V_outXcreatedX_groupCountXnameX =
      GremlinScala(g).V.out("created").groupCount[String]({ v: Vertex ⇒ v.value[String]("name") })
        .traversal.asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

    override def get_g_V_outXcreatedX_name_groupCount =
      GremlinScala(g).V.out("created").value[String]("name").groupCount()
        .traversal.asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

    override def get_g_V_outXcreatedX_name_groupCountXaX =
      GremlinScala(g).V.out("created").value[String]("name").groupCount().as("a")
        .traversal.asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

    override def get_g_V_filterXfalseX_groupCount =
      GremlinScala(g).V.filter(_ ⇒ false).groupCount()
        .traversal.asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

    override def get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_loops_lt_2X_capXaX =
      GremlinScala(g).V.as("x").out
        .groupCount("a", _.value[String]("name"))
        .jumpWithTraverser("x", _.getLoops < 2).cap("a")
        .asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]] //only for Scala 2.10...

    override def get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_2X_capXaX =
      GremlinScala(g).V.as("x").out
        .groupCount("a", _.value[String]("name"))
        .jump("x", 2).cap("a")
        .asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]] //only for Scala 2.10...

  }

  class ScalaGroupByTest extends GroupByTest with StandardTest {
    g = newTestGraphClassicDouble

    override def get_g_V_groupByXnameX =
      GremlinScala(g).V.groupBy(_.value[String]("name"))
        .traversal.asInstanceOf[Traversal[Vertex, JMap[String, JList[Vertex]]]]

    override def get_g_V_hasXlangX_groupByXa_lang_nameX_out_capXaX =
      GremlinScala(g).V.has("lang").groupBy(
        sideEffectKey = "a",
        keyFunction = _.value[String]("lang"),
        valueFunction = _.value[String]("name")
      ).as("a").out.cap("a")
        .asInstanceOf[Traversal[Vertex, JMap[String, JList[String]]]] //only for Scala 2.10...

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
        ).as("a").jump("x", 2).cap("a")
        .asInstanceOf[Traversal[Vertex, JMap[String, Integer]]] //only for Scala 2.10...

    override def get_g_V_asXxX_out_groupByXa_name_sizeX_jumpXx_loops_lt_2X_capXaX =
      GremlinScala(g).V.as("x").out
        .groupBy(
          sideEffectKey = "a",
          keyFunction = _.value[String]("name"),
          valueFunction = v ⇒ v,
          reduceFunction = { c: JCollection[_] ⇒ c.size }
        ).jumpWithTraverser("x", _.getLoops < 2).cap("a")
        .asInstanceOf[Traversal[Vertex, JMap[String, Integer]]] //only for Scala 2.10...
  }

}

trait StandardTest {
  def newTestGraphClassicDouble = {
    val g = ScalaGraph(TinkerGraph.open)

    val marko = g.addVertex(1: Integer, Map("name" → "marko", "age" → 29))
    val vadas = g.addVertex(2: Integer, Map("name" → "vadas", "age" → 27))
    val lop = g.addVertex(3: Integer, Map("name" → "lop", "lang" → "java"))
    val josh = g.addVertex(4: Integer, Map("name" → "josh", "age" → 32))
    val ripple = g.addVertex(5: Integer, Map("name" → "ripple", "lang" → "java"))
    val peter = g.addVertex(6: Integer, Map("name" → "peter", "age" → 35))

    marko.addEdge(7: Integer, "knows", vadas, Map("weight" → 0.5d))
    marko.addEdge(8: Integer, "knows", josh, Map("weight" → 1.0d))
    marko.addEdge(9: Integer, "created", lop, Map("weight" → 0.4d))
    josh.addEdge(10: Integer, "created", ripple, Map("weight" → 1.0d))
    josh.addEdge(11: Integer, "created", lop, Map("weight" → 0.4d))
    peter.addEdge(12: Integer, "created", lop, Map("weight" → 0.2d))
    g
  }

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
