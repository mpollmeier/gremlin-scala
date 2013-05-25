import com.tinkerpop.gremlin.scala.jsr223.ScalaScript
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory

class GremlinScript extends ScalaScript {
   def result = TinkerGraphFactory.createTinkerGraph        
}

