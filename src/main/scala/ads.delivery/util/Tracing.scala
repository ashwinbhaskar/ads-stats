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
import com.colisweb.tracing.context.NoOpTracingContext
import ads.delivery.config.TracingConfig
import io.jaegertracing.Configuration.ReporterConfiguration
import io.jaegertracing.spi.Reporter
import io.jaegertracing.Configuration.SenderConfiguration
import io.jaegertracing.internal.samplers.HttpSamplingManager

object Tracing {

  def loggingTraceContextBuilder[F[_]: Sync: Timer]
      : F[TracingContextBuilder[F]] =
    LoggingTracingContext.builder[F]

  def jaegarTracingContext[F[_]: Timer: Sync](
      tracerConfig: TracingConfig
  ): F[TracingContextBuilder[F]] = {
    val serviceName = tracerConfig.getServiceName
    val host = tracerConfig.getAgentHost
    val port = tracerConfig.getAgentPort
    val samplingManagerHost = tracerConfig.getSamplingManagerHost
    val samplingManagerPort = tracerConfig.getSamplingManagerPort
    System.setProperty("JAEGER_AGENT_HOST", host)
    System.setProperty("JAEGER_AGENT_PORT", port.toString)
    System.setProperty("JAEGER_SERVICE_NAME", serviceName)
    System.setProperty(
      "JAEGER_SAMPLER_MANAGER_HOST_PORT",
      s"$samplingManagerHost:$samplingManagerPort"
    )
    val configuration = Configuration.fromEnv
    val tracer: Tracer = configuration.getTracer
    OpenTracingContext.builder[F, Tracer, Span](tracer)
  }

  def noOpTracingContext[F[_]: Timer: Sync]: F[TracingContextBuilder[F]] =
    NoOpTracingContext.builder[F]
}
