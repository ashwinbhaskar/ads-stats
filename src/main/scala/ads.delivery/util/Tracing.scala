package ads.delivery.util

import io.opentracing.Tracer
import io.opentracing.Span
import io.opentracing._
import com.colisweb.tracing.core.implicits._
import cats.effect.{IO, Timer, Sync}
import com.colisweb.tracing.context._
import com.colisweb.tracing.core.TracingContextBuilder
import com.colisweb.tracing.context.OpenTracingContext
import com.colisweb.tracing.context.LoggingTracingContext

object Tracing {
  val tracer: Tracer = ???

  def loggingTraceContextBuilder[F[_]: Sync: Timer]: F[TracingContextBuilder[F]] =
    LoggingTracingContext.builder[F]
  
  val tracingContextBuilder: IO[TracingContextBuilder[IO]] =
    OpenTracingContext.builder[IO, Tracer, Span](tracer)
}
