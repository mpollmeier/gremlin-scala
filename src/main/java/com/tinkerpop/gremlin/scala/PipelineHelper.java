package com.tinkerpop.gremlin.scala;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * needed for scala / java interop on overloaded method with varargs ;(
 * http://stackoverflow
 * .com/questions/3313929/how-do-i-disambiguate-in-scala-between
 * -methods-with-vararg-and-without
 */
public class PipelineHelper {
	// public static GremlinPipeline<?, ?> has(
	// final GremlinPipeline<?, ?> pipeline,
	// final String key, final Object... values) {
	// return pipeline.has(key, values);
	// }

	public static <S> GremlinPipeline<S, ? extends Element> has(
			final GremlinPipeline<S, ? extends Element> pipeline,
			final String key, final Object... values) {
		return pipeline.has(key, values);
	}
}
