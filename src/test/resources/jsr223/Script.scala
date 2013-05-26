import com.tinkerpop.gremlin.scala.jsr223.ScalaScript
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import scala.collection.mutable

class Script(vals: mutable.Map[String, Any]) extends ScalaScript {
  
   val a  = vals("a").asInstanceOf[Int]
   def result = a + 1        
}

