package com.tinkerpop.gremlin.scala

import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.process._
import com.tinkerpop.gremlin.process.graph.step.filter._
import com.tinkerpop.gremlin.process.graph.step.map._
import com.tinkerpop.gremlin.structure.Element
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory

class StandardTests extends TestBase {
  import Tests._

  describe("filter steps") {
    it("dedups") {
      val test = new ScalaDedupTest
      test.g_V_both_dedup_name
      test.g_V_both_dedupXlangX_name
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
    }

    it("finds the simple path") {
      val test = new ScalaSimplePathTest
      test.g_v1_outXcreatedX_inXcreatedX_simplePath
    }

    it("finds the cyclic path") {
      val test = new ScalaCyclicPathTest
      test.g_v1_outXcreatedX_inXcreatedX_simplePath
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
      val graph = ScalaGraph(g)
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
      val test = new ScalaJumpTest
      test.g_v1_asXxX_out_jumpXx_loops_lt_2X_valueXnameX
    }

    it("maps") {
      val test = new ScalaMapTest
      test.g_v1_mapXnameX
      test.g_v1_outE_label_mapXlengthX
      test.g_v1_out_mapXnameX_transformXlengthX
      //test.g_V_asXaX_out_mapXa_nameX
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
    g = TinkerFactory.createClassic()

    override def get_g_V_both_dedup_name = ScalaGraph(g).V.both.dedup.value[String]("name")

    override def get_g_V_both_dedupXlangX_name =
      ScalaGraph(g).V.both
        .dedup(_.property[String]("lang").orElse(null))
        .value[String]("name")
  }

  class ScalaFilterTest extends FilterTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_V_filterXfalseX = ScalaGraph(g).V.filter(_ ⇒ false)

    override def get_g_V_filterXtrueX = ScalaGraph(g).V.filter(_ ⇒ true)

    override def get_g_V_filterXlang_eq_javaX = 
      ScalaGraph(g).V.filter(_.property("lang").orElse("none") == "java")

    override def get_g_v1_out_filterXage_gt_30X(v1Id: AnyRef) = 
      ScalaGraph(g).v(v1Id).get.out.filter(_.property("age").orElse(0) > 30)

    override def get_g_V_filterXname_startsWith_m_OR_name_startsWith_pX = ScalaGraph(g).V.filter { v ⇒
      val name = v.value[String]("name")
      name.startsWith("m") || name.startsWith("p")
    }

    override def get_g_E_filterXfalseX = ScalaGraph(g).E.filter(_ ⇒ false)

    override def get_g_E_filterXtrueX = ScalaGraph(g).E.filter(_ ⇒ true)

    override def get_g_v1_filterXage_gt_30X(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.filter(_.property("age").orElse(0) > 30)
  }

  class ScalaExceptTest extends ExceptTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_v1_out_exceptXg_v2X(v1Id: AnyRef, v2Id: AnyRef) = 
      ScalaGraph(g).v(v1Id).get.out.except(g.v(v2Id))
  
    override def get_g_v1_out_aggregateXxX_out_exceptXxX(v1Id: AnyRef) = 
      ScalaGraph(g).v(v1Id).get.out.aggregate("x").out.exceptVar("x")

    override def get_g_v1_outXcreatedX_inXcreatedX_exceptXg_v1X_valueXnameX(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.out("created").in("created")
        .except(g.v(v1Id)).value[String]("name")
  }

  class ScalaSimplePathTest extends SimplePathTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_v1_outXcreatedX_inXcreatedX_simplePath(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.out("created").in("created").simplePath
  }

  class ScalaCyclicPathTest extends CyclicPathTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_v1_outXcreatedX_inXcreatedX_cyclicPath(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.out("created").in("created").cyclicPath
  }

  class ScalaHasTest extends HasTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_V_hasXname_markoX = ScalaGraph(g).V.has("name", "marko")

    override def get_g_V_hasXname_blahX = ScalaGraph(g).V.has("name", "blah")

    override def get_g_V_hasXblahX = ScalaGraph(g).V.has("blah")

    override def get_g_v1_out_hasXid_2X(v1Id: AnyRef, v2Id: AnyRef) = 
      ScalaGraph(g).v(v1Id).get.out().has(Element.ID, v2Id)

    override def get_g_V_hasXage_gt_30X = ScalaGraph(g).V.has("age", T.gt, 30)

    override def get_g_E_hasXlabelXknowsX = ScalaGraph(g).E.has("label", "knows")

    override def get_g_E_hasXlabelXknows_createdX =
      ScalaGraph(g).E.has("label", T.in, List("knows", "created"))

    override def get_g_e7_hasXlabelXknowsX(e7Id: AnyRef) = ScalaGraph(g).e(e7Id).get.has("label", "knows")

    override def get_g_v1_hasXage_gt_30X(v1Id: AnyRef) = ScalaGraph(g).v(v1Id).get.has("age", T.gt, 30)

    override def get_g_v1_hasXkeyX(v1Id: AnyRef, key: String) = ScalaGraph(g).v(v1Id).get.has(key)

    override def get_g_v1_hasXname_markoX(v1Id: AnyRef) = ScalaGraph(g).v(v1Id).get.has("name", "marko")

    override def get_g_V_hasXname_equalspredicate_markoX() = ScalaGraph(g).V.has("name", "marko")

  }

  class ScalaHasNotTest extends HasNotTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_v1_hasNotXprop(v1Id: AnyRef, prop: String) = ScalaGraph(g).v(v1Id).get.hasNot(prop)
    override def get_g_V_hasNotXprop(prop: String) = ScalaGraph(g).V.hasNot(prop)
  }

  class ScalaIntervalTest extends IntervalTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_v1_outE_intervalXweight_0_06X_inV(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.outE.interval("weight", 0f, 0.6f).inV
  }

  class ScalaRandomTest extends RandomTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_V_randomX1X = ScalaGraph(g).V.random(1.0d)
    override def get_g_V_randomX0X = ScalaGraph(g).V.random(0.0d)
  }

  class ScalaRangeTest extends RangeTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_v1_out_rangeX0_1X(v1Id: AnyRef) = ScalaGraph(g).v(v1Id).get.out.range(0,1)

    override def get_g_V_outX1X_rangeX0_2X = ScalaGraph(g).V.out(1).range(0,2)

    override def get_g_v1_outXknowsX_outEXcreatedX_rangeX0_0X_inV(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.out("knows").outE("created").range(0, 0).inV

    override def get_g_v1_outXknowsX_outXcreatedX_rangeX0_0X(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.out("knows").out("created").range(0, 0)

    override def get_g_v1_outXcreatedX_inXcreatedX_rangeX1_2X(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.out("created").in("created").range(1, 2)

    override def get_g_v1_outXcreatedX_inEXcreatedX_rangeX1_2X_outV(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.out("created").inE("created").range(1, 2).outV
  }

  class ScalaRetainTest extends RetainTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_v1_out_retainXg_v2X(v1Id: AnyRef, v2Id: AnyRef) = {
      val v2 = g.v(v2Id)
      ScalaGraph(g).v(v1Id).get.out.retainOne(v2)
    }

    override def get_g_v1_out_aggregateXxX_out_retainXxX(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.out.aggregate("x").out.retain("x")
  }

  class ScalaBackTest extends BackTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_v1_asXhereX_out_backXhereX(v1Id: AnyRef) = 
      ScalaGraph(g).v(v1Id).get.as("here").out.back[Vertex]("here")

    override def get_g_v4_out_asXhereX_hasXlang_javaX_backXhereX(v4Id: AnyRef) = 
      ScalaGraph(g).v(v4Id).get.out.as("here").has("lang", "java").back[Vertex]("here")

    override def get_g_v4_out_asXhereX_hasXlang_javaX_backXhereX_valueXnameX(v4Id: AnyRef) = 
      ScalaGraph(g).v(v4Id).get.out.as("here").has("lang", "java").back[Vertex]("here").value[String]("name")

    override def get_g_v1_outE_asXhereX_inV_hasXname_vadasX_backXhereX(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.outE.as("here").inV.has("name", "vadas").back[Edge]("here")

    override def get_g_v1_outEXknowsX_hasXweight_1X_asXhereX_inV_hasXname_joshX_backXhereX(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.outE("knows").has("weight", 1.0f).as("here").inV.has("name", "josh").back[Edge]("here")
  }

  class ScalaJumpTest extends JumpTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_v1_asXxX_out_jumpXx_loops_lt_2X_valueXnameX(v1Id: AnyRef) = ???
      //ScalaGraph(g).v(v1Id).get.as("x").out.jump("x", h -> h.getLoops() < 2).value[String]("name")
  }

  class ScalaMapTest extends MapTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_v1_mapXnameX(v1Id: AnyRef) = 
      ScalaGraph(g).v(v1Id).get.map(_.get.value[String]("name"))

    override def get_g_v1_outE_label_mapXlengthX(v1Id: AnyRef) = 
      ScalaGraph(g).v(v1Id).get.outE.label.map(_.get.length: Integer)

    override def get_g_v1_out_mapXnameX_transformXlengthX(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.out.map(_.get.value[String]("name")).map(_.get.toString().length: Integer)

    override def get_g_V_asXaX_out_mapXa_nameX = ???
      //ScalaGraph(g).V.as("a").out.map(v -> ((Vertex) v.getPath().get("a")).value("name")).trackPaths()
      //ScalaGraph(g).V.as("a").out.map(_.getPath.get("a").value[String]("name")).trackPaths
  }

}

trait StandardTest {
  implicit def toTraversal[S,E](gs: GremlinScala[_,E]): Traversal[S,E] =
    gs.traversal.asInstanceOf[Traversal[S,E]]
}

/* running the tests with the standard TP3 testsuite broke in 5469da9 for some weired reason..
 * bisecting it down showed that just adding a comment in IoTest.java breaks it...
 * falling back to manually calling them in scalatest - that's more flexible anyway
 * downside: cannot reuse what the guys built in tp3 for running tests in multiple dbs
 */
//import Tests._
//class ScalaProcessStandardSuite(clazz: Class[_], builder: RunnerBuilder) 
  //extends AbstractGremlinSuite(clazz, builder, Array(
    //classOf[ScalaDedupTest],
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
