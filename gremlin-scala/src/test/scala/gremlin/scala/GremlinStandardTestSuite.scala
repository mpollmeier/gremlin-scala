package gremlin.scala

import java.lang.{Double ⇒ JDouble, Long ⇒ JLong}
import java.util.{ArrayList ⇒ JArrayList, Collection ⇒ JCollection, List ⇒ JList, Map ⇒ JMap, Set ⇒ JSet}

import org.apache.tinkerpop.gremlin.process.traversal._
import org.apache.tinkerpop.gremlin.process.traversal.step.filter._
import org.apache.tinkerpop.gremlin.process.traversal.step.map._
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect._
import org.apache.tinkerpop.gremlin.structure.T
import schema.Key
import TestGraph._

import scala.collection.JavaConversions._

object Tests {

  // class ScalaDedupTest extends DedupTest with StandardTest {
  //   override def get_g_V_both_dedup_name =
  //     graph.asScala.V.both.dedup().values[String]("name")

  //   override def get_g_V_both_hasXlabel_softwareX_dedup_byXlangX_name =
  //     graph.asScala.V.both.has(T.label, "software").dedup().by("lang").values[String]("name")

  //   override def get_g_V_both_name_order_byXa_bX_dedup_value =
  //     graph.asScala.V.both.values[String]("name").order.by { (a, b) ⇒ a < b }.dedup()

  //   override def get_g_V_both_both_name_dedup =
  //     graph.asScala.V.both.both.values[String]("name").dedup()

  //   override def get_g_V_both_both_dedup =
  //     graph.asScala.V.both.both.dedup()

  //   override def get_g_V_both_both_dedup_byXlabelX =
  //     graph.asScala.V.both.both.dedup().by(T.label)

  //   // override def get_g_V_asXaX_both_asXbX_dedupXa_bX_byXlabelX_selectXa_bX =
  //   //   graph.asScala.V.as("a").both.as("b").dedup("a", "b").by(T.label).select("a", "b")
  //   //     .asInstanceOf[GremlinScala[JMap[String, Vertex], _]]

  //   override def get_g_V_asXaX_outXcreatedX_asXbX_inXcreatedX_asXcX_dedupXa_bX_path =
  //     graph.asScala.V.as("a").out("created").as("b").in("created").as("c").dedup("a", "b").path

  //   override def get_g_V_group_byXlabelX_byXbothE_valuesXweightX_foldX_byXdedupXlocalXX =
  //     graph.asScala.V.group()
  //       .by(T.label)
  //       .by(__.bothE().values[java.lang.Float]("weight").fold())
  //       .by(__.dedup(Scope.local))
  //       .asInstanceOf[GremlinScala[JMap[String, JSet[JDouble]], _]]
  //   //TODO: get rid of cast

  //   override def get_g_V_outE_asXeX_inV_asXvX_selectXeX_order_byXweight_incrX_selectXvX_valuesXnameX_dedup =
  //     graph.asScala.V.outE.as("e").inV.as("v")
  //       .select[Edge]("e").order.by("weight", Order.incr)
  //       .select[Vertex]("v").values[String]("name").dedup()
  // }

  class ScalaFilterTest extends FilterTest with StandardTest {
    override def get_g_V_filterXfalseX = graph.asScala.V.filter(_ ⇒ false)

    override def get_g_V_filterXtrueX = graph.asScala.V.filter(_ ⇒ true)

    override def get_g_V_filterXlang_eq_javaX =
      graph.asScala.V.filter(_.property("lang").orElse("none") == "java")

    override def get_g_VX1X_filterXage_gt_30X(v1Id: AnyRef) =
      graph.asScala.V(v1Id).filter(_.property[Int]("age").orElse(0) > 30)

    override def get_g_V_filterXname_startsWith_m_OR_name_startsWith_pX = graph.asScala.V.filter { v ⇒
      val name = v.value[String]("name")
      name.startsWith("m") || name.startsWith("p")
    }

    override def get_g_VX1X_out_filterXage_gt_30X(v1Id: AnyRef) =
      graph.asScala.V(v1Id).out.filter(_.property[Int]("age").orElse(0) > 30)

    override def get_g_E_filterXfalseX = graph.asScala.E.filter(_ ⇒ false)

    override def get_g_E_filterXtrueX = graph.asScala.E.filter(_ ⇒ true)
  }

  class ScalaSimplePathTest extends SimplePathTest with StandardTest {
    override def get_g_VX1X_outXcreatedX_inXcreatedX_simplePath(v1Id: AnyRef) =
      graph.asScala.V(v1Id).out("created").in("created").simplePath

    override def get_g_V_repeatXboth_simplePathX_timesX3X_path =
      graph.asScala.V.repeat(_.both.simplePath).times(3).path
  }

  class ScalaCyclicPathTest extends CyclicPathTest with StandardTest {
    override def get_g_VX1X_outXcreatedX_inXcreatedX_cyclicPath(v1Id: AnyRef) =
      graph.asScala.V(v1Id).out("created").in("created").cyclicPath

    override def get_g_VX1X_outXcreatedX_inXcreatedX_cyclicPath_path(v1Id: AnyRef) =
      graph.asScala.V(v1Id).out("created").in("created").cyclicPath.path
  }

  class ScalaHasTest extends HasTest with StandardTest {
    override def get_g_EX11X_outV_outE_hasXid_10X(e11Id: AnyRef, e8Id: AnyRef) = {
      def a = graph.asScala.E(e11Id).outV.outE.has(T.id, e8Id)

      // println("XXXXXXXXXXXXX")
      // println(e8Id)
      // println(GS(graph).E(e8Id).toList)
      // a.toList foreach println
      // println("XXXXXXXXXXXXX")

      a
    }

    override def get_g_V_outXcreatedX_hasXname__mapXlengthX_isXgtX3XXX_name =
      graph.asScala.V.out("created")
        .has[String, Int](Name, _.map(_.length).is(P.gt(3)))
        .value(Name)

    override def get_g_VX1X_hasXkeyX(v1Id: AnyRef, key: String) =
      graph.asScala.V(v1Id).has(Key(key))

    override def get_g_VX1X_hasXname_markoX(v1Id: AnyRef) =
      graph.asScala.V(v1Id).has(Name, "marko")

    override def get_g_V_hasXname_markoX =
      graph.asScala.V.has(Name, "marko")

    override def get_g_V_hasXname_blahX =
      graph.asScala.V.has(Name, "blah")

    override def get_g_V_hasXblahX =
      graph.asScala.V.has(Key("blah"))

    override def get_g_VX1X_hasXage_gt_30X(v1Id: AnyRef) =
      graph.asScala.V(v1Id).has(Age, P.gt(30))

    override def get_g_VX1X_out_hasIdX2X(v1Id: AnyRef, v2Id: AnyRef) = {
      def a = graph.asScala.V(v1Id).out.hasId(v2Id)
      // println("XXXXXXXXXXXXX")
      // println(a.toList)
      // println("XXXXXXXXXXXXXY")
      a
    }

    override def get_g_VX1X_out_hasIdX2_3X(v1Id: AnyRef, v2Id: AnyRef, v3Id: AnyRef) =
      graph.asScala.V(v1Id).out.hasId(v2Id, v3Id)

    override def get_g_V_hasXage_gt_30X = {
      def a = graph.asScala.V.has(Age, P.gt(30))
      // def b = g.V().has("age", P.gt(30))
      // def b: GraphTraversal[Vertex, Vertex] = g.V().has("age", P.gt(30))
      // println("XXXXXXXXXXXXXXXXXX")
      // println(b.toList)
      // println("YYYYYYYYYYYYYYYY")
      // b
      a
    }

    override def get_g_EX7X_hasLabelXknowsX(e7Id: AnyRef) =
      graph.asScala.E(e7Id).hasLabel("knows")

    override def get_g_E_hasLabelXknowsX =
      graph.asScala.E.hasLabel("knows")

    override def get_g_E_hasLabelXuses_traversesX =
      graph.asScala.E.hasLabel("uses", "traverses")

    override def get_g_V_hasLabelXperson_software_blahX =
      graph.asScala.V.hasLabel("person", "software", "blah")

    override def get_g_V_hasXperson_name_markoX_age =
      graph.asScala.V.has("person", Name, "marko").value(Key[Integer]("age"))

    override def get_g_VX1X_outE_hasXweight_inside_0_06X_inV(v1Id: AnyRef) =
      graph.asScala.V(v1Id).outE.has(Weight, P.inside(0.0d, 0.6d)).inV

    override def get_g_VX1X_out_hasXid_lt_3X(v1Id: AnyRef, v3Id: AnyRef) =
      graph.asScala.V(v1Id).out.has(T.id, P.lt(v3Id))

    override def get_g_V_hasXage_isXgt_30XX =
      graph.asScala.V.has(Key[Any]("age"), __.is(P.gt(30)))

    override def get_g_V_hasXlocationX =
      graph.asScala.V.has(Location)

    override def get_g_VXv1X_hasXage_gt_30X(v1Id: AnyRef) =
      graph.asScala.V(graph.V(v1Id).head).has(Age, P.gt(30))
  }

  class ScalaCoinTest extends CoinTest with StandardTest {
    override def get_g_V_coinX1X = graph.asScala.V.coin(1.0d)

    override def get_g_V_coinX0X = graph.asScala.V.coin(0.0d)
  }

  // class ScalaRangeTest extends RangeTest with StandardTest {
  //       override def get_g_VX1X_out_limitX2X(v1Id: AnyRef) =
  //         graph.asScala.V(v1Id).out.limit(2)

  //       override def get_g_V_localXoutE_limitX1X_inVX_limitX3X =
  //         graph.asScala.V.local(_.outE.limit(1)).inV.limit(3)

  //       override def get_g_VX1X_outXknowsX_outEXcreatedX_rangeX0_1X_inV(v1Id: AnyRef) =
  //         graph.asScala.V(v1Id).out("knows").outE("created").range(0, 1).inV

  //       override def get_g_VX1X_outXknowsX_outXcreatedX_rangeX0_1X(v1Id: AnyRef) =
  //         graph.asScala.V(v1Id).out("knows").out("created").range(0, 1)

  //       override def get_g_VX1X_outXcreatedX_inXcreatedX_rangeX1_3X(v1Id: AnyRef) =
  //         graph.asScala.V(v1Id).out("created").in("created").range(1, 3)

  //       override def get_g_VX1X_outXcreatedX_inEXcreatedX_rangeX1_3X_outV(v1Id: AnyRef) =
  //         graph.asScala.V(v1Id).out("created").inE("created").range(1, 3).outV

  //       override def get_g_V_repeatXbothX_timesX3X_rangeX5_11X =
  //         graph.asScala.V.repeat(_.both).times(3).range(5, 11)

  // override def get_g_V_asXaX_in_asXaX_in_asXaX_selectXaX_byXunfold_valuesXnameX_foldX_limitXlocal_2X =
  // graph.asScala.V.as("a").in.as("a").in.as("a").select[JList[String]]("a").by(unfold.values("name").fold).limit(local, 2)

  // override def get_g_V_asXaX_in_asXaX_in_asXaX_selectXaX_byXunfold_valuesXnameX_foldX_limitXlocal_1X =
  //   graph.asScala.V.as("a").in.as("a").in.as("a").select[JList[String]]("a").by(unfold.values("name").fold).limit(local, 1)

  // override def get_g_V_asXaX_out_asXaX_out_asXaX_selectXaX_byXunfold_valuesXnameX_foldX_rangeXlocal_1_3X =
  //   graph.asScala.V.as("a").out.as("a").out.as("a").select[List<String>]("a").by(unfold.values("name").fold).range(local, 1, 3)

  // override def get_g_V_asXaX_out_asXaX_out_asXaX_selectXaX_byXunfold_valuesXnameX_foldX_rangeXlocal_1_2X =
  //   graph.asScala.V.as("a").out.as("a").out.as("a").select[List<String>]("a").by(unfold.values("name").fold).range(local, 1, 2)

  // override def get_g_V_asXaX_out_asXaX_out_asXaX_selectXaX_byXunfold_valuesXnameX_foldX_rangeXlocal_4_5X =
  //   graph.asScala.V.as("a").out.as("a").out.as("a").select[List<String>]("a").by(unfold.values("name").fold).range(local, 4, 5)

  // override def get_g_V_asXaX_in_asXbX_in_asXcX_selectXa_b_cX_byXnameX_limitXlocal_2X =
  //   graph.asScala.V.as("a").in.as("b").in.as("c").select[Map<String, String>]("a","b","c").by("name").limit(local, 2)

  // override def get_g_V_asXaX_in_asXbX_in_asXcX_selectXa_b_cX_byXnameX_limitXlocal_1X =
  //   graph.asScala.V.as("a").in.as("b").in.as("c").select[Map<String, String>]("a","b","c").by("name").limit(local, 1)

  // override def get_g_V_asXaX_out_asXbX_out_asXcX_selectXa_b_cX_byXnameX_rangeXlocal_1_3X =
  //   graph.asScala.V.as("a").out.as("b").out.as("c").select[Map<String, String>]("a","b","c").by("name").range(local, 1, 3)

  // override def get_g_V_asXaX_out_asXbX_out_asXcX_selectXa_b_cX_byXnameX_rangeXlocal_1_2X =
  //   graph.asScala.V.as("a").out.as("b").out.as("c").select[Map<String, String>]("a","b","c").by("name").range(local, 1, 2)

  // override def get_g_VX1X_out_limitX2X(v1Id: AnyRef) =
  //   graph.asScala.V(v1Id).out.limit(2)

  // override def get_g_V_localXoutE_limitX1X_inVX_limitX3X =
  //   graph.asScala.V.local(_.outE.limit(1)).inV.limit(3)

  // override def get_g_VX1X_outXknowsX_outEXcreatedX_rangeX0_1X_inV(v1Id: AnyRef) =
  //   graph.asScala.V(v1Id).out("knows").outE("created").range(0, 1).inV

  // override def get_g_VX1X_outXknowsX_outXcreatedX_rangeX0_1X(v1Id: AnyRef) =
  //   graph.asScala.V(v1Id).out("knows").out("created").range(0, 1)

  // override def get_g_VX1X_outXcreatedX_inXcreatedX_rangeX1_3X(v1Id: AnyRef) =
  //   graph.asScala.V(v1Id).out("created").in("created").range(1, 3)

  // override def get_g_VX1X_outXcreatedX_inEXcreatedX_rangeX1_3X_outV(v1Id: AnyRef) =
  //   graph.asScala.V(v1Id).out("created").inE("created").range(1, 3).outV

  // override def get_g_V_repeatXbothX_timesX3X_rangeX5_11X =
  //   graph.asScala.V.repeat(_.both).times(3).range(5, 11)

  // override def get_g_V_hasLabelXsoftwareX_asXsX_localXinEXcreatedX_valuesXweightX_fold_limitXlocal_1XX_asXwX_select_byXnameX_by =
  //   graph.asScala.V.hasLabel("software").as("s").local {
  //     _.inE("created").values("weight").fold.limit(Scope.local, 1)
  //   }.as("w").select.by("name").by.asInstanceOf[GremlinScala[JMap[String, AnyRef], _]] //TODO get rid of cast

  // override def get_g_V_hasLabelXsoftwareX_asXsX_localXinEXcreatedX_valuesXweightX_fold_rangeXlocal_1_3XX_asXwX_select_byXnameX_by =
  //   graph.asScala.V.hasLabel("software").as("s").local {
  //     _.inE("created").values("weight").fold.range(Scope.local, 1, 3)
  //   }.as("w").select.by("name").by.asInstanceOf[GremlinScala[JMap[String, AnyRef], _]] //TODO get rid of cast
  // }

  // class ScalaMapTest extends MapTest with StandardTest {
  //   override def get_g_VX1X_mapXnameX(v1Id: AnyRef) =
  //     graph.asScala.V(v1Id).map[String](_.value[String]("name"))

  //   override def get_g_VX1X_outE_label_mapXlengthX(v1Id: AnyRef) =
  //     graph.asScala.V(v1Id).outE.label.map(_.length: Integer)

  //   override def get_g_VX1X_out_mapXnameX_mapXlengthX(v1Id: AnyRef) =
  //     graph.asScala.V(v1Id).out.map(_.value[String]("name")).map(_.toString.length: Integer)

  //   override def get_g_V_asXaX_out_mapXa_nameX =
  //     graph.asScala.V.as("a").out.mapWithTraverser { t: Traverser[Vertex] ⇒
  //       t.path[Vertex]("a").value[String]("name")
  //     }

  //   override def get_g_V_asXaX_out_out_mapXa_name_it_nameX =
  //     graph.asScala.V.as("a").out.out.mapWithTraverser { t: Traverser[Vertex] ⇒
  //       val a = t.path[Vertex]("a")
  //       val aName = a.value[String]("name")
  //       val vName = t.get.value[String]("name")
  //       s"$aName$vName"
  //     }
  // }

  // class ScalaOrderTest extends OrderTest with StandardTest {
  //       override def get_g_V_name_order = graph.asScala.V.values[String]("name").order

  //   override def get_g_V_name_order_byXa1_b1X_byXb2_a2X = {
  //     println("XXXXXXXXXXXXXXXXXXXXXXXXXX")
  //     def a = 
  //       graph.asScala.V.values[String]("name")
  //       .order
  //       .by(lessThan = { case (a, b) => a.substring(1, 2) < b.substring(1, 2) }) 
  //       // .by(lessThan = { case (a, b) => b.substring(2, 3) < a.substring(2, 3) }) 
  //     a.toList foreach println
  //     println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ")
  // expected order:
  // assertEquals("marko", names.get(0));
  // assertEquals("vadas", names.get(1));
  // assertEquals("peter", names.get(2));
  // assertEquals("ripple", names.get(3));
  // assertEquals("josh", names.get(4));
  // assertEquals("lop", names.get(5));
  // return g.V().<String>values("name").order()
  // .by((a, b) -> a.substring(1, 2).compareTo(b.substring(1, 2)))
  // .by((a, b) -> b.substring(2, 3).compareTo(a.substring(2, 3)));

  // with first by step:
  // lop
  // josh
  // ripple
  // peter
  // marko
  // vadas

  // with only order:
  // josh
  // lop
  // marko
  // peter
  // ripple
  // vadas

  // graph.asScala.V.values[String]("name").order
  //   .by(lessThan = { case (a, b) => a.substring(1, 2) > b.substring(1, 2) }) 
  //   .by(lessThan = { case (a, b) => b.substring(2, 3) > b.substring(2, 3) }) 
  // ???
  //   a
  // }

  //       override def get_g_V_order_byXname_incrX_name =
  //         graph.asScala.V.order.by("name", Order.incr).values[String]("name")

  //       override def get_g_V_order_byXnameX_name =
  //         graph.asScala.V.order.by("name", Order.incr).values[String]("name")

  //       override def get_g_V_outE_order_byXweight_decrX_weight =
  //         graph.asScala.V.outE.order.by("weight", Order.decr).values[JDouble]("weight")

  //       override def get_g_V_order_byXname_a1_b1X_byXname_b2_a2X_name =
  //         graph.asScala.V.order
  //           .byP[String](elementPropertyKey = "name", lessThan = { case (a, b) => a.substring(1, 2) > b.substring(1, 2) })
  //           .byP[String](elementPropertyKey = "name", lessThan = { case (a, b) => a.substring(2, 3) > b.substring(2, 3) })
  //           .values[String]("name")

  //       override def get_g_V_asXaX_outXcreatedX_asXbX_order_byXshuffleX_select =
  //         graph.asScala.V.as("a").out("created").as("b").order.by(Order.shuffle).select

  //   override def get_g_VX1X_hasXlabel_personX_mapXmapXint_ageXX_orderXlocalX_byXvalueDecrX_byXkeyIncrX(v1Id: AnyRef) =
  //   {
  //         graph.asScala.V(v1Id).map{ v => 
  //           val jmap: JMap[Integer, Integer] = new java.util.HashMap[Integer, Integer]
  //           jmap.put(1, v.value[Integer]("age"))
  //           jmap.put(2, v.value[Integer]("age") * 2)
  //           jmap.put(3, v.value[Integer]("age") * 3)
  //           jmap.put(4, v.value[Integer]("age"))
  //           jmap 
  //         }.order(Scope.local).by(Order.valueDecr).by(Order.keyIncr)
  //   }

  //   override def get_g_V_order_byXoutE_count__decrX = 
  //     graph.asScala.V.order.byTraversal(_.outE.count, Order.decr)
  // }

  // class ScalaSelectTest extends SelectTest with StandardTest {

  //   // override def get_g_v1_asXaX_outXknowsX_asXbX_select(v1Id: AnyRef) =
  //   //   GremlinScala(g).v(v1Id).get.as("a").out("knows").as("b").select()
  //   
  //   // override def get_g_v1_asXaX_outXknowsX_asXbX_selectXnameX(v1Id: AnyRef) =
  //   //   GremlinScala(g).v(v1Id).get.as("a").out("knows").as("b").select().by { v: Vertex ⇒
  //   //     v.value[String]("name")
  //   //   }
  //   
  //   // override def get_g_v1_asXaX_outXknowsX_asXbX_selectXaX(v1Id: AnyRef) =
  //   //   GremlinScala(g).v(v1Id).get.as("a").out("knows").as("b").select(Seq("a"))
  //   
  //   // override def get_g_v1_asXaX_outXknowsX_asXbX_selectXa_nameX(v1Id: AnyRef) =
  //   //   ???
  //   // // GremlinScala(g).v(v1Id).get.as("a").out("knows").as("b")
  //   // //   .select(Seq("a"), {v: Vertex ⇒ v.value[String]("name")})
  //   
  //   // override def get_g_V_asXaX_out_asXbX_selectXnameX =
  //   //   GremlinScala(g).V.as("a").out.as("b").select { v: Vertex ⇒
  //   //     v.value[String]("name")
  //   //   }
  //   
  //   // override def get_g_V_asXaX_out_aggregate_asXbX_selectXnameX =
  //   //   GremlinScala(g).V.as("a").out.aggregate.as("b").select { v: Vertex ⇒
  //   //     v.value[String]("name")
  //   //   }
  //   
  //   // override def get_g_V_asXaX_name_order_asXbX_selectXname_itX = ???
  //   // return g.V().as("a").values("name").order().as("b").select(v -> ((Vertex) v).value("name"), Function.identity())
  //   // GremlinScala(g).V.as("a").values[String]("name").order.as("b").select { name: String ⇒
  //   //   v.value[String]("name")
  //   // }
  // }

  // class ScalaVertexTest extends VertexTest with StandardTest {
  //   override def get_g_V = graph.asScala.V

  //   override def get_g_VX1X_out(v1Id: AnyRef) = graph.asScala.V(v1Id).out

  //   override def get_g_VX2X_in(v2Id: AnyRef) = graph.asScala.V(v2Id).in

  //   override def get_g_VX4X_both(v4Id: AnyRef) = graph.asScala.V(v4Id).both

  //   override def get_g_E = graph.asScala.E

  //   override def get_g_VX1X_outE(v1Id: AnyRef) = graph.asScala.V(v1Id).outE

  //   override def get_g_VX2X_inE(v2Id: AnyRef) = graph.asScala.V(v2Id).inE

  //   override def get_g_VX4X_bothE(v4Id: AnyRef) = graph.asScala.V(v4Id).bothE

  //   override def get_g_VX4X_bothEXcreatedX(v4Id: AnyRef) = graph.asScala.V(v4Id).bothE("created")

  //   override def get_g_VX1X_outE_inV(v1Id: AnyRef) = graph.asScala.V(v1Id).outE.inV

  //   override def get_g_VX2X_inE_outV(v2Id: AnyRef) = graph.asScala.V(v2Id).inE.outV

  //   override def get_g_V_outE_hasXweight_1X_outV = graph.asScala.V.outE.has("weight", 1.0d).outV

  //   override def get_g_V_out_outE_inV_inE_inV_both_name = graph.asScala.V.out.outE.inV.inE.inV.both.values[String]("name")

  //   override def get_g_VX1X_outEXknowsX_bothV_name(v1Id: AnyRef) = graph.asScala.V(v1Id).outE("knows").bothV.values[String]("name")

  //   override def get_g_VX1X_outXknowsX(v1Id: AnyRef) = graph.asScala.V(v1Id).out("knows")

  //   override def get_g_VX1X_outXknows_createdX(v1Id: AnyRef) = graph.asScala.V(v1Id).out("knows", "created")

  //   override def get_g_VX1X_outEXknowsX_inV(v1Id: AnyRef) = graph.asScala.V(v1Id).outE("knows").inV

  //   override def get_g_VX1X_outEXknows_createdX_inV(v1Id: AnyRef) = graph.asScala.V(v1Id).outE("knows", "created").inV

  //   override def get_g_VX1X_outE_otherV(v1Id: AnyRef) = graph.asScala.V(v1Id).outE.otherV

  //   override def get_g_VX4X_bothE_otherV(v4Id: AnyRef) = graph.asScala.V(v4Id).bothE.otherV

  //   // override def get_g_VX4X_bothE_hasXweight_lt_1X_otherV(v4Id: AnyRef) = graph.asScala.V(v4Id).bothE.has("weight", Compare.lt, 1d).otherV

  //   override def get_g_V_out_out = graph.asScala.V.out.out

  //   override def get_g_VX1X_out_out_out(v1Id: AnyRef) = graph.asScala.V(v1Id).out.out.out

  //   override def get_g_VX1X_out_name(v1Id: AnyRef) = graph.asScala.V(v1Id).out.values[String]("name")

  //   override def get_g_VX1X_to_XOUT_knowsX(v1Id: AnyRef) = graph.asScala.V(v1Id).to(Direction.OUT, "knows")
  // }

  class ScalaAggregateTest extends AggregateTest with StandardTest {
    override def get_g_V_name_aggregateXxX_capXxX =
      graph.asScala.V.values("name").aggregate("x").cap("x")
        .asInstanceOf[GremlinScala[JList[String], _]]

    override def get_g_V_aggregateXxX_byXnameX_capXxX =
      graph.asScala.V.aggregate("x").by("name").cap("x")
        .asInstanceOf[GremlinScala[JList[String], _]]

    override def get_g_V_out_aggregateXaX_path =
      graph.asScala.V.out.aggregate("a").path
  }

  class ScalaCountTest extends CountTest with StandardTest {
    override def get_g_V_count =
      graph.asScala.V.count

    override def get_g_V_out_count =
      graph.asScala.V.out.count

    override def get_g_V_both_both_count =
      graph.asScala.V.both.both.count

    override def get_g_V_repeatXoutX_timesX3X_count =
      graph.asScala.V.repeat(_.out).times(3).count

    override def get_g_V_repeatXoutX_timesX8X_count =
      graph.asScala.V.repeat(_.out).times(8).count

    override def get_g_V_hasXnoX_count =
      graph.asScala.V.has(Key("no")).count

    override def get_g_V_fold_countXlocalX =
      graph.asScala.V.fold.count(Scope.local)
  }

  // class ScalaSideEffectTest extends sideEffect.SideEffectTest with StandardTest {
  // override def get_g_VX1X_sideEffectXstore_aX_name(v1Id: AnyRef) =
  // graph.asScala.V(v1Id).sideEffect("a", ArrayList::new).sideEffect(traverser -> =
  //       traverser.sideEffects[List]("a").clear
  //       traverser.sideEffects[List<Vertex>]("a").add(traverser.get)
  //   }).values[String]("name")

  // override def get_g_VX1X_out_sideEffectXincr_cX_name(v1Id: AnyRef) =
  //   graph.asScala.V(v1Id).sideEffect("c",  -> =
  //         final List<Integer> list = new ArrayList<>
  //         list.add(0)
  //         return list
  //     }).out.sideEffect(traverser -> =
  //         Integer temp = traverser.sideEffects[List<Integer>]("c").get(0)
  //         traverser.sideEffects[List<Integer>]("c").clear
  //         traverser.sideEffects[List<Integer>]("c").add(temp + 1)
  //     }).values[String]("name")

  // override def get_g_VX1X_out_sideEffectXX_name(v1Id: AnyRef) =
  //   graph.asScala.V(v1Id).out.sideEffect(traverser -> =
  //     }).values[String]("name")

  //old stuff:
  //   override def get_g_v1_sideEffectXstore_aX_name(v1Id: AnyRef) = {
  //     val a = new JArrayList[Vertex] //test is expecting a java arraylist..
  //     GremlinScala(g).v(v1Id).get.sideEffect("a", a).sideEffect { traverser ⇒
  //       a.add(traverser.get)
  //     }.values[String]("name")
  //   }

  //   override def get_g_v1_out_sideEffectXincr_cX_name(v1Id: AnyRef) = {
  //     val c = new JArrayList[Integer] //test is expecting a java arraylist..
  //     c.add(0)
  //     GremlinScala(g).v(v1Id).get.sideEffect("c", c).out.sideEffect { traverser ⇒
  //       val tmp = c.get(0)
  //       c.clear()
  //       c.add(tmp + 1)
  //     }.values[String]("name")
  //   }

  //   override def get_g_v1_out_sideEffectXX_name(v1Id: AnyRef) =
  //     GremlinScala(g).v(v1Id).get.out.sideEffect { traverser: Traverser[Vertex] ⇒
  //       // println("side effect")
  //     }.values[String]("name")
  // }

  // class ScalaSideEffectCapTest extends SideEffectCapTest with StandardTest {

  //   override def get_g_V_hasXageX_groupCountXa_nameX_out_capXaX =
  //     GremlinScala(g).V.has("age")
  //       .groupCount("a").by(_.value[String]("name"))
  //       .out.cap("a").traversal
  //       .asInstanceOf[Traversal[Vertex, JMap[String, JLong]]] //only for Scala 2.10...
  // }

  // class ScalaGroupCountTest extends GroupCountTest with StandardTest {

  //   override def get_g_V_outXcreatedX_groupCountXnameX =
  //     GremlinScala(g).V.out("created").groupCount.by { v: Vertex ⇒
  //       v.value[String]("name")
  //     }.traversal.asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

  //   override def get_g_V_outXcreatedX_name_groupCount =
  //     GremlinScala(g).V.out("created").values[String]("name").groupCount
  //       .traversal.asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

  //   override def get_g_V_outXcreatedX_name_groupCountXaX =
  //     GremlinScala(g).V.out("created").values[String]("name").groupCount("a")
  //       .traversal.asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

  //   override def get_g_V_filterXfalseX_groupCount =
  //     GremlinScala(g).V.filter(_ ⇒ false).groupCount
  //       .traversal.asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

  //   // override def get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_loops_lt_2X_capXaX =
  //   //   GremlinScala(g).V.as("x").out
  //   //     .groupCount("a").by(_.value[String]("name"))
  //   //     .jump("x", _.loops < 2)
  //   //     .cap("a")
  //   //     .traversal
  //   //     .asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]

  //   // override def get_g_V_asXxX_out_groupCountXa_nameX_jumpXx_2X_capXaX =
  //   //   GremlinScala(g).V.as("x").out
  //   //     .groupCount("a", _.value[String]("name"))
  //   //     .jump("x", 2)
  //   //     .cap("a")
  //   //     .traversal
  //   //     .asInstanceOf[Traversal[Vertex, JMap[AnyRef, JLong]]]
  // }

  // class ScalaGroupTest extends GroupTest with StandardTest {

  //   // override def get_g_V_groupByXnameX =
  //   //   GremlinScala(g).V.groupBy(_.value[String]("name"))
  //   //     .traversal.asInstanceOf[Traversal[Vertex, JMap[String, JCollection[Vertex]]]]
  //
  //   // override def get_g_V_hasXlangX_groupByXa_lang_nameX_out_capXaX =
  //   //   GremlinScala(g).V.has("lang").groupBy(
  //   //     sideEffectKey = "a",
  //   //     keyFunction = _.value[String]("lang"),
  //   //     valueFunction = _.value[String]("name")
  //   //   ).out.cap("a")
  //   //     .traversal
  //   //     .asInstanceOf[Traversal[Vertex, JMap[String, JCollection[String]]]] //only for Scala 2.10...
  //
  //   // override def get_g_V_hasXlangX_groupByXlang_1_sizeX =
  //   //   GremlinScala(g).V.has("lang").groupBy(
  //   //     keyFunction = _.value[String]("lang"),
  //   //     valueFunction = _ ⇒ 1,
  //   //     reduceFunction = { c: JCollection[_] ⇒ c.size }
  //   //   ).traversal.asInstanceOf[Traversal[Vertex, JMap[String, Integer]]]
  //
  //   // override def get_g_V_asXxX_out_groupByXa_name_sizeX_jumpXx_2X_capXaX =
  //   //   GremlinScala(g).V.as("x").out
  //   //     .groupBy(
  //   //       sideEffectKey = "a",
  //   //       keyFunction = _.value[String]("name"),
  //   //       valueFunction = v ⇒ v,
  //   //       reduceFunction = { c: JCollection[_] ⇒ c.size }
  //   //     ).jump("x", 2).cap("a")
  //   //     .traversal
  //   //     .asInstanceOf[Traversal[Vertex, JMap[String, Integer]]] //only for Scala 2.10...
  //
  //   // override def get_g_V_asXxX_out_groupByXa_name_sizeX_jumpXx_loops_lt_2X_capXaX =
  //   //   GremlinScala(g).V.as("x").out
  //   //     .groupBy(
  //   //       sideEffectKey = "a",
  //   //       keyFunction = _.value[String]("name"),
  //   //       valueFunction = v ⇒ v,
  //   //       reduceFunction = { c: JCollection[_] ⇒ c.size }
  //   //     ).jump("x", _.loops < 2).cap("a")
  //   //     .traversal
  //   //     .asInstanceOf[Traversal[Vertex, JMap[String, Integer]]] //only for Scala 2.10...
  // }
}

trait StandardTest {

  import scala.language.implicitConversions

  implicit def toTraversal[S, E](gs: GremlinScala[E, _]): Traversal[S, E] =
    gs.traversal.asInstanceOf[Traversal[S, E]]
}

import java.io.File
import java.util.{Map ⇒ JMap}

import gremlin.scala.Tests._
import org.apache.commons.configuration.Configuration
import org.apache.tinkerpop.gremlin._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.junit.runner.RunWith
import org.junit.runners.model.RunnerBuilder

class GremlinScalaStandardSuite(clazz: Class[_], builder: RunnerBuilder)
  extends AbstractGremlinSuite(clazz, builder,
    Array( //testsToExecute - all are in ProcessStandardSuite
      // classOf[ScalaDedupTest],
      classOf[ScalaFilterTest],
      classOf[ScalaSimplePathTest],
      classOf[ScalaCyclicPathTest],
      classOf[ScalaCoinTest],
      classOf[ScalaAggregateTest],
      classOf[ScalaCountTest]

    // classOf[ScalaMapTest],
    // classOf[ScalaVertexTest],
    // classOf[ScalaSideEffectTest]
    // classOf[ScalaSideEffectCapTest]
    // classOf[ScalaGroupCountTest]
    // classOf[ScalaGroupTest]
    // classOf[ScalaOrderTest]
    // classOf[ScalaSelectTest] //doesnt fully work yet.. we need a typesafe alternative
    // classOf[ScalaRangeTest]
    // classOf[ScalaHasTest] //three tests don't work for some reason...
    ),
    Array.empty, //testsToEnforce
    true, //gremlinFlavourSuite - don't enforce opt-ins for graph implementations
    TraversalEngine.Type.STANDARD //OLTP
    )

@RunWith(classOf[GremlinScalaStandardSuite])
@GraphProviderClass(
  provider = classOf[TinkerGraphGraphProvider],
  graph = classOf[TinkerGraph]
)
class ScalaTinkerGraphStandardTest {}

//TODO configure sbt to run with junit so it properly prints the test count - does that work in gremlin-java?

class TinkerGraphGraphProvider extends AbstractGraphProvider {

  import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal
  import org.apache.tinkerpop.gremlin.tinkergraph.structure._

  override def getImplementations = Set(
    classOf[TinkerEdge],
    classOf[TinkerElement],
    classOf[TinkerGraph],
    classOf[TinkerGraphVariables],
    classOf[TinkerProperty[_]],
    classOf[TinkerVertex],
    classOf[TinkerVertexProperty[_]],
    classOf[DefaultGraphTraversal[_, _]]
  // classOf[AnonymousGraphTraversal.Tokens]
  ): Set[Class[_]]

  override def getBaseConfiguration(
    graphName: String,
    test: Class[_],
    testMethodName: String,
    loadGraphWith: LoadGraphWith.GraphData
  ): JMap[String, AnyRef] =
    Map("gremlin.graph" → classOf[TinkerGraph].getName)

  override def clear(graph: Graph, configuration: Configuration): Unit =
    Option(graph) map { graph ⇒
      graph.close()
      if (configuration.containsKey("gremlin.tg.directory"))
        new File(configuration.getString("gremlin.tg.directory")).delete()
    }
}
