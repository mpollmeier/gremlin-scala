package gremlin.scala

import java.lang.{ Long ⇒ JLong, Double ⇒ JDouble }
import java.util.{ List ⇒ JList, ArrayList ⇒ JArrayList, Map ⇒ JMap, Collection ⇒ JCollection, Set ⇒ JSet }
import scala.collection.JavaConversions._

import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.step.filter._
import org.apache.tinkerpop.gremlin.process.traversal.step.branch._
import org.apache.tinkerpop.gremlin.process.traversal.step.map._
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect._
import org.apache.tinkerpop.gremlin.structure.{ T, Direction }
import org.apache.tinkerpop.gremlin.tinkergraph.structure.{ TinkerFactory, TinkerGraph }
import shapeless._
import shapeless.ops.hlist._
import org.apache.tinkerpop.gremlin.process.traversal._
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__

object Tests {

  class ScalaDedupTest extends DedupTest with StandardTest {
    override def get_g_V_both_dedup_name =
      GremlinScala(graph).V.both.dedup().values[String]("name")

    override def get_g_V_both_hasXlabel_softwareX_dedup_byXlangX_name =
      GremlinScala(graph).V.both.has(T.label, "software").dedup().by("lang").values[String]("name")

    override def get_g_V_both_name_order_byXa_bX_dedup_value =
      GremlinScala(graph).V.both.values[String]("name").order.by { (a, b) ⇒ a < b }.dedup()

    override def get_g_V_both_both_name_dedup =
      GremlinScala(graph).V.both.both.values[String]("name").dedup()

    override def get_g_V_both_both_dedup =
      GremlinScala(graph).V.both.both.dedup()

    override def get_g_V_both_both_dedup_byXlabelX =
      GremlinScala(graph).V.both.both.dedup().by(T.label)

    override def get_g_V_asXaX_both_asXbX_dedupXa_bX_byXlabelX_selectXa_bX =
      GremlinScala(graph).V.as("a").both.as("b").dedup("a", "b").by(T.label).select("a", "b")

    override def get_g_V_asXaX_outXcreatedX_asXbX_inXcreatedX_asXcX_dedupXa_bX_path =
      GremlinScala(graph).V.as("a").out("created").as("b").in("created").as("c").dedup("a", "b").path

    override def get_g_V_group_byXlabelX_byXbothE_valuesXweightX_foldX_byXdedupXlocalXX =
      GremlinScala(graph).V.group()
        .by(T.label)
        .by(__.bothE().values[java.lang.Float]("weight").fold())
        .by(__.dedup(Scope.local))
        .asInstanceOf[GremlinScala[JMap[String, JSet[JDouble]], _]]
    //TODO: get rid of cast
  }

  class ScalaFilterTest extends FilterTest with StandardTest {
    override def get_g_V_filterXfalseX = GremlinScala(graph).V.filter(_ ⇒ false)

    override def get_g_V_filterXtrueX = GremlinScala(graph).V.filter(_ ⇒ true)

    override def get_g_V_filterXlang_eq_javaX =
      GremlinScala(graph).V.filter(_.property("lang").orElse("none") == "java")

    override def get_g_VX1X_filterXage_gt_30X(v1Id: AnyRef) =
      GremlinScala(graph).V(v1Id).filter(_.property[Int]("age").orElse(0) > 30)

    override def get_g_V_filterXname_startsWith_m_OR_name_startsWith_pX = GremlinScala(graph).V.filter { v ⇒
      val name = v.value[String]("name")
      name.startsWith("m") || name.startsWith("p")
    }

    override def get_g_VX1X_out_filterXage_gt_30X(v1Id: AnyRef) =
      GremlinScala(graph).V(v1Id).out.filter(_.property[Int]("age").orElse(0) > 30)

    override def get_g_E_filterXfalseX = GremlinScala(graph).E.filter(_ ⇒ false)

    override def get_g_E_filterXtrueX = GremlinScala(graph).E.filter(_ ⇒ true)
  }

  class ScalaSimplePathTest extends SimplePathTest with StandardTest {
    override def get_g_VX1X_outXcreatedX_inXcreatedX_simplePath(v1Id: AnyRef) =
      GremlinScala(graph).V(v1Id).out("created").in("created").simplePath

    override def get_g_V_repeatXboth_simplePathX_timesX3X_path =
      GremlinScala(graph).V.repeat(_.both.simplePath).times(3).path
  }

  class ScalaCyclicPathTest extends CyclicPathTest with StandardTest {
    override def get_g_VX1X_outXcreatedX_inXcreatedX_cyclicPath(v1Id: AnyRef) =
      GremlinScala(graph).V(v1Id).out("created").in("created").cyclicPath

    override def get_g_VX1X_outXcreatedX_inXcreatedX_cyclicPath_path(v1Id: AnyRef) =
      GremlinScala(graph).V(v1Id).out("created").in("created").cyclicPath.path
  }

  class ScalaHasTest extends HasTest with StandardTest {
    override def get_g_EX11X_outV_outE_hasXid_10X(e11Id: AnyRef, e8Id: AnyRef) =
    {
      def a = GremlinScala(graph).E(e11Id).outV.outE.has(T.id, e8Id)

      // println("XXXXXXXXXXXXX")
      // println(e8Id)
      // println(GS(graph).E(e8Id).toList)
      // a.toList foreach println
      // println("XXXXXXXXXXXXX")

      a
    }

    override def get_g_V_outXcreatedX_hasXname__mapXlengthX_isXgtX3XXX_name =
      GremlinScala(graph).V.out("created")
        .has[String, Int]("name", _.map(_.length).is(P.gt(3)))
        .values[String]("name")

    override def get_g_VX1X_hasXkeyX(v1Id: AnyRef, key: String) =
      GremlinScala(graph).V(v1Id).has(key)

    override def get_g_VX1X_hasXname_markoX(v1Id: AnyRef) =
      GremlinScala(graph).V(v1Id).has("name", "marko")

    override def get_g_V_hasXname_markoX =
      GremlinScala(graph).V.has("name", "marko")

    override def get_g_V_hasXname_blahX =
      GremlinScala(graph).V.has("name", "blah")

    override def get_g_V_hasXblahX =
      GremlinScala(graph).V.has("blah")

    override def get_g_VX1X_hasXage_gt_30X(v1Id: AnyRef) =
      GremlinScala(graph).V(v1Id).has("age", P.gt(30))

    override def get_g_VX1X_out_hasIdX2X(v1Id: AnyRef, v2Id: AnyRef) =
    {
      def a = GremlinScala(graph).V(v1Id).out.hasId(v2Id)
      // println("XXXXXXXXXXXXX")
      // println(a.toList)
      // println("XXXXXXXXXXXXXY")
      a
    }

    override def get_g_VX1X_out_hasIdX2_3X(v1Id: AnyRef, v2Id: AnyRef, v3Id: AnyRef) =
      GremlinScala(graph).V(v1Id).out.hasId(v2Id, v3Id)

    override def get_g_V_hasXage_gt_30X =
    {
      def a = GremlinScala(graph).V.has("age", P.gt(30))
      // def b = g.V().has("age", P.gt(30))
      // def b: GraphTraversal[Vertex, Vertex] = g.V().has("age", P.gt(30))
      // println("XXXXXXXXXXXXXXXXXX")
      // println(b.toList)
      // println("YYYYYYYYYYYYYYYY")
      // b
      a
    }

    override def get_g_EX7X_hasLabelXknowsX(e7Id: AnyRef) =
      GremlinScala(graph).E(e7Id).hasLabel("knows")

    override def get_g_E_hasLabelXknowsX =
      GremlinScala(graph).E.hasLabel("knows")

    override def get_g_E_hasLabelXuses_traversesX =
      GremlinScala(graph).E.hasLabel("uses", "traverses")

    override def get_g_V_hasLabelXperson_software_blahX =
      GremlinScala(graph).V.hasLabel("person", "software", "blah")

    override def get_g_V_hasXperson_name_markoX_age =
      GremlinScala(graph).V.has("person", "name", "marko").values[Integer]("age")

    override def get_g_VX1X_outE_hasXweight_inside_0_06X_inV(v1Id: AnyRef) =
      GremlinScala(graph).V(v1Id).outE.has("weight", P.inside(0.0d, 0.6d)).inV

    override def get_g_VX1X_out_hasXid_lt_3X(v1Id: AnyRef, v3Id: AnyRef) =
      GremlinScala(graph).V(v1Id).out.has(T.id, P.lt(v3Id))

    override def get_g_V_hasXage_isXgt_30XX =
      GremlinScala(graph).V.has("age", __.is(P.gt(30)))

    override def get_g_V_hasXlocationX =
      GremlinScala(graph).V.has("location")
  }

  class ScalaCoinTest extends CoinTest with StandardTest {
    override def get_g_V_coinX1X = GremlinScala(graph).V.coin(1.0d)
    override def get_g_V_coinX0X = GremlinScala(graph).V.coin(0.0d)
  }

  // class ScalaRangeTest extends RangeTest with StandardTest {
  //       override def get_g_VX1X_out_limitX2X(v1Id: AnyRef) =
  //         GremlinScala(graph).V(v1Id).out.limit(2)

  //       override def get_g_V_localXoutE_limitX1X_inVX_limitX3X =
  //         GremlinScala(graph).V.local(_.outE.limit(1)).inV.limit(3)

  //       override def get_g_VX1X_outXknowsX_outEXcreatedX_rangeX0_1X_inV(v1Id: AnyRef) =
  //         GremlinScala(graph).V(v1Id).out("knows").outE("created").range(0, 1).inV

  //       override def get_g_VX1X_outXknowsX_outXcreatedX_rangeX0_1X(v1Id: AnyRef) =
  //         GremlinScala(graph).V(v1Id).out("knows").out("created").range(0, 1)

  //       override def get_g_VX1X_outXcreatedX_inXcreatedX_rangeX1_3X(v1Id: AnyRef) =
  //         GremlinScala(graph).V(v1Id).out("created").in("created").range(1, 3)

  //       override def get_g_VX1X_outXcreatedX_inEXcreatedX_rangeX1_3X_outV(v1Id: AnyRef) =
  //         GremlinScala(graph).V(v1Id).out("created").inE("created").range(1, 3).outV

  //       override def get_g_V_repeatXbothX_timesX3X_rangeX5_11X =
  //         GremlinScala(graph).V.repeat(_.both).times(3).range(5, 11)

        // override def get_g_V_asXaX_in_asXaX_in_asXaX_selectXaX_byXunfold_valuesXnameX_foldX_limitXlocal_2X =
          // GremlinScala(graph).V.as("a").in.as("a").in.as("a").select[JList[String]]("a").by(unfold.values("name").fold).limit(local, 2)

        // override def get_g_V_asXaX_in_asXaX_in_asXaX_selectXaX_byXunfold_valuesXnameX_foldX_limitXlocal_1X =
        //   GremlinScala(graph).V.as("a").in.as("a").in.as("a").select[JList[String]]("a").by(unfold.values("name").fold).limit(local, 1)


        // override def get_g_V_asXaX_out_asXaX_out_asXaX_selectXaX_byXunfold_valuesXnameX_foldX_rangeXlocal_1_3X =
        //   GremlinScala(graph).V.as("a").out.as("a").out.as("a").select[List<String>]("a").by(unfold.values("name").fold).range(local, 1, 3)


        // override def get_g_V_asXaX_out_asXaX_out_asXaX_selectXaX_byXunfold_valuesXnameX_foldX_rangeXlocal_1_2X =
        //   GremlinScala(graph).V.as("a").out.as("a").out.as("a").select[List<String>]("a").by(unfold.values("name").fold).range(local, 1, 2)


        // override def get_g_V_asXaX_out_asXaX_out_asXaX_selectXaX_byXunfold_valuesXnameX_foldX_rangeXlocal_4_5X =
        //   GremlinScala(graph).V.as("a").out.as("a").out.as("a").select[List<String>]("a").by(unfold.values("name").fold).range(local, 4, 5)


        // override def get_g_V_asXaX_in_asXbX_in_asXcX_selectXa_b_cX_byXnameX_limitXlocal_2X =
        //   GremlinScala(graph).V.as("a").in.as("b").in.as("c").select[Map<String, String>]("a","b","c").by("name").limit(local, 2)


        // override def get_g_V_asXaX_in_asXbX_in_asXcX_selectXa_b_cX_byXnameX_limitXlocal_1X =
        //   GremlinScala(graph).V.as("a").in.as("b").in.as("c").select[Map<String, String>]("a","b","c").by("name").limit(local, 1)


        // override def get_g_V_asXaX_out_asXbX_out_asXcX_selectXa_b_cX_byXnameX_rangeXlocal_1_3X =
        //   GremlinScala(graph).V.as("a").out.as("b").out.as("c").select[Map<String, String>]("a","b","c").by("name").range(local, 1, 3)


        // override def get_g_V_asXaX_out_asXbX_out_asXcX_selectXa_b_cX_byXnameX_rangeXlocal_1_2X =
        //   GremlinScala(graph).V.as("a").out.as("b").out.as("c").select[Map<String, String>]("a","b","c").by("name").range(local, 1, 2)




    // override def get_g_VX1X_out_limitX2X(v1Id: AnyRef) =
    //   GremlinScala(graph).V(v1Id).out.limit(2)

    // override def get_g_V_localXoutE_limitX1X_inVX_limitX3X =
    //   GremlinScala(graph).V.local(_.outE.limit(1)).inV.limit(3)

    // override def get_g_VX1X_outXknowsX_outEXcreatedX_rangeX0_1X_inV(v1Id: AnyRef) =
    //   GremlinScala(graph).V(v1Id).out("knows").outE("created").range(0, 1).inV

    // override def get_g_VX1X_outXknowsX_outXcreatedX_rangeX0_1X(v1Id: AnyRef) =
    //   GremlinScala(graph).V(v1Id).out("knows").out("created").range(0, 1)

    // override def get_g_VX1X_outXcreatedX_inXcreatedX_rangeX1_3X(v1Id: AnyRef) =
    //   GremlinScala(graph).V(v1Id).out("created").in("created").range(1, 3)

    // override def get_g_VX1X_outXcreatedX_inEXcreatedX_rangeX1_3X_outV(v1Id: AnyRef) =
    //   GremlinScala(graph).V(v1Id).out("created").inE("created").range(1, 3).outV

    // override def get_g_V_repeatXbothX_timesX3X_rangeX5_11X =
    //   GremlinScala(graph).V.repeat(_.both).times(3).range(5, 11)

    // override def get_g_V_hasLabelXsoftwareX_asXsX_localXinEXcreatedX_valuesXweightX_fold_limitXlocal_1XX_asXwX_select_byXnameX_by =
    //   GremlinScala(graph).V.hasLabel("software").as("s").local {
    //     _.inE("created").values("weight").fold.limit(Scope.local, 1)
    //   }.as("w").select.by("name").by.asInstanceOf[GremlinScala[JMap[String, AnyRef], _]] //TODO get rid of cast

    // override def get_g_V_hasLabelXsoftwareX_asXsX_localXinEXcreatedX_valuesXweightX_fold_rangeXlocal_1_3XX_asXwX_select_byXnameX_by =
    //   GremlinScala(graph).V.hasLabel("software").as("s").local {
    //     _.inE("created").values("weight").fold.range(Scope.local, 1, 3)
    //   }.as("w").select.by("name").by.asInstanceOf[GremlinScala[JMap[String, AnyRef], _]] //TODO get rid of cast
  // }

  // class ScalaMapTest extends MapTest with StandardTest {
  //   override def get_g_VX1X_mapXnameX(v1Id: AnyRef) =
  //     GremlinScala(graph).V(v1Id).map[String](_.value[String]("name"))

  //   override def get_g_VX1X_outE_label_mapXlengthX(v1Id: AnyRef) =
  //     GremlinScala(graph).V(v1Id).outE.label.map(_.length: Integer)

  //   override def get_g_VX1X_out_mapXnameX_mapXlengthX(v1Id: AnyRef) =
  //     GremlinScala(graph).V(v1Id).out.map(_.value[String]("name")).map(_.toString.length: Integer)

  //   override def get_g_V_asXaX_out_mapXa_nameX =
  //     GremlinScala(graph).V.as("a").out.mapWithTraverser { t: Traverser[Vertex] ⇒
  //       t.path[Vertex]("a").value[String]("name")
  //     }

  //   override def get_g_V_asXaX_out_out_mapXa_name_it_nameX =
  //     GremlinScala(graph).V.as("a").out.out.mapWithTraverser { t: Traverser[Vertex] ⇒
  //       val a = t.path[Vertex]("a")
  //       val aName = a.value[String]("name")
  //       val vName = t.get.value[String]("name")
  //       s"$aName$vName"
  //     }
  // }

  // class ScalaOrderTest extends OrderTest with StandardTest {
  //       override def get_g_V_name_order = GremlinScala(graph).V.values[String]("name").order

  //   override def get_g_V_name_order_byXa1_b1X_byXb2_a2X = {
  //     println("XXXXXXXXXXXXXXXXXXXXXXXXXX")
  //     def a = 
  //       GremlinScala(graph).V.values[String]("name")
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

  // GremlinScala(graph).V.values[String]("name").order
  //   .by(lessThan = { case (a, b) => a.substring(1, 2) > b.substring(1, 2) }) 
  //   .by(lessThan = { case (a, b) => b.substring(2, 3) > b.substring(2, 3) }) 
  // ???
  //   a
  // }

  //       override def get_g_V_order_byXname_incrX_name =
  //         GremlinScala(graph).V.order.by("name", Order.incr).values[String]("name")

  //       override def get_g_V_order_byXnameX_name =
  //         GremlinScala(graph).V.order.by("name", Order.incr).values[String]("name")

  //       override def get_g_V_outE_order_byXweight_decrX_weight =
  //         GremlinScala(graph).V.outE.order.by("weight", Order.decr).values[JDouble]("weight")

  //       override def get_g_V_order_byXname_a1_b1X_byXname_b2_a2X_name =
  //         GremlinScala(graph).V.order
  //           .byP[String](elementPropertyKey = "name", lessThan = { case (a, b) => a.substring(1, 2) > b.substring(1, 2) })
  //           .byP[String](elementPropertyKey = "name", lessThan = { case (a, b) => a.substring(2, 3) > b.substring(2, 3) })
  //           .values[String]("name")

  //       override def get_g_V_asXaX_outXcreatedX_asXbX_order_byXshuffleX_select =
  //         GremlinScala(graph).V.as("a").out("created").as("b").order.by(Order.shuffle).select

  //   override def get_g_VX1X_hasXlabel_personX_mapXmapXint_ageXX_orderXlocalX_byXvalueDecrX_byXkeyIncrX(v1Id: AnyRef) =
  //   {
  //         GremlinScala(graph).V(v1Id).map{ v => 
  //           val jmap: JMap[Integer, Integer] = new java.util.HashMap[Integer, Integer]
  //           jmap.put(1, v.value[Integer]("age"))
  //           jmap.put(2, v.value[Integer]("age") * 2)
  //           jmap.put(3, v.value[Integer]("age") * 3)
  //           jmap.put(4, v.value[Integer]("age"))
  //           jmap 
  //         }.order(Scope.local).by(Order.valueDecr).by(Order.keyIncr)
  //   }

  //   override def get_g_V_order_byXoutE_count__decrX = 
  //     GremlinScala(graph).V.order.byTraversal(_.outE.count, Order.decr)
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
  //   override def get_g_V = GremlinScala(graph).V

  //   override def get_g_VX1X_out(v1Id: AnyRef) = GremlinScala(graph).V(v1Id).out

  //   override def get_g_VX2X_in(v2Id: AnyRef) = GremlinScala(graph).V(v2Id).in

  //   override def get_g_VX4X_both(v4Id: AnyRef) = GremlinScala(graph).V(v4Id).both

  //   override def get_g_E = GremlinScala(graph).E

  //   override def get_g_VX1X_outE(v1Id: AnyRef) = GremlinScala(graph).V(v1Id).outE

  //   override def get_g_VX2X_inE(v2Id: AnyRef) = GremlinScala(graph).V(v2Id).inE

  //   override def get_g_VX4X_bothE(v4Id: AnyRef) = GremlinScala(graph).V(v4Id).bothE

  //   override def get_g_VX4X_bothEXcreatedX(v4Id: AnyRef) = GremlinScala(graph).V(v4Id).bothE("created")

  //   override def get_g_VX1X_outE_inV(v1Id: AnyRef) = GremlinScala(graph).V(v1Id).outE.inV

  //   override def get_g_VX2X_inE_outV(v2Id: AnyRef) = GremlinScala(graph).V(v2Id).inE.outV

  //   override def get_g_V_outE_hasXweight_1X_outV = GremlinScala(graph).V.outE.has("weight", 1.0d).outV

  //   override def get_g_V_out_outE_inV_inE_inV_both_name = GremlinScala(graph).V.out.outE.inV.inE.inV.both.values[String]("name")

  //   override def get_g_VX1X_outEXknowsX_bothV_name(v1Id: AnyRef) = GremlinScala(graph).V(v1Id).outE("knows").bothV.values[String]("name")

  //   override def get_g_VX1X_outXknowsX(v1Id: AnyRef) = GremlinScala(graph).V(v1Id).out("knows")

  //   override def get_g_VX1X_outXknows_createdX(v1Id: AnyRef) = GremlinScala(graph).V(v1Id).out("knows", "created")

  //   override def get_g_VX1X_outEXknowsX_inV(v1Id: AnyRef) = GremlinScala(graph).V(v1Id).outE("knows").inV

  //   override def get_g_VX1X_outEXknows_createdX_inV(v1Id: AnyRef) = GremlinScala(graph).V(v1Id).outE("knows", "created").inV

  //   override def get_g_VX1X_outE_otherV(v1Id: AnyRef) = GremlinScala(graph).V(v1Id).outE.otherV

  //   override def get_g_VX4X_bothE_otherV(v4Id: AnyRef) = GremlinScala(graph).V(v4Id).bothE.otherV

  //   // override def get_g_VX4X_bothE_hasXweight_lt_1X_otherV(v4Id: AnyRef) = GremlinScala(graph).V(v4Id).bothE.has("weight", Compare.lt, 1d).otherV

  //   override def get_g_V_out_out = GremlinScala(graph).V.out.out

  //   override def get_g_VX1X_out_out_out(v1Id: AnyRef) = GremlinScala(graph).V(v1Id).out.out.out

  //   override def get_g_VX1X_out_name(v1Id: AnyRef) = GremlinScala(graph).V(v1Id).out.values[String]("name")

  //   override def get_g_VX1X_to_XOUT_knowsX(v1Id: AnyRef) = GremlinScala(graph).V(v1Id).to(Direction.OUT, "knows")
  // }

  class ScalaAggregateTest extends AggregateTest with StandardTest {
    override def get_g_V_name_aggregateXxX_capXxX =
      GremlinScala(graph).V.values("name").aggregate("x").cap("x")
        .asInstanceOf[GremlinScala[JList[String], _]]

    override def get_g_V_aggregateXxX_byXnameX_capXxX =
      GremlinScala(graph).V.aggregate("x").by("name").cap("x")
        .asInstanceOf[GremlinScala[JList[String], _]]

    override def get_g_V_out_aggregateXaX_path =
      GremlinScala(graph).V.out.aggregate("a").path
  }

  class ScalaCountTest extends CountTest with StandardTest {
    override def get_g_V_count =
      GremlinScala(graph).V.count

    override def get_g_V_out_count =
      GremlinScala(graph).V.out.count

    override def get_g_V_both_both_count =
      GremlinScala(graph).V.both.both.count

    override def get_g_V_repeatXoutX_timesX3X_count =
      GremlinScala(graph).V.repeat(_.out).times(3).count

    override def get_g_V_repeatXoutX_timesX8X_count =
      GremlinScala(graph).V.repeat(_.out).times(8).count

    override def get_g_V_hasXnoX_count =
      GremlinScala(graph).V.has("no").count

    override def get_g_V_fold_countXlocalX =
      GremlinScala(graph).V.fold.count(Scope.local)
  }

  // class ScalaSideEffectTest extends sideEffect.SideEffectTest with StandardTest {
  // override def get_g_VX1X_sideEffectXstore_aX_name(v1Id: AnyRef) =
  // GremlinScala(graph).V(v1Id).sideEffect("a", ArrayList::new).sideEffect(traverser -> =
  //       traverser.sideEffects[List]("a").clear
  //       traverser.sideEffects[List<Vertex>]("a").add(traverser.get)
  //   }).values[String]("name")

  // override def get_g_VX1X_out_sideEffectXincr_cX_name(v1Id: AnyRef) =
  //   GremlinScala(graph).V(v1Id).sideEffect("c",  -> =
  //         final List<Integer> list = new ArrayList<>
  //         list.add(0)
  //         return list
  //     }).out.sideEffect(traverser -> =
  //         Integer temp = traverser.sideEffects[List<Integer>]("c").get(0)
  //         traverser.sideEffects[List<Integer>]("c").clear
  //         traverser.sideEffects[List<Integer>]("c").add(temp + 1)
  //     }).values[String]("name")

  // override def get_g_VX1X_out_sideEffectXX_name(v1Id: AnyRef) =
  //   GremlinScala(graph).V(v1Id).out.sideEffect(traverser -> =
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

import Tests._
import org.apache.tinkerpop.gremlin._
import org.junit.runners.model.RunnerBuilder
import org.junit.runner.RunWith
import java.util.{ Map ⇒ JMap }
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.apache.tinkerpop.gremlin.structure.StructureStandardSuite
import org.apache.commons.configuration.Configuration
import java.io.File
class GremlinScalaStandardSuite(clazz: Class[_], builder: RunnerBuilder)
  extends AbstractGremlinSuite(clazz, builder,
    Array( //testsToExecute - all are in ProcessStandardSuite
      classOf[ScalaDedupTest],
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
  import org.apache.tinkerpop.gremlin.tinkergraph.structure._
  import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal

  override def getImplementations =
    Set(
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
