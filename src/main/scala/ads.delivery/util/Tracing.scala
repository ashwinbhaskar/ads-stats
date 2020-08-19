package ads.delivery.util

import io.opentracing.Tracer
import io.opentracing.Span
import cats.effect.IO
import com.colisweb.tracing.core.TracingContextBuilder
import com.colisweb.tracing.context.OpenTracingContext

object Tracing {
    val tracer: Tracer = ???
    val tracingContextBuilder: IO[TracingContextBuilder[IO]] = 
        OpenTracingContext.builder[IO, Tracer, Span](tracer)
}
