
import com.tinkerpop.gremlin.scala.jsr223.ScalaScript
import com.tinkerpop.blueprints.impls.tg._
import scala.collection.mutable
class Script(vals: mutable.Map[String, Any]) extends ScalaScript { //val a  = vals("a").asInstanceOf[Int]
          def result =  "dummy response" 
       }