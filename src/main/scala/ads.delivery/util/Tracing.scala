package ads.delivery.util

import io.opentracing.Tracer
import io.opentracing.Span
import io.opentracing._
import io.jaegertracing.Configuration
import com.colisweb.tracing.core.implicits._
import cats.effect.{IO, Timer, Sync}
import com.colisweb.tracing.context._
import com.colisweb.tracing.core.TracingContextBuilder
import com.colisweb.tracing.context.OpenTracingContext
import com.colisweb.tracing.context.LoggingTracingContext
import ads.delivery.config.TracingConfig
import io.jaegertracing.Configuration.ReporterConfiguration
import io.jaegertracing.spi.Reporter
import io.jaegertracing.Configuration.SenderConfiguration

object Tracing {

  def loggingTraceContextBuilder[F[_]: Sync: Timer]
      : F[TracingContextBuilder[F]] =
    LoggingTracingContext.builder[F]

  def jaegarTracincContext[F[_]: Timer: Sync](tracerConfig: TracingConfig): F[TracingContextBuilder[F]] = {
    val senderConfiguration = (new SenderConfiguration).withAgentHost(tracerConfig.getAgentHost).withAgentPort(tracerConfig.getAgentPort)
    val reporterConfiguration = (new ReporterConfiguration).withSender(senderConfiguration)
    val configuration = new Configuration(tracerConfig.getServiceName).withReporter(reporterConfiguration)
    val tracer: Tracer = configuration.getTracer
    OpenTracingContext.builder[F, Tracer, Span](tracer)
  }
}
