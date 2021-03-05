package ads.delivery.util

import cats.effect.kernel.Sync
import ads.delivery.config.TracingConfig
import natchez.jaeger.Jaeger
import natchez.EntryPoint
import cats.effect.kernel.Resource
import io.jaegertracing.Configuration.SamplerConfiguration
import io.jaegertracing.Configuration.ReporterConfiguration

object Tracing {

  def loggingTraceContextBuilder[F[_]: Sync]
      : Resource[F, EntryPoint[F]] = ???

  def jaegarTracingContext[F[_]: Sync](
      tracerConfig: TracingConfig
  ): Resource[F, EntryPoint[F]] = {
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
    val sampler = SamplerConfiguration.fromEnv()
    val reporter = ReporterConfiguration.fromEnv()
    Jaeger.entryPoint[F](system = "ads-stats"){c => 
      Sync[F].delay {
        c.withSampler(sampler)
          .withReporter(reporter)
          .getTracer
      }
    }
  }

  def noOpTracingContext[F[_]: Sync]: Resource[F,EntryPoint[F]] = ???
}
