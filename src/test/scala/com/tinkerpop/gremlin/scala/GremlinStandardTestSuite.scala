package com.tinkerpop.gremlin.scala

import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.process._
import com.tinkerpop.gremlin.process.graph.filter._
import com.tinkerpop.gremlin.structure.Element
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory

class StandardTests extends TestBase {
  import Tests._

  it("dedups") {
    val test = new ScalaDedupTest
    test.get_g_V_both_dedup_name.toSet shouldBe allNames
    test.get_g_V_both_dedupXlangX_name.toSet shouldBe Set("lop", "vadas")
  }

  it("filters") {
    val test = new ScalaFilterTest
    test.get_g_V_filterXfalseX.toSet shouldBe Set.empty
    test.get_g_V_filterXtrueX.toSet map getId shouldBe allVertexIds
    test.get_g_V_filterXlang_eq_javaX.toSet map getId shouldBe Set(v3Id, v5Id)
    test.get_g_v1_out_filterXage_gt_30X(v1Id).toSet map getId shouldBe Set(v4Id)
    test.get_g_V_filterXname_startsWith_m_OR_name_startsWith_pX.toSet map getId shouldBe Set(v1Id, v6Id)
    test.get_g_E_filterXfalseX.toSet shouldBe Set.empty
    test.get_g_E_filterXtrueX.toSet map getId shouldBe allEdgeIds
    test.get_g_v1_filterXage_gt_30X(v1Id).toSet shouldBe Set.empty
  }

  it("excepts") {
    val test = new ScalaExceptTest
    test.get_g_v1_out_exceptXg_v2X(v1Id, v2Id).toSet map getId shouldBe Set(v3Id, v4Id)
    test.get_g_v1_out_aggregateXxX_out_exceptXxX(v1Id).toSet map getId shouldBe Set(v5Id)
    test.get_g_v1_outXcreatedX_inXcreatedX_exceptXg_v1X_valueXnameX(v1Id).toSet shouldBe Set("josh", "peter")
  }

  it("finds the simple path") {
    val test = new ScalaSimplePathTest
    test.get_g_v1_outXcreatedX_inXcreatedX_simplePath(v1Id).toSet map getId shouldBe Set(v4Id, v6Id)
  }

  it("finds the cyclic path") {
    val test = new ScalaCyclicPathTest
    test.get_g_v1_outXcreatedX_inXcreatedX_cyclicPath(v1Id).toSet map getId shouldBe Set(v1Id)
  }

  it("filters with has") {
    val test = new ScalaHasTest
    test.get_g_V_hasXname_markoX.toSet map getId shouldBe Set(v1Id)
    test.get_g_V_hasXname_blahX.toSet.size shouldBe 0
    test.get_g_V_hasXblahX.toSet.size shouldBe 0
    test.get_g_v1_out_hasXid_2X(v1Id, v2Id).toSet map getId shouldBe Set(v2Id)
    test.get_g_V_hasXage_gt_30X.toSet map getId shouldBe Set(v4Id, v6Id)
    test.get_g_E_hasXlabelXknowsX.toSet map getId shouldBe Set(e7Id, e8Id)
    test.get_g_E_hasXlabelXknows_createdX.toSet map getId shouldBe allEdgeIds
    test.get_g_e7_hasXlabelXknowsX(e7Id).toSet map getId shouldBe Set(e7Id)
    test.get_g_v1_hasXage_gt_30X(v1Id).toSet.size shouldBe 0
    test.get_g_v1_hasXkeyX(v1Id, "circumference").toSet.size shouldBe(0)
    test.get_g_v1_hasXname_markoX(v1Id).toSet map getId shouldBe Set(v1Id)
  }

  it("filters with has not") {
    val test = new ScalaHasNotTest
    test.get_g_V_hasNotXprop("circumference").toList.size shouldBe 6
    test.get_g_v1_hasNotXprop(v1Id, "circumference").toList.size shouldBe 1
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
  }

  class ScalaHasNotTest extends HasNotTest with StandardTest {
    g = TinkerFactory.createClassic()

    override def get_g_v1_hasNotXprop(v1Id: AnyRef, prop: String) = ScalaGraph(g).v(v1Id).get.hasNot(prop)
    override def get_g_V_hasNotXprop(prop: String) = ScalaGraph(g).V.hasNot(prop)
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
